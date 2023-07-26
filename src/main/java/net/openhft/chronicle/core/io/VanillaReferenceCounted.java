/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;

import static net.openhft.chronicle.core.internal.CloseableUtils.asString;

/**
 * This class provides a basic implementation of the {@link MonitorReferenceCounted} interface.
 * It is responsible for keeping track of reference counts and releasing resources
 * once they are no longer needed.
 */
public final class  VanillaReferenceCounted implements MonitorReferenceCounted {

    private static final long VALUE;

    static {
        VALUE = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(VanillaReferenceCounted.class, "value"));
    }

    private final Runnable onRelease;
    private final Class<?> type;
    private final ReferenceChangeListenerManager referenceChangeListeners;
    // must be volatile
    @UsedViaReflection
    private volatile int value = 1;
    private volatile boolean released = false;
    private boolean unmonitored;
    private StackTrace releasedHere;

    /**
     * Constructs a new instance of {@code VanillaReferenceCounted} with the specified action to be performed
     * on release and the class type of the resource.
     *
     * @param onRelease The action to be executed once the reference count drops to 0 and the resource is released.
     * @param type      The class type of the resource being reference counted.
     */
    VanillaReferenceCounted(final Runnable onRelease, Class<?> type) {
        this.onRelease = onRelease;
        this.type = type;
        referenceChangeListeners = new ReferenceChangeListenerManager(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StackTrace createdHere() {
        return null;
    }

    @Deprecated(/* To be removed in 2.25 */)
    @Override
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException(type.getName() + " no reservations for " + asString(owner));
        // otherwise, not sure.
        return true;
    }

    /**
     * Reserves the resource for the provided reference owner.
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the object has already been released.
     */
    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw newReleasedClosedIllegalStateException();
            }
            if (valueCompareAndSet(v, v + 1)) {
                referenceChangeListeners.notifyAdded(id);
                break;
            }
        }
    }

    /**
     * Transfers the reservation of the resource from one reference owner to another.
     *
     * @param from The current reference owner.
     * @param to   The new reference owner.
     * @throws ClosedIllegalStateException If the object has already been released.
     */
    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException {
        throwExceptionIfReleased();
        referenceChangeListeners.notifyTransferred(from, to);
    }

    /**
     * Attempts to reserve the resource for the provided reference owner.
     *
     * @param id The reference owner.
     * @return {@code true} if the reservation was successful, {@code false} if the object has already been released.
     */
    @Override
    public boolean tryReserve(ReferenceOwner id) {
        for (; ; ) {
            int v = value;
            if (v <= 0)
                return false;

            if (valueCompareAndSet(v, v + 1)) {
                referenceChangeListeners.notifyAdded(id);
                return true;
            }
        }
    }

    private boolean valueCompareAndSet(int from, int to) {
        return UnsafeMemory.INSTANCE.compareAndSwapInt(this, VALUE, from, to);
    }

    private int valueGetAndSet(int to) {
        return UnsafeMemory.INSTANCE.getAndSetInt(this, VALUE, to);
    }

    /**
     * Releases the resource reserved by the provided reference owner.
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the object has already been released.
     */
    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw newReleasedClosedIllegalStateException();
            }
            int count = v - 1;
            if (valueCompareAndSet(v, count)) {
                referenceChangeListeners.notifyRemoved(id);
                if (count == 0) {
                    callOnRelease();
                }
                break;
            }
        }
    }

    private void callOnRelease() throws ClosedIllegalStateException {
        if (released && !Jvm.supportThread())
            throw new ClosedIllegalStateException(type.getName() + " already released", releasedHere);
        releasedHere = Jvm.isResourceTracing() ? new StackTrace("Released here") : null;
        released = true;
        onRelease.run();
        referenceChangeListeners.clear();
    }

    /**
     * Releases the last reference of the resource.
     *
     * @param id The reference owner.
     * @throws IllegalStateException If the object has more references.
     */
    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        Exception thrownException = null;
        try {
            release(id);
        } catch (Exception e) {
            thrownException = e;
        }
        if (value > 0) {
            final IllegalStateException ise = new IllegalStateException(type.getName() + " not the last released");
            if (thrownException != null) {
                ise.addSuppressed(thrownException);
            }
            throw ise;
        }
        if (thrownException != null) {
            Jvm.rethrow(thrownException);
        }
    }

    @Override
    public int refCount() {
        return value;
    }

    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        if (refCount() > 0)
            throw new IllegalStateException(type.getName() + " still reserved, count=" + refCount());
    }

    @Override
    public void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
        if (valueGetAndSet(0) > 0) {
            if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING)
                Jvm.warn().on(type, "Discarded without being released");
            callOnRelease();
        }
    }

    @Override
    public void unmonitored(boolean unmonitored) {
        this.unmonitored = unmonitored;
    }

    @Override
    public boolean unmonitored() {
        return unmonitored;
    }

    /**
     * Adds a {@link ReferenceChangeListener} that will be notified when references are added or removed.
     *
     * @param referenceChangeListener The {@link ReferenceChangeListener} to be added.
     */
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    /**
     * Removes a {@link ReferenceChangeListener} from being notified when references are added or removed.
     *
     * @param referenceChangeListener The {@link ReferenceChangeListener} to be removed.
     */
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    private ClosedIllegalStateException newReleasedClosedIllegalStateException() {
        return new ClosedIllegalStateException(type.getName() + " released");
    }
}