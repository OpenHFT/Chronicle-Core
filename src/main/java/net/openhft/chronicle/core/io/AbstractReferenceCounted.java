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
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

/**
 * Abstract base class for managing reference-counted resources.
 * <p>
 * This class provides the common functionality required for implementing
 * reference counting mechanisms in resources, such as managing the number of
 * references and releasing resources when they are no longer needed.
 * It ensures proper release of resources, monitors usage, and provides
 * debugging information through tracing.
 * </p>
 */
public abstract class AbstractReferenceCounted implements ReferenceCountedTracer, ReferenceOwner, SingleThreadedChecked {

    // Constants for warning thresholds
    protected static final long WARN_NS = (long) (Jvm.getDouble("reference.warn.secs", 0.003) * 1e9);
    protected static final int WARN_COUNT = Jvm.getInteger("reference.warn.count", Integer.MAX_VALUE);

    // Set to keep track of all reference-counted resources
    static volatile Set<AbstractReferenceCounted> referenceCountedSet;

    // Monitor for the reference counting
    protected final transient MonitorReferenceCounted referenceCounted;

    // Unique identifier for each reference-counted resource
    private final int referenceId;

    // Thread that is currently using this resource
    private transient volatile Thread usedByThread;

    // Stack trace of where the resource is being used
    private transient volatile StackTrace usedByThreadHere;

    // Flag to disable single-threaded check
    private boolean singleThreadedCheckDisabled;

    /**
     * Constructs an AbstractReferenceCounted with default monitoring.
     * This constructor assumes that the resource should be monitored.
     */
    protected AbstractReferenceCounted() {
        this(true);
    }

    /**
     * Constructs an AbstractReferenceCounted.
     *
     * @param monitored If true, the resource will be monitored for reference-counted release.
     *                  If false, monitoring will be disabled.
     */
    protected AbstractReferenceCounted(boolean monitored) {
        // Determine the release mechanism based on background releaser settings
        Runnable performRelease = BG_RELEASER && canReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;

        // Assign a unique reference ID for tracking
        referenceId = IOTools.counter(getClass()).incrementAndGet();

        // Initialize the reference counting monitor
        referenceCounted = (MonitorReferenceCounted) ReferenceCountedTracer.onReleased(performRelease, this::referenceName, getClass());

        // Disable monitoring if not required
        referenceCounted.unmonitored(!monitored);

        // Add to the monitored set if monitoring is enabled
        if (monitored) {
            ReferenceCountedUtils.add(this);
        }
    }

    /**
     * Enables reference tracing for all reference-counted resources.
     * Tracing allows for detailed tracking of resource usage and helps in debugging.
     */
    public static void enableReferenceTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    /**
     * Disables reference tracing for all reference-counted resources.
     * <p>
     * <b>NOTE:</b> Resources will still be released appropriately,
     * but detailed tracing information will not be recorded.
     * </p>
     */
    public static void disableReferenceTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    /**
     * Asserts that all references have been released.
     * This method should be called during shutdown or cleanup to ensure no resources are leaking.
     */
    public static void assertReferencesReleased() {
        ReferenceCountedUtils.assertReferencesReleased();
    }

    /**
     * Marks a reference-counted resource as unmonitored.
     * <p>
     * <b>NOTE:</b> The resource will still be released appropriately,
     * but it won't give a warning if it is not released.
     * </p>
     *
     * @param counted the resource to unmonitor.
     */
    public static void unmonitor(ReferenceCounted counted) {
        ReferenceCountedUtils.unmonitor(counted);
    }

    /**
     * Returns the unique reference ID for this resource.
     *
     * @return The reference ID.
     */
    @Override
    public int referenceId() {
        return referenceId;
    }

    /**
     * Returns the stack trace for where this resource was created.
     *
     * @return The stack trace.
     */
    @Override
    public StackTrace createdHere() {
        return referenceCounted.createdHere();
    }

    /**
     * Throws an exception if the resource has already been released.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        referenceCounted.throwExceptionIfNotReleased();
    }

    /**
     * Performs the release operation in the background.
     * This method should be called if the resource can be safely released asynchronously.
     */
    protected void backgroundPerformRelease() {
        BackgroundResourceReleaser.release(this);
    }

    /**
     * Performs the release operation in the current thread.
     * This method handles resource release synchronously and catches any exceptions.
     */
    void inThreadPerformRelease() {
        try {
            performRelease();
        } catch (Exception e) {
            // Log the exception if release fails
            Jvm.warn().on(getClass(), e);
        }
    }

    /**
     * Returns whether the resource can be released in the background.
     *
     * @return {@code true} if the resource can be released in the background, {@code false} otherwise.
     */
    protected boolean canReleaseInBackground() {
        return false;
    }

    /**
     * Releases the resource. Subclasses should provide the specific implementation.
     *
     * @throws IllegalStateException if the resource cannot be released.
     */
    protected abstract void performRelease() throws IllegalStateException;

