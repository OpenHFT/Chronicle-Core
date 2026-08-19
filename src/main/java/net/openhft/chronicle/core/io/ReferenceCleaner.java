/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link PhantomReference}-based replacement for {@code finalize()} that runs a
 * cleanup action once its referent has become unreachable.
 * <p>
 * This is the Java&nbsp;8-compatible equivalent of {@code java.lang.ref.Cleaner}
 * (added in Java&nbsp;9) and exists to support removing the remaining
 * {@code finalize()} overrides in this module (issue&nbsp;#331). Unlike
 * finalisation it does not defeat escape analysis, cannot resurrect objects, and
 * does not run on the GC's finalizer thread.
 *
 * <h2>Usage contract</h2>
 * The {@code cleanupAction} <strong>must not</strong> hold a strong reference to the
 * registered object, directly or through a captured lambda: doing so would make the
 * object permanently reachable and the cleanup would never run. Capture only the
 * separate state that needs releasing (a file descriptor, an address, a delegate
 * {@link java.io.Closeable}, etc.).
 * <pre>{@code
 * // GOOD: cleanup captures only the resource, not the owner
 * class Owner {
 *     private final ReferenceCleaner.Cleanable cleanable;
 *     Owner(Resource r) {
 *         this.cleanable = ReferenceCleaner.register(this, r::release);
 *     }
 *     void close() { cleanable.clean(); } // deterministic release
 * }
 * }</pre>
 *
 * <h2>Finaliser migration inventory (issue #331)</h2>
 * As of this change the module still overrides {@code finalize()} in three places.
 * They should move to {@code ReferenceCleaner}/{@code java.lang.ref.Cleaner} in the
 * first post-Java-8 release, in this order:
 * <ol>
 *   <li>{@code CleaningRandomAccessFile} &mdash; safety-net close of the file. The
 *       cleanup must capture a closeable that closes the file descriptor without
 *       referencing the {@code RandomAccessFile} instance.</li>
 *   <li>{@code AbstractCloseable.Finalizer} &mdash; warn-and-close of a leaked
 *       resource. Needs the warning/close state extracted into a separate holder so
 *       the cleanup does not strongly reference the closeable.</li>
 *   <li>{@code CloseableUtils.gcAndWaitForCloseablesToClose} &mdash; a test/diagnostic
 *       barrier that deliberately waits for the <em>finalizer</em> thread. It must
 *       migrate <strong>last</strong>: while the classes above still finalise, this
 *       barrier has to observe finalisation, so replacing it earlier would weaken the
 *       guarantee it provides.</li>
 * </ol>
 * The migrated classes should additionally be verified under a JVM run with
 * finalisation disabled (e.g. {@code --finalization=disabled} on JDK&nbsp;18+), so the
 * reference-based cleanup is exercised as the sole path.
 */
public final class ReferenceCleaner {

    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();

    // Keeps each Cleanable reachable until it has been cleaned; otherwise the
    // PhantomReference itself could be collected before it is enqueued.
    private static final Set<Cleanable> REGISTERED = ConcurrentHashMap.newKeySet();

    static {
        final Thread thread = new Thread(ReferenceCleaner::processQueue, "reference~cleaner");
        thread.setDaemon(true);
        thread.start();
    }

    private ReferenceCleaner() {
    }

    /**
     * Registers {@code obj} so that {@code cleanupAction} is run once {@code obj}
     * becomes phantom-reachable (or when {@link Cleanable#clean()} is called first,
     * whichever happens sooner). The action runs at most once.
     *
     * @param obj           the object whose unreachability triggers cleanup
     * @param cleanupAction the action to run; must not reference {@code obj}
     * @return a handle allowing the cleanup to be run deterministically
     */
    @NotNull
    public static Cleanable register(@NotNull Object obj, @NotNull Runnable cleanupAction) {
        if (obj == null)
            throw new NullPointerException("obj");
        if (cleanupAction == null)
            throw new NullPointerException("cleanupAction");
        return new Cleanable(obj, cleanupAction);
    }

    private static void processQueue() {
        while (true) {
            try {
                final Cleanable cleanable = (Cleanable) QUEUE.remove();
                cleanable.clean();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                Jvm.warn().on(ReferenceCleaner.class, "Failed to run cleanup action", t);
            }
        }
    }

    /**
     * A handle to a registered cleanup action.
     */
    public static final class Cleanable extends PhantomReference<Object> {
        private final Runnable cleanupAction;
        private final AtomicBoolean cleaned = new AtomicBoolean();

        Cleanable(Object referent, Runnable cleanupAction) {
            super(referent, QUEUE);
            this.cleanupAction = cleanupAction;
            REGISTERED.add(this);
        }

        /**
         * Runs the cleanup action if it has not already run. Safe to call explicitly
         * (for deterministic release) and idempotent with the automatic path.
         */
        public void clean() {
            if (cleaned.compareAndSet(false, true)) {
                REGISTERED.remove(this);
                clear();
                cleanupAction.run();
            }
        }
    }
}
