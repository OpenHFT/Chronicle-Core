/*
 * Copyright 2016-2020 chronicle.software
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
import net.openhft.chronicle.core.internal.CloseableUtils;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

/**
 * An abstract class representing a closeable resource.
 */
public abstract class AbstractCloseable implements ReferenceOwner, ManagedCloseable, SingleThreadedChecked {
    @Deprecated(/* remove in x.25 */)
    protected static final boolean DISABLE_THREAD_SAFETY = DISABLE_SINGLE_THREADED_CHECK;

    /**
     * Flag indicating whether discard warning is disabled.
     */
    protected static final boolean DISABLE_DISCARD_WARNING = Jvm.getBoolean("disable.discard.warning", false);
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(/* remove in x.25 */)
    protected static final boolean STRICT_DISCARD_WARNING;

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
        CLOSED_OFFSET = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(AbstractCloseable.class, "closed"));
        if (Jvm.getProperty("strict.discard.warning") != null) {
            Jvm.warn().on(AbstractCloseable.class, "strict.discard.warning is deprecated and has no effect, it will be removed in x.25");
        }
        STRICT_DISCARD_WARNING = Jvm.getBoolean("strict.discard.warning", false);
    }

    private final transient StackTrace createdHere;
    protected transient volatile StackTrace closedHere;
    private transient volatile int closed = 0;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private transient boolean singleThreadedCheckDisabled;

    @UsedViaReflection
    private final transient Finalizer finalizer = DISABLE_DISCARD_WARNING ? null : new Finalizer();

    private int referenceId;

    /**
     * Constructs a new AbstractCloseable instance.
     */
    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + " created here") : null;
        CloseableUtils.add(this);
    }

    /**
     * Enables tracing of closeable resources.
     * Tracked resources will be stored in a set.
     * This method should be called to enable tracing before using closeable resources.
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
     *
     * @throws AssertionError If the finalizer does not complete within the specified timeout.
     */
    public static void gcAndWaitForCloseablesToClose() {
        CloseableUtils.gcAndWaitForCloseablesToClose();
    }

    public static boolean waitForCloseablesToClose(long millis) {
        return CloseableUtils.waitForCloseablesToClose(millis);
    }

    public static void assertCloseablesClosed() {
        CloseableUtils.assertCloseablesClosed();
    }

    public static void unmonitor(Closeable closeable) {
        CloseableUtils.unmonitor(closeable);
    }

    @Override
    public int referenceId() {
        if (referenceId == 0)
            referenceId = IOTools.counter(getClass()).incrementAndGet();
        return referenceId;
    }

    @Override
    public StackTrace createdHere() {
        return createdHere;
    }

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

    protected boolean isInUserThread() {
        return Thread.currentThread().getName().indexOf('~') < 0;
    }

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
     * Called when a resources needs to be open to use it.
     *
     * @throws ClosedIllegalStateException if closed
     * @throws IllegalStateException       if the thread safety check fails
     */
    @Override
    public void throwExceptionIfClosed() throws ClosedIllegalStateException, IllegalStateException {
        if (isClosed())
            throwClosed();
        threadSafetyCheck(true);
    }

    private void throwClosed() {
        throw new ClosedIllegalStateException(getClass().getName() + " closed for " + Thread.currentThread().getName(), closedHere);
    }

    public void throwExceptionIfClosedInSetter() throws IllegalStateException {
        if (isClosed())
            throwClosed();
        threadSafetyCheck(false);
    }

    /**
     * Called from finalise() implementations.
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
     * Call close() to ensure this is called exactly once.
     */
    protected abstract void performClose() throws IllegalStateException;

    void callPerformClose() {
        try {
            performClose();
        } catch (Throwable t) {
            Jvm.warn().on(getClass(), "Error occurred in close method", t);
        } finally {
            closed = STATE_CLOSED;
        }
    }

    @Override
    public boolean isClosing() {
        return closed != STATE_NOT_CLOSED;
    }

    @Override
    public boolean isClosed() {
        return closed == STATE_CLOSED;
    }

    /**
     * @return whether this component should be closed in the background or not
     */
    protected boolean shouldPerformCloseInBackground() {
        return false;
    }

    /**
     * @return whether this component should be wait for closed to complete
     */
    protected boolean shouldWaitForClosed() {
        return false;
    }

    protected void threadSafetyCheck(boolean isUsed) throws IllegalStateException {
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return;
        threadSafetyCheck0(isUsed);
    }

    private void threadSafetyCheck0(boolean isUsed) {
        if (usedByThread == null && !isUsed)
            return;

        Thread currentThread = Thread.currentThread();
        if (usedByThread == null) {
            usedByThread = currentThread;
            if (Jvm.isResourceTracing())
                usedByThreadHere = new StackTrace(getClass().getName() + " used here");
        } else if (usedByThread != currentThread) {
            if (usedByThread.isAlive()) // really expensive call.
                throw new IllegalStateException(getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread, usedByThreadHere);
            usedByThread = currentThread;
        }
    }

    /**
     * @deprecated Use @{code singleThreadedCheckReset()} instead
     */
    @Deprecated(/* to be removed in x.25 */)
    public void resetUsedByThread() {
        singleThreadedCheckReset();
    }

    /**
     * @deprecated Use @{code singleThreadedCheckReset()} instead
     */
    @Deprecated(/* to be removed in x.25 */)
    public void clearUsedByThread() {
        singleThreadedCheckReset();
    }

    public void singleThreadedCheckReset() {
        usedByThread = null;
        usedByThreadHere = null;
    }

    @Override
    public String toString() {
        return referenceName();
    }

    /**
     * @deprecated Use @{code singleThreadedCheckDisabled()} instead
     */
    @Deprecated(/* to be removed in x.25 */)
    public boolean disableThreadSafetyCheck() {
        return singleThreadedCheckDisabled;
    }

    protected boolean singleThreadedCheckDisabled() {
        return singleThreadedCheckDisabled;
    }

    /**
     * @deprecated Use @{code disableThreadSafetyCheck(boolean)} instead
     */
    @Deprecated(/* to be removed in x.25 */)
    public AbstractCloseable disableThreadSafetyCheck(boolean disableThreadSafetyCheck) {
        singleThreadedCheckDisabled(disableThreadSafetyCheck);
        return this;
    }

    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
        if (singleThreadedCheckDisabled)
            singleThreadedCheckReset();
    }

    class Finalizer {
        @Override
        protected void finalize()
                throws Throwable {
            warnAndCloseIfNotClosed();
            super.finalize();
        }
    }
}
