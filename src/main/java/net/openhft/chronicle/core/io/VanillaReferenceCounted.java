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

/**
 * This class provides a basic implementation of the {@link MonitorReferenceCounted} interface.
 * <p>
 * It is responsible for keeping track of reference counts and releasing resources
 * once they are no longer needed. This implementation uses low-level atomic operations
 * provided by {@link UnsafeMemory} to manage the reference count in a thread-safe manner.
 * </p>
 */
public final class VanillaReferenceCounted implements MonitorReferenceCounted {

    private static final long VALUE;

    static {
        // Initialize VALUE to the memory offset of the 'value' field
        VALUE = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(VanillaReferenceCounted.class, "value"));
    }

    // Action to be executed when the resource is released
    private final Runnable onRelease;

    // The class type of the resource being reference counted
    private final Class<?> type;

    // Manager for reference change listeners
    private final ReferenceChangeListenerManager referenceChangeListeners;

    // Volatile field to keep track of the reference count
    @UsedViaReflection
    private volatile int value = 1;

    // Volatile flag indicating whether the resource has been released
    private volatile boolean released = false;

    // Flag indicating whether this object is unmonitored
    private boolean unmonitored;

    // Stack trace of where the object was released, if available
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
     * Returns {@code null} as this class does not track the creation stack trace.
     *
     * @return {@code null}.
     */
    @Override
    public StackTrace createdHere() {
        return null;
    }

    /**
     * Reserves the resource for the provided reference owner.
     * <p>
     * Increments the reference count atomically. If the resource has already been released,
     * an exception is thrown.
     * </p>
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
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
     * <p>
     * This operation does not modify the reference count but notifies listeners of the transfer.
     * If the resource has already been released, an exception is thrown.
     * </p>
     *
     * @param from The current reference owner.
     * @param to   The new reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException {
        throwExceptionIfReleased();
        referenceChangeListeners.notifyTransferred(from, to);
    }

    /**
     * Attempts to reserve the resource for the provided reference owner.
     * <p>
     * Increments the reference count atomically if the resource has not been released.
     * </p>
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

    /**
     * Atomically compares the current reference count with the expected value and, if they are equal,
     * sets it to the new value.
     *
     * @param from The expected current value.
     * @param to   The new value to set.
     * @return {@code true} if the operation was successful, {@code false} otherwise.
     */
    private boolean valueCompareAndSet(int from, int to) {
        return UnsafeMemory.INSTANCE.compareAndSwapInt(this, VALUE, from, to);
    }

    /**
     * Atomically sets the reference count to the new value and returns the old value.
     *
     * @param to The new value to set.
     * @return The previous value.
     */
    private int valueGetAndSet(int to) {
        return UnsafeMemory.INSTANCE.getAndSetInt(this, VALUE, to);
    }

    /**
     * Releases the resource reserved by the provided reference owner.
     * <p>
     * Decrements the reference count atomically. If the reference count reaches zero, the resource
     * is released by executing the specified release action.
     * </p>
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
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

    /**
     * Executes the release action when the reference count reaches zero.
     *
     * @throws ClosedIllegalStateException If the resource has already been released.
     */
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
     * <p>
     * This method should be called when a reference owner knows it holds the last reference
     * to the resource. If any other references are still held, an exception is thrown.
     * </p>
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

    /**
     * Returns the current reference count.
     *
     * @return The current reference count.
     */
    @Override
    public int refCount() {
        return value;
    }

    /**
     * Returns a string representation of the current reference count.
     *
     * @return A string representing the current reference count.
     */
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Throws an exception if the resource has not been released, indicating that references are still held.
     *
     * @throws IllegalStateException If the resource has not been released.
     */
    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        if (refCount() > 0)
            throw new IllegalStateException(type.getName() + " still reserved, count=" + refCount());
    }

    /**
     * Logs a warning and releases all resources if they have not been released.
     * <p>
     * This method sets the reference count to zero and runs the release action, logging a warning
     * if the resource was discarded without being properly released.
     * </p>
     *
     * @throws IllegalStateException If the resource has been released or closed.
     */
    @Override
    public void warnAndReleaseIfNotReleased() throws IllegalStateException {
        if (valueGetAndSet(0) <= 0)
            return;

        if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING)
            Jvm.warn().on(type, "Discarded without being released");
        try {
            callOnRelease();
        } catch (ClosedIllegalStateException e) {
            // This shouldn't happen given we just tested the refCount.
            throw new AssertionError(e);
        }
    }

    /**
     * Sets whether the object is unmonitored.
     *
     * @param unmonitored {@code true} if the object is unmonitored, {@code false} otherwise.
     */
    @Override
    public void unmonitored(boolean unmonitored) {
        this.unmonitored = unmonitored;
    }

    /**
     * Returns whether the object is unmonitored.
     *
     * @return {@code true} if the object is unmonitored, {@code false} otherwise.
     */
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

    /**
     * Creates a new {@link ClosedIllegalStateException} indicating that the resource has already been released.
     *
     * @return A new {@link ClosedIllegalStateException}.
     */
    private ClosedIllegalStateException newReleasedClosedIllegalStateException() {
        return new ClosedIllegalStateException(type.getName() + " released");
    }
}