    /**
     * Increments the reference count by one, indicating that the resource is now shared among multiple owners.
     * <p>
     * When tracing is enabled, it checks the same owner doesn't try to reserve it twice (without releasing it in the meantime)
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        if ((WARN_COUNT < Integer.MAX_VALUE && referenceCounted.refCount() >= WARN_COUNT) && (referenceCounted.refCount() - WARN_COUNT) % 10 == 0)
            Jvm.warn().on(getClass(), "high reserve count for " + referenceName() +
                    " was " + referenceCounted.refCount(), new StackTrace("reserved here"));
        referenceCounted.reserve(id);
    }

    /**
     * Decrements the reference count by one. If the reference count drops to 0, the resource will be released.
     * <p>
     * When tracing is enabled, it checks the resource was an owner, and doesn't attempt to release twice.
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {
        referenceCounted.release(id);
    }

    /**
     * Releases the last reference to the resource.
     * <p>
     * When tracing is enabled, it checks the resource was an owner, and doesn't attempt to release twice.
     *
     * @param id The reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void releaseLast(ReferenceOwner id) throws ClosedIllegalStateException {
        referenceCounted.releaseLast(id);
    }

    /**
     * Attempts to reserve the resource without throwing an exception if it is already released.
     *
     * @param id The reference owner.
     * @return {@code true} if the reservation was successful, {@code false} otherwise.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     * @throws IllegalArgumentException    If the reference owner is not valid.
     */
    @Override
    public boolean tryReserve(ReferenceOwner id) throws ClosedIllegalStateException, IllegalArgumentException {
        return referenceCounted.tryReserve(id);
    }

    /**
     * Transfers a reference from one owner to another.
     *
     * @param from The current reference owner.
     * @param to   The new reference owner.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        referenceCounted.reserveTransfer(from, to);
    }

    /**
     * Retrieves the current reference count.
     *
     * @return The current reference count.
     */
    @Override
    public int refCount() {
        return referenceCounted.refCount();
    }

    /**
     * Throws an exception if the resource has been released.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        // Delegate the check to the monitored reference count
        referenceCounted.throwExceptionIfReleased();
    }

    /**
     * If the resource has not been released, logs a warning and then releases the resource.
     * This method is used to handle unexpected states where a resource might not have been properly released.
     *
     * @throws IllegalStateException If the resource hasn't been released.
     */
    @Override
    public void warnAndReleaseIfNotReleased() throws IllegalStateException {
        // Logs a warning and releases the resource if not already released
        referenceCounted.warnAndReleaseIfNotReleased();
    }

    /**
     * Sets whether the single-threaded check is disabled for this resource.
     * Disabling this check allows the resource to be accessed by multiple threads without throwing exceptions.
     *
     * @param singleThreadedCheckDisabled True to disable the single-threaded check, false to enable it.
     */
    @Override
    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        // Update the flag to disable or enable single-threaded checks
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
    }

    /**
     * Adds a listener to be notified of reference count changes.
     *
     * @param referenceChangeListener The listener to add.
     */
    @Override
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        // Add the listener to the monitored reference count
        referenceCounted.addReferenceChangeListener(referenceChangeListener);
    }

    /**
     * Removes a listener from being notified of reference count changes.
     *
     * @param referenceChangeListener The listener to remove.
     */
    @Override
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        // Remove the listener from the monitored reference count
        referenceCounted.removeReferenceChangeListener(referenceChangeListener);
    }

    /**
     * Checks if the resource is being used in a thread-safe manner.
     * If single-threaded checks are enabled, it verifies that the current thread
     * matches the thread that last used the resource.
     *
     * @param isUsed Indicates if the resource is currently in use.
     * @return True if the resource usage is thread-safe, false otherwise.
     * @throws ThreadingIllegalStateException If the resource is being used by multiple threads unsafely.
     */
    protected boolean threadSafetyCheck(boolean isUsed) throws ThreadingIllegalStateException {
        // Skip checks if single-threaded checks are globally disabled or specifically disabled for this instance
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return true;

        // Perform the thread safety check
        return threadSafetyCheck0(isUsed);
    }

    /**
     * Performs a basic thread-safety check. This method is called if initial checks pass.
     *
     * @param isUsed Indicates if the resource is currently in use.
     * @return True if the resource is thread-safe, false otherwise.
     * @throws ThreadingIllegalStateException If the resource is accessed unsafely by multiple threads.
     */
    private boolean threadSafetyCheck0(boolean isUsed) throws ThreadingIllegalStateException {
        // If the resource is not currently in use by any thread and is not marked as used, it is thread-safe
        if (usedByThread == null && !isUsed)
            return true;

        // Get the current thread executing this method
        Thread currentThread = Thread.currentThread();

        // If the current thread is the same as the last thread that used the resource, it is thread-safe
        if (usedByThread == currentThread)
            return true;

        // Further check if the resource is being used in a thread-safe manner
        return threadSafetyCheck2(currentThread);
    }

    /**
     * Performs a deeper thread-safety check, verifying if the resource can be safely reassigned to a new thread.
     *
     * @param currentThread The current thread using the resource.
     * @return True if the resource usage is thread-safe, false otherwise.
     * @throws ThreadingIllegalStateException If the resource is being accessed unsafely by multiple threads.
     */
    private boolean threadSafetyCheck2(Thread currentThread) throws ThreadingIllegalStateException {
        // If no thread is currently assigned or the previous thread is dead, reassign the current thread
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
            usedByThreadHere = new StackTrace("Used here");
            return true;

        } else {
            // If the resource is being accessed by different threads, throw an exception
            final String message = getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread;
            throw new ThreadingIllegalStateException(message, usedByThreadHere);
        }
    }

    /**
     * Resets the thread-safety check state.
     * This is typically used to indicate that the resource can be safely used again by a different thread.
     */
    public void singleThreadedCheckReset() {
        // Reset the thread tracking variables to allow new thread assignment
        usedByThread = null;
    }

    /**
     * Returns a string representation of the object including its reference name.
     * This is primarily used for debugging and logging purposes.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        // Delegate to the reference name for string representation
        return referenceName();
    }

    /**
     * Sets whether the reference counting for this resource should be unmonitored.
     * When unmonitored, the resource will not be checked for proper release.
     *
     * @param unmonitored True to disable monitoring, false to enable it.
     */
    public void referenceCountedUnmonitored(boolean unmonitored) {
        // Update the monitoring status of the reference count
        referenceCounted.unmonitored(unmonitored);
    }
}
