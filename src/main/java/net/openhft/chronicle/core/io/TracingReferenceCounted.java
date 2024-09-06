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
import net.openhft.chronicle.core.internal.CloseableUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.openhft.chronicle.core.internal.CloseableUtils.asString;

/**
 * A {@code TracingReferenceCounted} is an implementation of {@link MonitorReferenceCounted}
 * that tracks reference counting with detailed stack traces for debugging purposes.
 * <p>
 * This class is designed to help identify where resources are reserved and released
 * by tracking each operation with a stack trace. It maintains a map of references and
 * releases, providing useful information for troubleshooting resource management issues.
 * </p>
 * <p>
 * The {@code TracingReferenceCounted} also supports adding and removing
 * {@link ReferenceChangeListener}s to notify external components of reference changes.
 * </p>
 */
public final class TracingReferenceCounted implements MonitorReferenceCounted {

    // Map to keep track of current references and their stack traces
    private final Map<ReferenceOwner, StackTrace> references = Collections.synchronizedMap(new IdentityHashMap<>());

    // Map to keep track of releases and their stack traces
    private final Map<ReferenceOwner, StackTrace> releases = Collections.synchronizedMap(new IdentityHashMap<>());

    // Runnable to execute when the resource is released
    private final Runnable onRelease;

    // Unique identifier for this instance
    private final String uniqueId;

    // Type of the resource being monitored
    private final Class<?> type;

    // Stack trace of where this object was created
    private final StackTrace createdHere;

    // Manager for reference change listeners
    private final ReferenceChangeListenerManager referenceChangeListeners;

    // Stack trace of where the object was released
    private volatile StackTrace releasedHere;

    // Flag indicating whether this object is unmonitored
    private boolean unmonitored;

    /**
     * Constructs a new {@code TracingReferenceCounted} with the specified release action,
     * unique identifier, and type.
     *
     * @param onRelease The {@link Runnable} to execute when the resource is released.
     * @param uniqueId  The unique identifier for this instance.
     * @param type      The type of the resource being monitored.
     */
    TracingReferenceCounted(final Runnable onRelease, String uniqueId, Class<?> type) {
        this.onRelease = onRelease;
        this.uniqueId = uniqueId;
        this.type = type;
        createdHere = stackTrace("init", INIT);
        references.put(INIT, createdHere);
        referenceChangeListeners = new ReferenceChangeListenerManager(this);
    }

    /**
     * Returns the stack trace of where this object was created.
     *
     * @return The stack trace of creation.
     */
    @Override
    public StackTrace createdHere() {
        return createdHere;
    }

    /**
     * Adds a {@link ReferenceChangeListener} to be notified of reference changes.
     *
     * @param referenceChangeListener The {@link ReferenceChangeListener} to add.
     */
    @Override
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    /**
     * Removes a {@link ReferenceChangeListener} from being notified of reference changes.
     *
     * @param referenceChangeListener The {@link ReferenceChangeListener} to remove.
     */
    @Override
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    /**
     * Reserves a reference for the specified owner, transferring it from another owner.
     * <p>
     * If the resource is already reserved by the new owner, or if it cannot be transferred
     * from the current owner, an exception is thrown.
     * </p>
     *
     * @param id The owner of the reference to reserve.
     * @throws ClosedIllegalStateException If the resource has already been released.
     */
    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException {
        tryReserve(id, true);
    }

    /**
     * Attempts to reserve a reference for the specified owner without throwing an exception.
     * <p>
     * If the resource has already been released, the method returns {@code false}.
     * If the reference is already reserved by the owner itself, an exception is thrown.
     * </p>
     *
     * @param id  The owner of the reference to reserve.
     * @return {@code true} if the reference was successfully reserved, {@code false} otherwise.
     * @throws ClosedIllegalStateException If the resource has already been released.
     * @throws IllegalArgumentException    If the reference is already reserved by the owner.
     */
    @Override
    public boolean tryReserve(ReferenceOwner id) throws ClosedIllegalStateException, IllegalArgumentException {
        return tryReserve(id, false);
    }

