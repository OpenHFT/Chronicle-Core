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
 */
public abstract class AbstractReferenceCounted implements ReferenceCountedTracer, ReferenceOwner, SingleThreadedChecked {
    // Constants
    protected static final long WARN_NS = (long) (Jvm.getDouble("reference.warn.secs", 0.003) * 1e9);
    protected static final int WARN_COUNT = Jvm.getInteger("reference.warn.count", Integer.MAX_VALUE);

    // Fields
    static volatile Set<AbstractReferenceCounted> referenceCountedSet;
    protected final transient MonitorReferenceCounted referenceCounted;
    private final int referenceId;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private boolean singleThreadedCheckDisabled;

    /**
     * Constructs an AbstractReferenceCounted with default monitoring.
     */
    protected AbstractReferenceCounted() {
        this(true);
    }

    /**
     * Constructs an AbstractReferenceCounted.
     *
     * @param monitored If true, the resource will be monitored for reference counted release.
     */
    protected AbstractReferenceCounted(boolean monitored) {
        Runnable performRelease = BG_RELEASER && canReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;
        referenceId = IOTools.counter(getClass()).incrementAndGet();
        referenceCounted = (MonitorReferenceCounted) ReferenceCountedTracer.onReleased(performRelease, this::referenceName, getClass());
        referenceCounted.unmonitored(!monitored);
        if (monitored) {
            ReferenceCountedUtils.add(this);
        }
    }

    /**
     * Enables reference tracing.
     */
    public static void enableReferenceTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    /**
     * Disables reference tracing.
     * <p>
     * <b>NOTE:</b> The resources will still be released appropriately, however if detailed tracing won't be recorded
     */
    public static void disableReferenceTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    /**
     * Asserts that all references have been released.
     */
    public static void assertReferencesReleased() {
        ReferenceCountedUtils.assertReferencesReleased();
    }

    /**
     * Marks a reference-counted resource as unmonitored.
     * <p>
     * <b>NOTE:</b> The resource will still be released appropriately, however it won't give a warning if it is not.
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
     */
    protected void backgroundPerformRelease() {
        BackgroundResourceReleaser.release(this);
    }

    void inThreadPerformRelease() {
        try {
            performRelease();
        } catch (Exception e) {
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
        referenceCounted.throwExceptionIfReleased();
    }

    /**
     * If not released, logs a warning and releases the resource.
     *
     * @throws IllegalStateException If the resource hadn't been released.
     */
    @Override
    public void warnAndReleaseIfNotReleased() throws IllegalStateException {
        referenceCounted.warnAndReleaseIfNotReleased();
    }

    @Override
    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
    }

    @Override
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceCounted.addReferenceChangeListener(referenceChangeListener);
    }

    @Override
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceCounted.removeReferenceChangeListener(referenceChangeListener);
    }

    protected boolean threadSafetyCheck(boolean isUsed) throws ThreadingIllegalStateException {
        // most common check, and sometimes the only check
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return true;
        return threadSafetyCheck0(isUsed);
    }

    private boolean threadSafetyCheck0(boolean isUsed) throws ThreadingIllegalStateException {
        // not so common but very cheap
        if (usedByThread == null && !isUsed)
            return true;

        Thread currentThread = Thread.currentThread();
        // when checking safety, this is the common case
        if (usedByThread == currentThread)
            return true;

        // very rare, only one first use after reset or a failure
        return threadSafetyCheck2(currentThread);
    }

    private boolean threadSafetyCheck2(Thread currentThread) throws ThreadingIllegalStateException {
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
            usedByThreadHere = new StackTrace("Used here");
            return true;

        } else {
            final String message = getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread;
            throw new ThreadingIllegalStateException(message, usedByThreadHere);
        }
    }

    /**
     * Resets the thread-safety check state. This is typically used to indicate
     * that the resource can be used again by a different thread.
     */
    public void singleThreadedCheckReset() {
        usedByThread = null;
    }

    /**
     * Returns a string representation of the object including its reference name.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        return referenceName();
    }

    public void referenceCountedUnmonitored(boolean unmonitored) {
        referenceCounted.unmonitored(unmonitored);
    }
}
