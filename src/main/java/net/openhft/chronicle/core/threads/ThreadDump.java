/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for monitoring and managing threads.
 * <p>
 * Intended for unit and integration test assertions to detect stray threads and
 * should not be used on hot paths as the checks are relatively expensive.
 * <p>
 * Provides functionality for collecting stack traces of threads and detecting
 * unexpected thread creation, which can be useful in testing and debugging scenarios.
 */
public class ThreadDump {

    public static final String IGNORE_THREAD_IF_IN_NAME = "~";
    static final Map<Thread, StackTrace> THREAD_STACK_TRACE_MAP =
            new WeakIdentityHashMap<>();
    @NotNull
    final transient Set<Thread> threads;
    final Set<String> ignored = new HashSet<>();

    /**
     * Constructs a ThreadDump instance, initializing the set of threads
     * at the time of creation.
     */
    public ThreadDump() {
        this.threads = new HashSet<>(Arrays.asList(getAllThreadsInGroup()));
        ignored.add("Time-limited test");
        ignored.add("Attach Listener");
        ignored.add("process reaper");
        ignored.add("junit-jupiter-timeout-watcher");
    }

    /**
     * Adds a thread with its stack trace to the map of monitored threads.
     *
     * @param t          the thread to be monitored
     * @param stackTrace the stack trace of the thread
     */
    public static void add(Thread t, StackTrace stackTrace) {
        if (Jvm.isResourceTracing())
            THREAD_STACK_TRACE_MAP.put(t, stackTrace);
    }

    /**
     * Retrieves the stack trace for a given thread.
     *
     * @param thread the thread whose stack trace is to be retrieved
     * @return the stack trace for the given thread
     */
    public static StackTrace createdHereFor(Thread thread) {
        return THREAD_STACK_TRACE_MAP.get(thread);
    }

    private static boolean startsWith(String str, String... strs) {
        for (String s : strs) {
            if (str.startsWith(s))
                return true;
        }
        return false;
    }

    /**
     * Marks a thread to be ignored by the {@link #assertNoNewThreads()} check.
     *
     * @param threadName the name of the thread to be ignored
     */
    public void ignore(String threadName) {
        ignored.add(threadName);
    }

    /**
     * Fail if any new threads still exist after an adaptive wait.
     *
     * <p>The initial set of threads is captured when this object is constructed.
     * The method waits in a loop with exponentially increasing pauses to give
     * short-lived threads time to finish.
     */
    public void assertNoNewThreads() {
        assertNoNewThreads(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Fail if any new threads remain after a supplied delay.
     *
     * <p>The wait is adaptive: up to fourteen iterations (eighteen on ARM) are
     * performed, each pausing for {@code delay} divided across the loop plus an
     * exponential back-off of {@code 1L << (i/2)} milliseconds. Longer delays on
     * ARM compensate for slower context switching.
     *
     * @param delay     total extra time to wait across all iterations
     * @param delayUnit unit of {@code delay}
     */
    public void assertNoNewThreads(int delay, @NotNull TimeUnit delayUnit) {
        int last = Jvm.isArm() ? 18 : 14;
        long delayMillis = (delayUnit.toMillis(delay) + last - 1) / last;
        for (int i = 1; i <= last; i++) {
            Thread.yield();
            Thread[] group = getAllThreadsInGroup();
            List<Thread> extra = i == last ? new ArrayList<>() : null;
            boolean ok = true;
            for (Thread t : group) {
                if (t != null && t.isAlive() && !this.threads.contains(t) && isExtra(t.getName())) {
                    // a thread is alive that we didn't expect to be
                    ok = false;
                    if (i == last) {
                        extra.add(t);
                    } else {
                        break;
                    }
                }
            }
            if (ok)
                return;
            if (i == last) {
                if (extra.isEmpty())
                    break;

                AssertionError assertionError = new AssertionError("Threads still running " + extra);
                for (Thread thread : extra) {
                    addThreadErrorDetails(assertionError, thread);
                }
                throw assertionError;
            }
            Jvm.pause(delayMillis + (1L << (i/2)));
        }
    }

    @SuppressWarnings("java:S3014")
    private Thread[] getAllThreadsInGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int threadCountEstimate = threadGroup.activeCount();
        Thread[] threads = new Thread[threadCountEstimate + 8];
        threadGroup.enumerate(threads);
        // NOTE: many entries will be null
        return threads;
    }

    private boolean isExtra(String name) {
        if (name.contains(IGNORE_THREAD_IF_IN_NAME))
            return false;
        if (ignored.contains(name))
            return false;
        if (startsWith(name, "RMI ", "VM JFR ", "JFR ", "JMX ", "ForkJoinPool.commonPool-worker-", "JVMCI"))
            return false;
        if (name.startsWith("HttpClient-") && name.endsWith("-SelectorManager"))
            return false;
        return true;
    }

    private void addThreadErrorDetails(AssertionError assertionError, Thread thread) {
        StackTrace stackTrace0 = ThreadDump.createdHereFor(thread);
        StackTrace st = new StackTrace(thread.toString(), stackTrace0);

        StackTraceElement[] stackTrace = thread.getStackTrace();
        st.setStackTrace(stackTrace);

        assertionError.addSuppressed(st);
    }
}
