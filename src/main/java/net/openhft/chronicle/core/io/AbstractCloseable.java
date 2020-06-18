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
import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;
import static net.openhft.chronicle.core.io.TracingReferenceCounted.asString;

public abstract class AbstractCloseable implements CloseableTracer, ReferenceOwner {
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
    private final int referenceId;

    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace("Created Here") : null;

        Set<CloseableTracer> set = CLOSEABLE_SET;
        if (set != null)
            set.add(this);
        referenceId = IOTools.counter(getClass()).incrementAndGet();
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
        Set<CloseableTracer> traceSet = CLOSEABLE_SET;
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
                    openFiles.addSuppressed(new IllegalStateException("Not closed " + asString(key), t));
                    key.close();
                }
            }
        }
        if (openFiles.getSuppressed().length > 0)
            throw openFiles;
    }

    public static void unmonitor(Closeable closeable) {
        if (CLOSEABLE_SET != null)
            CLOSEABLE_SET.remove(closeable);
    }

    @Override
    public int referenceId() {
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
        closedHere = Jvm.isResourceTracing() ? new StackTrace("Closed here") : null;
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
            if (time >= 10_000_000)
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
            throw new IllegalStateException("Closed", closedHere);
        assert threadSafetyCheck();
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

    protected boolean threadSafetyCheck() {
        Thread currentThread = Thread.currentThread();
        if (usedByThread == null) {
            usedByThread = currentThread;
        } else if (usedByThread != currentThread) {
            if (usedByThread.isAlive()) // really expensive call.
                throw new IllegalStateException("Component which is not thread safes used by " + usedByThread + " and " + currentThread);
            usedByThread = currentThread;
        }
        return true;
    }

    public void resetUsedByThread() {
        usedByThread = Thread.currentThread();
    }

    public void clearUsedByThread() {
        usedByThread = null;
    }
}
