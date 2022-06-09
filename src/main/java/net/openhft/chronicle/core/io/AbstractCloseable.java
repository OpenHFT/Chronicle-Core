/*
 * Copyright 2016-2020 chronicle.software
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
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.CleaningThreadLocal;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;
import static net.openhft.chronicle.core.io.TracingReferenceCounted.asString;

public abstract class AbstractCloseable implements ReferenceOwner, ManagedCloseable, SingleThreadedChecked {
    protected static final boolean DISABLE_SINGLE_THREADED_CHECK =
            Jvm.getBoolean("disable.single.threaded.check",
                    Jvm.getBoolean("disable.thread.safety", false));
    protected static final boolean DISABLE_THREAD_SAFETY = DISABLE_SINGLE_THREADED_CHECK;
    protected static final boolean DISABLE_DISCARD_WARNING = Jvm.getBoolean("disable.discard.warning", false);
    protected static final boolean STRICT_DISCARD_WARNING = Jvm.getBoolean("strict.discard.warning", false);

    protected static final long WARN_NS = (long) (Jvm.getDouble("closeable.warn.secs", 0.02) * 1e9);

    private static final long CLOSED_OFFSET;
    private static final int STATE_NOT_CLOSED = 0;
    private static final int STATE_CLOSING = ~0;
    private static final int STATE_CLOSED = 1;
    static volatile Set<Closeable> closeableSet;

    static {
        if (Jvm.isResourceTracing())
            enableCloseableTracing();
        CLOSED_OFFSET = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(AbstractCloseable.class, "closed"));
    }

    private final transient StackTrace createdHere;
    protected transient volatile StackTrace closedHere;
    private transient volatile int closed = 0;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private transient boolean singleThreadedCheckDisabled;

    @UsedViaReflection
    private transient Finalizer finalizer = DISABLE_DISCARD_WARNING ? null : new Finalizer();

    private int referenceId;

    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + " created here") : null;

        final Set<Closeable> set = closeableSet;
        if (set != null)
            synchronized (set) {
                set.add(this);
            }
    }

    public static void enableCloseableTracing() {
        closeableSet =
                Collections.newSetFromMap(
                        new WeakIdentityHashMap<>());
    }

    public static void disableCloseableTracing() {
        closeableSet = null;
    }

    public static void gcAndWaitForCloseablesToClose() {
        CleaningThread.performCleanup(Thread.currentThread());

        // find any discarded resources.
        final BlockingQueue<String> q = new LinkedBlockingQueue<>();
        new Object() {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                q.add("finalized");
            }
        };
        System.gc();
        AbstractCloseable.waitForCloseablesToClose(1000);
        try {
            if (q.poll(5, TimeUnit.SECONDS) == null)
                throw new AssertionError("Timed out waiting for the Finalizer");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    public static boolean waitForCloseablesToClose(long millis) {
        final Set<Closeable> traceSet = closeableSet;
        if (traceSet == null) {
            return true;
        }

        long end = System.currentTimeMillis() + millis;

        BackgroundResourceReleaser.releasePendingResources();

        toWait:
        do {
            CleaningThreadLocal.cleanupNonCleaningThreads();
            synchronized (traceSet) {
                for (Closeable key : traceSet) {
                    try {
                        // too late to be checking thread safety.
                        if (key instanceof AbstractCloseable) {
                            ((AbstractCloseable) key).singleThreadedCheckDisabled(true);
                        }
                        if (key instanceof ReferenceCountedTracer) {
                            ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                        }
                    } catch (IllegalStateException e) {
                        Jvm.pause(1);
                        continue toWait;
                    }
                }
            }
            Jvm.pause(1);
            return true;
        } while (System.currentTimeMillis() < end);
        return false;
    }

    public static void assertCloseablesClosed() {
        final Set<Closeable> traceSet = closeableSet;
        if (traceSet == null) {
            Jvm.warn().on(AbstractCloseable.class, "closable tracing disabled");
            return;
        }

        BackgroundResourceReleaser.releasePendingResources();

        AssertionError openFiles = new AssertionError("Closeables still open");

        synchronized (traceSet) {
            traceSet.removeIf(o -> o == null || o.isClosing());
            Set<Closeable> nested = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Closeable key : traceSet) {
                addNested(nested, key, 1);
            }
            Set<Closeable> traceSet2 = Collections.newSetFromMap(new IdentityHashMap<>());
            traceSet2.addAll(traceSet);
            traceSet2.removeAll(nested);

            for (Closeable key : traceSet2) {
                Throwable t = null;
                try {
                    if (key instanceof ReferenceCountedTracer) {
                        ((ReferenceCountedTracer) key).throwExceptionIfNotReleased();
                    }
                    if (key instanceof ManagedCloseable) {
                        t = ((ManagedCloseable) key).createdHere();
                    }
                } catch (IllegalStateException e) {
                    t = e;
                }
                IllegalStateException exception = new IllegalStateException("Not closed " + asString(key), t);
                Thread.yield();
                if (key.isClosed()) {
                    continue;
                }
                exception.printStackTrace();
                openFiles.addSuppressed(exception);
                key.close();
            }
        }

        if (openFiles.getSuppressed().length > 0) {
            throw openFiles;
        }
    }

    private static void addNested(Set<Closeable> nested, Closeable key, int depth) {
        if (key.isClosing())
            return;
        Set<Field> fields = new HashSet<>();
        Class<? extends Closeable> keyClass = key.getClass();
        getCloseableFields(keyClass, fields);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Closeable o = (Closeable) field.get(key);
                if (o != null && nested.add(o) && depth > 1)
                    addNested(nested, o, depth - 1);
            } catch (IllegalAccessException e) {
                Jvm.warn().on(keyClass, e);
            }
        }
    }

    private static void getCloseableFields(Class<?> keyClass, Set<Field> fields) {
        if (keyClass == null || keyClass == Object.class)
            return;
        for (Field field : keyClass.getDeclaredFields())
            if (Closeable.class.isAssignableFrom(field.getType()))
                fields.add(field);
        getCloseableFields(keyClass.getSuperclass(), fields);
    }

    public static void unmonitor(Closeable closeable) {
        final Set<Closeable> set = closeableSet;
        if (set != null)
            synchronized (set) {
                set.remove(closeable);
            }
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
        long start = System.currentTimeMillis();
        while (closed != STATE_CLOSED) {
            if (System.currentTimeMillis() > start + 10_000) {
                Jvm.warn().on(getClass(), "Aborting close()ing object " + referenceId +
                        " after " + (System.currentTimeMillis() - start) / 1e3 + " secs", new StackTrace("waiting here", closedHere));
                break;
            }
            Jvm.pause(1);
        }
    }

    /**
     * Called when a resources needs to be open to use it.
     *
     * @throws ClosedIllegalStateException if closed
     * @throws IllegalStateException       if the thread safety check fails
     */
    @Override
    public void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosed())
            throw new ClosedIllegalStateException(getClass().getName() + " closed for " + Thread.currentThread().getName(), closedHere);
        if (!DISABLE_SINGLE_THREADED_CHECK)
            threadSafetyCheck(true);
    }

    public void throwExceptionIfClosedInSetter() throws IllegalStateException {
        if (isClosed())
            throw new ClosedIllegalStateException(getClass().getName() + " closed for " + Thread.currentThread().getName(), closedHere);
        // only check it if this has been used.
        if (!DISABLE_SINGLE_THREADED_CHECK)
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
            Jvm.debug().on(getClass(), t);
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

    @Deprecated(/* to be removed in x.25 */)
    public void resetUsedByThread() {
        singleThreadedCheckReset();
    }

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

    @Deprecated(/* to be removed in x.25 */)
    public boolean disableThreadSafetyCheck() {
        return singleThreadedCheckDisabled;
    }

    protected boolean singleThreadedCheckDisabled() {
        return singleThreadedCheckDisabled;
    }

    @Deprecated(/* to be removed in x.25 */)
    public AbstractCloseable disableThreadSafetyCheck(boolean disableThreadSafetyCheck) {
        singleThreadedCheckDisabled(disableThreadSafetyCheck);
        return this;
    }

    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
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
