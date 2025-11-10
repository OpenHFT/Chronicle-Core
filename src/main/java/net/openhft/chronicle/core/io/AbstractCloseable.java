//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.internal.ClassUtil;
import net.openhft.chronicle.core.internal.CloseableUtils;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

/**
 * Base class for Chronicle components that need deterministic shutdown.
 * It implements {@link ManagedCloseable} and performs thread-safety checks.
 * Concrete subclasses implement their clean-up in {@link #performClose()}.
 * <p>
 * Resource tracing and optional background closing are provided for debugging
 * and performance tuning. See {@link Closeable#closeQuietly(Object)} for a
 * helper that ignores exceptions from {@link #performClose()}.
 */
@SuppressWarnings({"java:S2065", "java:S1186", "java:S3077"}) // suppress false positive for serialization
public abstract class AbstractCloseable implements ReferenceOwner, ManagedCloseable, SingleThreadedChecked, Monitorable {

    /**
     * Flag indicating whether discard warning is disabled.
     */
    protected static final boolean DISABLE_DISCARD_WARNING = Jvm.getBoolean("disable.discard.warning", true);

    /**
     * The number of nanoseconds to warn before closing a resource.
     */
    protected static final long WARN_NS = (long) (Jvm.getDouble("closeable.warn.secs", 0.02) * 1e9);

    private static final long CLOSED_OFFSET;
    private static final int STATE_NOT_CLOSED = 0;
    private static final int STATE_CLOSING = ~0;
    private static final int STATE_CLOSED = 1;

    static {
        if (Jvm.isResourceTracing())
            enableCloseableTracing();
        CLOSED_OFFSET = UnsafeMemory.unsafeObjectFieldOffset(ClassUtil.getField0(AbstractCloseable.class, "closed", true, false));
    }

    private final transient StackTrace createdHere;
    @SuppressWarnings("unused")
    @UsedViaReflection
    private final transient Finalizer finalizer = DISABLE_DISCARD_WARNING ? null : new Finalizer();
    protected transient volatile StackTrace closedHere;
    private transient volatile int closed = 0;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private transient boolean singleThreadedCheckDisabled;
    private int referenceId;