    /**
     * Attempts to reserve a reference for the specified owner, optionally enforcing the reservation.
     * <p>
     * If the resource has already been released and {@code must} is {@code true},
     * an exception is thrown. Otherwise, the reservation is added if it does not already exist.
     * </p>
     *
     * @param id   The owner of the reference to reserve.
     * @param must If {@code true}, throws an exception if the resource is released.
     * @return {@code true} if the reference was successfully reserved, {@code false} otherwise.
     * @throws ClosedIllegalStateException If the resource has already been released and {@code must} is {@code true}.
     */
    private boolean tryReserve(ReferenceOwner id, boolean must) throws ClosedIllegalStateException {
        if (id == this)
            throw new AssertionError(type.getName() + " the counter cannot reserve itself");
        synchronized (references) {
            if (references.isEmpty()) {
                if (must)
                    throw new ClosedIllegalStateException(type.getName() + " cannot reserve freed resource", createdHere);
                return false;
            }
            StackTrace stackTrace = references.get(id);
            if (stackTrace == null)
                references.put(id, stackTrace("reserve", id));
            else
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(id) + " here", stackTrace);
        }
        // notify outside the synchronized block to avoid potential deadlock
        referenceChangeListeners.notifyAdded(id);
        releases.remove(id);
        return true;
    }

    /**
     * Releases a reference for the specified owner.
     * <p>
     * If the reference is not found, an exception is thrown. If all references are released,
     * the {@code onRelease} action is executed.
     * </p>
     *
     * @param id The owner of the reference to release.
     * @throws ClosedIllegalStateException If the reference has already been released or is not found.
     */
    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {

        boolean doOnRelease = false;
        synchronized (references) {
            if (references.remove(id) == null) {
                throw throwInvalidReleaseException(id);
            }
            releases.put(id, stackTrace("release", id));
            if (references.isEmpty()) {
                // Prevent this being called more than once.
                doOnRelease = true;
            }
        }
        // Needs to be called outside synchronized block above to avoid deadlock.
        referenceChangeListeners.notifyRemoved(id);
        if (doOnRelease) {
            if (releasedHere != null) {
                throw new IllegalStateException(type.getName() + " already released", releasedHere);
            }
            onRelease.run();
            releasedHere = new StackTrace(type.getName() + " released here");
        }
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException {
        synchronized (references) {
            final StackTrace stackTrace = references.get(to);
            if (stackTrace != null) {
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(to) + " here", stackTrace);
            }
            if (references.remove(from) == null) {
                throw throwInvalidReleaseException(from);
            }
            releases.put(from, stackTrace("reserveTransfer", from));
            references.put(to, stackTrace("reserveTransfer", to));
            releases.remove(to);
        }
        referenceChangeListeners.notifyTransferred(from, to);
    }

    /**
     * Throws an {@link IllegalStateException} if the specified reference owner has not been reserved or has been released.
     *
     * @param id The reference owner.
     * @return An {@link IllegalStateException} if the release is invalid.
     * @throws ClosedIllegalStateException If the resource has already been released.
     */
    private IllegalStateException throwInvalidReleaseException(ReferenceOwner id) throws ClosedIllegalStateException {
        StackTrace stackTrace = releases.get(id);
        if (stackTrace == null) {
            Throwable cause = createdHere;
            if (!references.isEmpty()) {
                StackTrace ste = references.values().iterator().next();
                cause = new IllegalStateException(type.getName() + " reserved by " + referencesAsString(), ste);
            }
            throw new IllegalStateException(type.getName() + " not reserved by " + asString(id), cause);
        } else {
            throw new ClosedIllegalStateException(type.getName() + " already released " + asString(id) + " location ", stackTrace);
        }
    }

    /**
     * Returns a list of strings representing the current references.
     *
     * @return A list of reference strings.
     */
    @NotNull
    public List<String> referencesAsString() {
        synchronized (references) {
            return references.keySet().stream()
                    .map(CloseableUtils::asString)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Releases the last reference for the specified owner, throwing an exception if any references remain.
     * <p>
     * If any references remain after the release attempt, an exception is thrown. If all references
     * are successfully released, all listeners are cleared.
     * </p>
     *
     * @param id The reference owner to release.
     * @throws IllegalStateException If references remain after the release.
     */
    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        Exception e0 = null;
        try {
            release(id);
        } catch (Exception e) {
            e0 = e;
        }
        if (references.size() > 0) {
            IllegalStateException ise = new IllegalStateException(type.getName() + " still reserved " + referencesAsString(), createdHere);
            synchronized (references) {
                references.values().forEach(ise::addSuppressed);
            }
            if (e0 != null)
                ise.addSuppressed(e0);
            throw ise;
        }
        if (e0 != null)
            Jvm.rethrow(e0);
        // If all went well, clear the listeners
        referenceChangeListeners.clear();
    }

    /**
     * Returns the current reference count.
     *
     * @return The number of references currently held.
     */
    @Override
    public int refCount() {
        return references.size();
    }

    /**
     * Returns a string representation of this {@code TracingReferenceCounted}, including its unique ID and references.
     *
     * @return A string representation of this object.
     */
    @NotNull
    public String toString() {
        return uniqueId + " - " + referencesAsString();
    }

    /**
     * Creates a stack trace for the specified operation and reference owner.
     *
     * @param oper The operation being performed.
     * @param ro   The reference owner.
     * @return A {@link StackTrace} object representing the operation.
     */
    @NotNull
    private StackTrace stackTrace(String oper, ReferenceOwner ro) {
        return new StackTrace(uniqueId + " "
                + Thread.currentThread().getName() + " "
                + oper + " "
                + asString(ro));
    }

    /**
     * Throws an exception if the resource has not been released, listing all retained references.
     * <p>
     * This method checks if there are any retained references that have not been released and throws
     * an exception with a detailed list of all retained references and their stack traces.
     * </p>
     *
     * @throws IllegalStateException If the resource has not been released.
     */
    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        synchronized (references) {
            if (references.isEmpty())
                return;
            IllegalStateException ise = new IllegalStateException(type.getName() + " retained reference closed");

            for (Map.Entry<ReferenceOwner, StackTrace> entry : references.entrySet()) {
                ReferenceOwner referenceOwner = entry.getKey();
                IllegalStateException ise2 = generateIllegalStateException(referenceOwner, entry.getValue());
                ise.addSuppressed(ise2);

                if (referenceOwner instanceof AbstractCloseable) {
                    addCloseableSuppressed(ise, (AbstractCloseable) referenceOwner);
                } else if (referenceOwner instanceof ManagedCloseable) {
                    addManagedCloseableSuppressed(ise, (ManagedCloseable) referenceOwner);
                }
            }
            if (ise.getSuppressed().length > 0)
                throw ise;
        }
    }

    /**
     * Generates an {@link IllegalStateException} for a retained reference owner.
     *
     * @param referenceOwner The retained reference owner.
     * @param reservedHere   The stack trace of where the reference was reserved.
     * @return An {@link IllegalStateException} for the retained reference.
     */
    private IllegalStateException generateIllegalStateException(ReferenceOwner referenceOwner, StackTrace reservedHere) {
        IllegalStateException ise2 = new IllegalStateException(type.getName() + " reserved by " + asString(referenceOwner), reservedHere);
        if (referenceOwner instanceof Closeable) {
            try {
                ((ManagedCloseable) referenceOwner).throwExceptionIfClosed();
            } catch (Throwable ise3) {
                ise2.addSuppressed(ise3);
            }
        } else if (referenceOwner instanceof AbstractReferenceCounted) {
            try {
                ((AbstractReferenceCounted) referenceOwner).throwExceptionIfReleased();
            } catch (Throwable ise3) {
                ise2.addSuppressed(ise3);
            }
        }
        return ise2;
    }

    /**
     * Adds suppressed exceptions for a closeable object.
     *
     * @param ise The exception to add suppressed exceptions to.
     * @param ac  The {@link AbstractCloseable} object to check for closure exceptions.
     */
    private void addCloseableSuppressed(Exception ise, AbstractCloseable ac) {
        try {
            ac.throwExceptionIfClosed();
        } catch (Throwable e) {
            ise.addSuppressed(e);
        }
    }

    /**
     * Adds suppressed exceptions for a managed closeable object.
     *
     * @param ise The exception to add suppressed exceptions to.
     * @param mc  The {@link ManagedCloseable} object to check for closure exceptions.
     */
    private void addManagedCloseableSuppressed(Exception ise, ManagedCloseable mc) {
        try {
            mc.throwExceptionIfClosed();
        } catch (Throwable t) {
            ise.addSuppressed(new ClosedIllegalStateException(type.getName() + " closed " + asString(mc), t));
        }
    }

    /**
     * Throws an exception if the resource has been released.
     *
     * @throws ClosedIllegalStateException If the resource has been released.
     */
    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException(type.getName() + " released", releasedHere);
    }

    /**
     * Logs a warning and releases all resources if they have not been released.
     * <p>
     * If the object is unmonitored or the discard warning is disabled, this method clears all references
     * and runs the release action.
     * </p>
     */
    @Override
    public void warnAndReleaseIfNotReleased() {
        boolean runOnRelease = false;
        synchronized (references) {
            if (refCount() > 0) {
                if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING) {
                    Jvm.warn().on(type, "Discarded without being released by " + referencesAsString(), createdHere);
                }
                references.clear();
                runOnRelease = true;
            }
        }
        // Run outside the synchronized block to avoid risk of deadlock
        if (runOnRelease) {
            onRelease.run();
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
}
