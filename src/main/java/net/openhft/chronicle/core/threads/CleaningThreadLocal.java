/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * <h2>CleaningThreadLocal - a ThreadLocal that never leaks native resources</h2>
 *
 * <p>{@code CleaningThreadLocal} augments {@link ThreadLocal} with two additional
 * capabilities:</p>
 *
 * <ol>
 *   <li><b>In-thread cleanup for {@link CleaningThread}s.</b><br>
 *       A value attached to a {@code CleaningThread} is cleaned immediately
 *       (still on that same thread) when it is replaced or {@code remove()} is called.</li>
 *
 *   <li><b>Best-effort cleanup for <em>ordinary</em> threads.</b><br>
 *       Values produced by threads that are <em>not</em> instances of
 *       {@code CleaningThread} are registered globally. The static method
 *       {@link #cleanupNonCleaningThreads()} can be invoked-on a timer, in a
 *       background executor, or during JVM shutdown-to free resources that
 *       belong to threads which have already terminated.</li>
 * </ol>
 *
 * <p>This class is typically used for off-heap buffers, direct I/O handles,
 * {@link java.io.Closeable Closeable}s, or any resource that must be released
 * deterministically even when user code forgets to call {@code close()}.</p>
 *
 * <h3>Controlling orphan tracking</h3>
 *
 * <p>Orphan tracking can be toggled in two mutually-aware ways (highest
 * precedence first):</p>
 *
 * <ol>
 *   <li><b>System property</b><br>
 *       Setting <code>-Ddisable.ctl.orphan.tracking</code>
 *       <strong>globally disables</strong> orphan tracking for every
 *       {@code CleaningThreadLocal} in the JVM, regardless of assertions or
 *       constructor flags.  This is the easiest "set-and-forget" option for
 *       ultra-low-latency production deployments.</li>
 *
 *   <li><b>JVM assertions</b><br>
 *       When above mechanisms is not used, orphan tracking is
 *       enabled <em>only</em> when assertions are turned on ( {@code -ea} ).
 *       This keeps the production fast path allocation-free by default.</li>
 * </ol>
 *
 * <p>Summary of common launch options:</p>
 *
 * <pre>
 *   # production, no tracking
 *   java -Ddisable.ctl.orphan.tracking  ...
 *
 *   # production, tracking ON for diagnostics
 *   java -ea:net.openhft.chronicle.core.threads.CleaningThreadLocal -cp ...
 *
 *   # tests: assertions on, but tracking OFF for this one class
 *   java -ea -da:net.openhft.chronicle.core.threads.CleaningThreadLocal ...
 * </pre>
 *
 * @param <T> the type stored in the thread-local variable
 * @see CleaningThread
 * @see #cleanupNonCleaningThreads()
 */

public class CleaningThreadLocal<T> extends ThreadLocal<T> {
    private static final boolean DISABLE_CTL_ORPHAN_TRACKING =
            Jvm.getBoolean("disable.ctl.orphan.tracking");
    /**
     * All {@code CleaningThreadLocal} instances that may currently hold orphan values.
     * Guarded by the set's intrinsic monitor; contention is minimal because items are
     * added only at construction time and removed when empty.
     */
    private static final Set<CleaningThreadLocal<?>> cleaningThreadLocals =
            Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Factory for the initial value. Never returns {@code null}.
     */
    @NotNull
    private final Supplier<T> supplier;

    /**
     * Optional transformation applied by {@link #get()}.  A common use-case is
     * {@code ByteBuffer::duplicate} to hide internal position/limit mutations
     * from callers.
     */
    @NotNull
    private final Function<T, T> getWrapper;

    /**
     * Action that releases or closes the resource.
     */
    @NotNull
    private final ThrowingConsumer<T, Exception> cleanup;
    /**
     * {@code true} when we should record values belonging to non-CleaningThreads
     * so they can be cleaned up later.
     */
    private final boolean trackNonCleaningThreads;
    /**
     * Map &lt;Thread,value&gt; that holds the latest resource produced by each
     * <em>non-CleaningThread</em>.  Only initialised when tracking is enabled
     * to keep the memory overhead tiny in the common case.
     *
     * <p>Guarded by {@link #cleaningThreadLocals}.
     */
    private Map<Thread, Object> nonCleaningThreadValues;

    private CleaningThreadLocal(Supplier<T> supplier,
                                ThrowingConsumer<T, Exception> cleanup) {
        this(supplier, cleanup, UnaryOperator.identity());
    }

    private CleaningThreadLocal(Supplier<T> supplier,
                                ThrowingConsumer<T, Exception> cleanup,
                                UnaryOperator<T> getWrapper) {
        this(supplier, cleanup, getWrapper, null);
    }

    /**
     * Package-private constructor that allows unit tests (or power users) to
     * force orphan-tracking on or off regardless of the JVM's assertion flags.
     *
     * @param overrideTrackNonCleaningThreads {@code Boolean.TRUE} to force
     *                                        orphan-tracking ON, {@code Boolean.FALSE} to force it OFF,
     *                                        or {@code null} to accept the default "track only when -ea".
     */
    CleaningThreadLocal(Supplier<T> supplier,
                        ThrowingConsumer<T, Exception> cleanup,
                        UnaryOperator<T> getWrapper,
                        Boolean overrideTrackNonCleaningThreads) {

        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
        this.getWrapper = Objects.requireNonNull(getWrapper, "getWrapper");

        // decide whether to gather orphan values
        boolean track = false;
        if (CleaningThreadLocal.class.desiredAssertionStatus()) {
            // Only initialise tracking when assertions are enabled to avoid production overhead
            track = enableOrphanTracking();
            assert track;
        }
        this.trackNonCleaningThreads =
                !DISABLE_CTL_ORPHAN_TRACKING &&
                        (overrideTrackNonCleaningThreads != null
                                ? overrideTrackNonCleaningThreads
                                : track);
    }

    /**
     * Creates a {@code CleaningThreadLocal} whose cleanup simply calls
     * {@link Closeable#closeQuietly}.
     *
     * @param supplier supplies the resource (may return {@code null})
     * @param <T>      any subtype of {@link Closeable}
     * @return a new {@code CleaningThreadLocal}
     */
    public static <T> CleaningThreadLocal<T> withCloseQuietly(Supplier<T> supplier) {
        return new CleaningThreadLocal<>(supplier, Closeable::closeQuietly);
    }

    /**
     * Creates a {@code CleaningThreadLocal} with a custom cleanup action but
     * without an initial-value supplier.
     *
     * @param cleanup The consumer that cleans up the resource.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(() -> null, cleanup);
    }

    /**
     * Creates a CleaningThreadLocal with a supplier and a custom cleanup strategy.
     *
     * @param supplier The supplier that provides the resource.
     * @param cleanup  The consumer that cleans up the resource.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(supplier, cleanup);
    }

    /**
     * Creates a CleaningThreadLocal with a supplier, a custom cleanup strategy, and a function to apply when the get method is called.
     *
     * @param supplier   The supplier that provides the resource.
     * @param cleanup    The consumer that cleans up the resource.
     * @param getWrapper The function to apply when the get method is called.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, Function<T, T> getWrapper) {
        return new CleaningThreadLocal<>(supplier, cleanup, getWrapper::apply);
    }

    /**
     * Performs a single orphan-sweep:
     * <ol>
     *   <li>Iterates through all live {@code CleaningThreadLocal}s that track
     *       non-CleaningThreads.</li>
     *   <li>Identifies threads that have terminated.</li>
     *   <li>Invokes the configured cleanup for each stale value.</li>
     *   <li>Purges the entry from internal bookkeeping.</li>
     * </ol>
     *
     * <p>Call at whatever cadence suits your application (e.g.&nbsp;every few
     * seconds, once a minute, or only at JVM shutdown).</p>
     */
    public static void cleanupNonCleaningThreads() {
        if (cleaningThreadLocals.isEmpty())
            return;

        cleaningThreadLocals.removeIf(CleaningThreadLocal::doCleanupNonCleaningThreads);
    }

    private boolean doCleanupNonCleaningThreads() {
        if (!trackNonCleaningThreads)
            return true;

        for (Iterator<Map.Entry<Thread, Object>> mapIt =
             nonCleaningThreadValues.entrySet().iterator();
             mapIt.hasNext(); ) {

            Map.Entry<Thread, Object> e = mapIt.next();
            if (!e.getKey().isAlive()) {
                cleanup(uncheckedCast(e.getValue()));
                mapIt.remove();
            }
        }
        return nonCleaningThreadValues.isEmpty();
    }

    /**
     * Returns the initial value and records it for later cleanup when the
     * thread is not a {@link CleaningThread}.
     */
    @Override
    protected T initialValue() {
        T value = supplier.get();
        if (trackNonCleaningThreads &&
                !(Thread.currentThread() instanceof CleaningThread)) {
            nonCleaningThreadValues.put(Thread.currentThread(), value);
        }
        return value;
    }

    /**
     * Returns the value of this CleaningThreadLocal.
     *
     * @return The current value.
     */
    @Override
    public T get() {
        return getWrapper.apply(super.get());
    }

    /**
     * Sets the value of this CleaningThreadLocal and performs cleanup if necessary.
     *
     * @param value The new value to be set.
     */
    @Override
    public void set(T value) {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (trackNonCleaningThreads) {
            @SuppressWarnings("unchecked")
            T previous = (T) nonCleaningThreadValues.put(thread, value);
            cleanup(previous);
        }
        super.set(value);
    }

    /**
     * Removes the value for this CleaningThreadLocal from the current thread and performs cleanup.
     */
    @Override
    public void remove() {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (trackNonCleaningThreads) {
            @SuppressWarnings("unchecked")
            T previous = (T) nonCleaningThreadValues.remove(thread);
            cleanup(previous);
        }
        super.remove();
    }

    /**
     * Runs exactly once per instance <em>when assertions are enabled</em>.
     * Registers {@code this} in the global set and initialises the orphan map.
     * No code executes when {@code -ea} is absent, so production performance
     * is unaffected.
     *
     * @return always {@code true}; used only for the assignment in the assert.
     */
    private boolean enableOrphanTracking() {
        // prune any stale CTLs before adding a new one
        cleanupNonCleaningThreads();

        cleaningThreadLocals.add(this);
        nonCleaningThreadValues = Collections.synchronizedMap(new LinkedHashMap<>());
        return true;
    }

    /**
     * Idempotent helper that applies {@link #cleanup} to {@code value}.
     * Any exception thrown by user code is swallowed and logged so that
     * cleanup can never compromise the core invariant of this class.
     */
    public synchronized void cleanup(@Nullable T value) {
        if (value == null) return;
        try {
            cleanup.accept(value);
        } catch (Exception ex) {
            Jvm.warn().on(getClass(),
                    "Exception during cleanup of " + value.getClass(), ex);
        }
    }

    @Override
    public String toString() {
        return "CleaningThreadLocal{" +
                "trackedNonCleaningThreads=" +
                (nonCleaningThreadValues == null ? 0 : nonCleaningThreadValues.size()) +
                ", tracking=" + trackNonCleaningThreads +
                '}';
    }
}