    /**
     * Constructs a new instance. Registers the instance
     * for resource tracing and monitoring if enabled.
     */
    @SuppressWarnings("this-escape")
    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + " created here") : null;
        CloseableUtils.add(this);
    }

    /**
     * Enables tracing of closeable resources. When enabled, instances of resources
     * that are not yet closed are stored in a set for tracking and debugging purposes.
     */
    public static void enableCloseableTracing() {
        CloseableUtils.enableCloseableTracing();
    }

    /**
     * Disables tracing of closeable resources.
     * Tracked resources will no longer be stored in the set.
     * This method should be called to disable tracing when no longer needed.
     */
    public static void disableCloseableTracing() {
        CloseableUtils.disableCloseableTracing();
    }

    /**
     * Performs garbage collection and waits for closeables to close.
     * This method is useful for ensuring that closeable resources are properly closed before continuing.
     * It performs the following steps:
     * 1. Performs cleanup on the current thread.
     * 2. Performs garbage collection.
     * 3. Waits for closeables to close with a specified timeout.
     * 4. Checks if the finalizer thread has finalized any objects.
     * If not, it throws an AssertionError.
     * <p>
     * NOTE: This is slower than waitForCloseablesToClose and it can clean up resources that should have be released deterministically.
     *
     * @throws AssertionError If the finalizer does not complete within the specified timeout.
     */
    public static void gcAndWaitForCloseablesToClose() {
        CloseableUtils.gcAndWaitForCloseablesToClose();
    }

    /**
     * Waits for closeable resources to be closed within a specified timeout period.
     *
     * @param millis the maximum time in milliseconds to wait for closeables to close.
     * @return {@code true} if all closeable resources were closed within the specified
     * timeout, {@code false} otherwise.
     */
    public static boolean waitForCloseablesToClose(long millis) {
        return CloseableUtils.waitForCloseablesToClose(millis);
    }

    /**
     * Asserts that all closeable resources are closed. If any closeable resource is not
     * closed, this method throws an {@link AssertionError}.
     *
     * @throws AssertionError if any closeable resource is not closed.
     */
    public static void assertCloseablesClosed() {
        CloseableUtils.assertCloseablesClosed();
    }

    /**
     * Returns the unique reference ID of this closeable resource.
     *
     * @return the unique reference ID.
     */
    @Override
    public int referenceId() {
        if (referenceId == 0)
            referenceId = IOTools.counter(getClass()).incrementAndGet();
        return referenceId;
    }

    /**
     * Returns the stack trace at the point this closeable resource was created.
     *
     * @return the stack trace at the point of creation.
     */
    @Override
    public StackTrace createdHere() {
        return createdHere;
    }

    /**
     * Idempotent close entry point used by try-with-resources. Delegates to
     * {@link #performClose()}.
     */
    @Override
    public final void close() {
        assertCloseable();
        if (!UnsafeMemory.INSTANCE.compareAndSwapInt(this, CLOSED_OFFSET, STATE_NOT_CLOSED, STATE_CLOSING)) {
            if (shouldWaitForClosed() && isInUserThread()) {
                waitForClosed();
            }
            return;
        }
        closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + " closed here") : null;
        if (BG_RELEASER && shouldPerformCloseInBackground()) {
            BackgroundResourceReleaser.release(this);
            return;
        }

        long start = System.nanoTime();
        callPerformClose();
        long time = System.nanoTime() - start;
        if (time >= WARN_NS &&
                !BackgroundResourceReleaser.isOnBackgroundResourceReleaserThread())
            Jvm.perf().on(getClass(), "Took " + time / 1000_000 + " ms to performClose");
    }

    /**
     * Asserts that this resource can be closed, throwing an {@link IllegalStateException } if it cannot.
     * <p>
     * This can be useful for resources that have outstanding guarantees such as acquired update locks or open
     * contexts.
     * <p>
     * Electing to throw an Exception might cause memory-leaks as there is no guarantee that the
     * close methods will ever be invoked again in the general case.
     *
     * @throws IllegalStateException if the resource cannot be closed.
     */
    protected void assertCloseable() {
        // Do nothing by default, allowing close() to complete unconditionally
    }

    /**
     * Checks if the current thread is a user thread.
     *
     * @return {@code true} if the current thread is a user thread; {@code false} otherwise.
     */
    protected boolean isInUserThread() {
        return Thread.currentThread().getName().indexOf('~') < 0;
    }

    /**
     * Waits for the resource to transition to the closed state.
     * This is useful for scenarios where close operations might be asynchronous and
     * the caller needs to wait until the resource is effectively closed.
     */
    protected void waitForClosed() {
        boolean interrupted = false;
        try {
            long start = System.currentTimeMillis();
            while (closed != STATE_CLOSED) {
                if (System.currentTimeMillis() > start + 2_500) {
                    Jvm.warn().on(getClass(), "Aborting close()ing object " + referenceId +
                            " after " + (System.currentTimeMillis() - start) / 1e3 + " secs", new StackTrace("waiting here", closedHere));
                    break;
                }
                try {
                    //noinspection BusyWait
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Throws an exception if the resource is closed.
     * This method is used to ensure that the resource is open before attempting an operation
     * that requires it to be open.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the thread safety check fails.
     */
    @Override
    public void throwExceptionIfClosed() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        if (isClosed())
            throwClosed();
        threadSafetyCheck(true);
    }

    private void throwClosed() throws ClosedIllegalStateException {
        throw new ClosedIllegalStateException(getClass().getName() + " closed for " + Thread.currentThread().getName(), closedHere);
    }

    /**
     * Throws an exception if the resource is closed during a setter operation.
     * This method is used to ensure that the resource is open before attempting to modify its state.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the thread safety check fails.
     */
    public void throwExceptionIfClosedInSetter() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        if (isClosed())
            throwClosed();
        threadSafetyCheck(false);
    }

    /**
     * If the resource is still open a warning is logged and it is closed using
     * {@link Closeable#closeQuietly(Object)}. Intended for use from finalisers.
     */
    @Override
    public void warnAndCloseIfNotClosed() {
        if (!isClosing()) {
            if (Jvm.isResourceTracing() && !DISABLE_DISCARD_WARNING) {
                ExceptionHandler warn = Jvm.getBoolean("warnAndCloseIfNotClosed") ? Jvm.warn() : Slf4jExceptionHandler.WARN;
                warn.on(getClass(), "Discarded without closing", new IllegalStateException(createdHere));
            }
            close();
        }
    }

    /**
     * Implement the clean-up logic. {@code close()} invokes this method exactly
     * once via {@link #callPerformClose()}.
     */
    protected abstract void performClose();

    /**
     * Invoked by {@link #close()}. Ensures {@link #performClose()} runs once and
     * swallows any exception by logging it.
     */
    void callPerformClose() {
        try {
            performClose();
        } catch (Throwable t) { // NOSONAR
            Jvm.warn().on(getClass(), "Error occurred in close method", t);
        } finally {
            closed = STATE_CLOSED;
        }
    }

    /**
     * Checks if the resource is in the process of being closed.
     *
     * @return {@code true} if the resource is either closed or in the process of being closed;
     * {@code false} otherwise.
     */
    @Override
    public boolean isClosing() {
        return closed != STATE_NOT_CLOSED;
    }

    /**
     * Checks if the resource is closed.
     *
     * @return {@code true} if the resource is closed; {@code false} otherwise.
     */
    @Override
    public boolean isClosed() {
        return closed == STATE_CLOSED;
    }

    /**
     * Determines if this component should be closed asynchronously in the background.
     * Subclasses may override this method to change the default behavior, which is to close
     * synchronously.
     *
     * @return {@code true} if this component should be closed in the background; {@code false} otherwise.
     */
    protected boolean shouldPerformCloseInBackground() {
        return false;
    }

    /**
     * Determines if this component should wait for the close operation to complete before
     * returning from the {@code close()} method.
     * Subclasses may override this method to change the default behavior.
     *
     * @return {@code true} if this component should wait for the close to complete; {@code false} otherwise.
     */
    protected boolean shouldWaitForClosed() {
        return false;
    }

    /**
     * Performs a thread safety check on the component.
     * If the component is not thread-safe and is accessed by multiple threads,
     * an {@link ThreadingIllegalStateException} is thrown.
     * This method is intended to be called before operations that require thread safety.
     *
     * @param isUsed flag indicating whether the component is being used in the current operation.
     * @throws ThreadingIllegalStateException If the component is not thread-safe and accessed by multiple threads.
     */
    protected void threadSafetyCheck(boolean isUsed) throws ThreadingIllegalStateException {
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return;
        threadSafetyCheck0(isUsed);
    }

    private void threadSafetyCheck0(boolean isUsed) throws ThreadingIllegalStateException {
        if (usedByThread == null && !isUsed)
            return;

        Thread currentThread = Thread.currentThread();
        if (usedByThread == null) {
            usedByThread = currentThread;
            if (Jvm.isResourceTracing())
                usedByThreadHere = new StackTrace(getClass().getName() + " used here");
        } else if (usedByThread != currentThread) {
            if (usedByThread.isAlive()) // really expensive call.
                throw new ThreadingIllegalStateException(getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread, usedByThreadHere);
            usedByThread = currentThread;
        }
    }

    /**
     * Resets the state of the thread safety check.
     * After calling this method, the component's thread safety check state will be cleared,
     * and it will no longer remember which thread it was last accessed by.
     */
    public void singleThreadedCheckReset() {
        usedByThread = null;
        usedByThreadHere = null;
    }

    /**
     * Returns a string representation of this component, typically used for debugging purposes.
     * By default, this returns the reference name of the component.
     *
     * @return the string representation of this component.
     */
    @Override
    public String toString() {
        return referenceName();
    }

    /**
     * Determines if the single-threaded safety check is disabled for this component.
     * If disabled, the component will not perform checks to ensure that it is being accessed
     * by a single thread.
     *
     * @return {@code true} if single-threaded safety check is disabled; {@code false} otherwise.
     */
    protected boolean singleThreadedCheckDisabled() {
        return singleThreadedCheckDisabled;
    }

    /**
     * Disables or enables the single-threaded safety check.
     * If disabled, the component will not perform checks to ensure that it is being accessed
     * by a single thread.
     *
     * @param singleThreadedCheckDisabled {@code true} to disable single-threaded safety check; {@code false} to enable it.
     */
    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
        if (singleThreadedCheckDisabled) {
            singleThreadedCheckReset();
        }
    }

    @Override
    public void unmonitor() {
        CloseableUtils.unmonitor(this);
    }

    /**
     * The Finalizer inner class is used to ensure that resources are properly closed
     * when the garbage collector decides to reclaim the memory for the enclosing AbstractCloseable instance.
     */
    @SuppressWarnings("RedundantSuppression")
    class Finalizer {
        /**
         * Called by the garbage collector when the enclosing AbstractCloseable instance is
         * being finalized. This method ensures that if the enclosing instance is not closed,
         * a warning is issued and the close method is called.
         *
         * @throws Throwable if an error occurs during finalization.
         */
        @SuppressWarnings({"deprecation", "removal", "java:S1113"})
        @Override
        protected void finalize() throws Throwable {
            warnAndCloseIfNotClosed();
            super.finalize();
        }
    }
}
