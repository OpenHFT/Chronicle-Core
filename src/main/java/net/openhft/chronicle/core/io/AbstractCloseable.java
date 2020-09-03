/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;

import java.util.Collections;
import java.util.Set;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BACKGROUND_RESOURCE_RELEASER;
import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;
import static net.openhft.chronicle.core.io.TracingReferenceCounted.asString;

public abstract class AbstractCloseable implements CloseableTracer, ReferenceOwner {
    protected static final boolean CHECK_THREAD_SAFETY = Jvm.getBoolean("check.thread.safety", false);

    private static final long CLOSED_OFFSET;
    static volatile Set<CloseableTracer> CLOSEABLE_SET;

    static {
        enableCloseableTracing();
        CLOSED_OFFSET = UNSAFE.objectFieldOffset(Jvm.getField(AbstractCloseable.class, "closed"));
    }

    private transient volatile int closed = 0;
    private transient volatile StackTrace createdHere;
    private transient volatile StackTrace closedHere;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private int referenceId;

    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace(getClass() + " - Created Here") : null;

        Set<CloseableTracer> set = CLOSEABLE_SET;
        if (set != null)
            set.add(this);
    }

    public static void enableCloseableTracing() {
        CLOSEABLE_SET =
                Collections.newSetFromMap(
                        new WeakIdentityHashMap<>());
    }

    public static void disableCloseableTracing() {
        CLOSEABLE_SET = null;
    }

    public static void assertCloseablesClosed() {
        final Set<CloseableTracer> traceSet = CLOSEABLE_SET;
        if (traceSet == null) {
            Jvm.warn().on(AbstractCloseable.class, "closable tracing disabled");
            return;
        }

        BackgroundResourceReleaser.releasePendingResources();

        AssertionError openFiles = new AssertionError("Closeables still open");

        synchronized (traceSet) {
            for (CloseableTracer key : traceSet) {
                if (key != null && !key.isClosed()) {
                    Throwable t;
                    try {
                        if (key instanceof ReferenceCountedTracer) {
                            ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                        }
                        t = key.createdHere();
                    } catch (IllegalStateException e) {
                        t = e;
                    }
                    IllegalStateException exception = new IllegalStateException("Not closed " + asString(key), t);
                    Thread.yield();
                    if (key.isClosed()) {
                        System.out.println(exception.getMessage() + " is now closed...");
                        continue;
                    }
                    exception.printStackTrace();
                    openFiles.addSuppressed(exception);
                    key.close();
                }
            }
        }

        if (openFiles.getSuppressed().length > 0) {
            throw openFiles;
        }
    }

    public static void unmonitor(Closeable closeable) {
        if (CLOSEABLE_SET != null)
            CLOSEABLE_SET.remove(closeable);
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

    /**
     * Close a resource so it cannot be used again.
     */
    @Override
    public final void close() {
        if (UNSAFE.getAndSetInt(this, CLOSED_OFFSET, 1) != 0) {
            return;
        }
        closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass() + " - Closed here") : null;
        if (BG_RELEASER && performCloseInBackground()) {
            BackgroundResourceReleaser.release(this);
        } else {
            long start = System.nanoTime();
            try {
                performClose();
            } catch (Throwable e) {
                Jvm.debug().on(getClass(), "Exception thrown on performClose", e);
            }
            long time = System.nanoTime() - start;
            if (time >= 20_000_000 &&
                    !Thread.currentThread().getName().equals(BACKGROUND_RESOURCE_RELEASER))
                Jvm.warn().on(getClass(), "Took " + time / 1000_000 + " ms to performClose");
        }
    }

    /**
     * Called when a resources needs to be open to use it.
     *
     * @throws IllegalStateException if closed
     */
    public void throwExceptionIfClosed() throws IllegalStateException {
        if (closed != 0)
            throw new ClosedIllegalStateException("Closed", closedHere);
        if (CHECK_THREAD_SAFETY)
            threadSafetyCheck(true);
    }

    public void throwExceptionIfClosedInSetter() throws IllegalStateException {
        if (closed != 0)
            throw new ClosedIllegalStateException("Closed", closedHere);
        // only check it if this has been used.
        if (CHECK_THREAD_SAFETY)
            threadSafetyCheck(false);
    }

    /**
     * Called from finalise() implementations.
     */
    protected void warnAndCloseIfNotClosed() {
        if (!isClosed()) {
            if (Jvm.isResourceTracing()) {
                ExceptionHandler warn = Jvm.getBoolean("warnAndCloseIfNotClosed") ? Jvm.warn() : Slf4jExceptionHandler.WARN;
                warn.on(getClass(), "Discarded without closing", new IllegalStateException(createdHere));
            }
            close();
        }
    }

    /**
     * Call close() to ensure this is called exactly once.
     */
    protected abstract void performClose();

    @Override
    public boolean isClosed() {
        return closed != 0;
    }

    protected boolean performCloseInBackground() {
        return false;
    }

    protected boolean threadSafetyCheck(boolean isUsed) {
        if (!CHECK_THREAD_SAFETY)
            return true;
        if (usedByThread == null && !isUsed)
            return true;

        Thread currentThread = Thread.currentThread();
        if (usedByThread == null) {
            usedByThread = currentThread;
            if (Jvm.isResourceTracing())
                usedByThreadHere = new StackTrace("Used here");
        } else if (usedByThread != currentThread) {
            if (usedByThread.isAlive()) // really expensive call.
                throw new IllegalStateException("Component which is not thread safes used by " + usedByThread + " and " + currentThread, usedByThreadHere);
            usedByThread = currentThread;
        }
        return true;
    }

    public void resetUsedByThread() {
        usedByThread = Thread.currentThread();
        usedByThreadHere = new StackTrace("Used here");
    }

    public void clearUsedByThread() {
        usedByThread = null;
        usedByThreadHere = null;
    }

    @Override
    public String toString() {
        return referenceName();
    }
}
