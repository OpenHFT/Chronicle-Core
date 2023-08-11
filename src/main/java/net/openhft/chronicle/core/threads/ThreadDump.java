/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
     * Asserts that no new threads are running beyond the ones that existed at the time
     * this ThreadDump instance was created. This method waits for a short period
     * of time for threads to terminate before throwing an AssertionError if new threads are detected.
     * <p>
     * This method can be used in testing scenarios to ensure that no unexpected threads have been left running.
     */
    public void assertNoNewThreads() {
        assertNoNewThreads(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Asserts that no new threads are running beyond the ones that existed at the time
     * this ThreadDump instance was created. This method waits for a specified delay
     * for threads to terminate before throwing an AssertionError if new threads are detected.
     *
     * @param delay     the extra time to wait for threads to terminate
     * @param delayUnit the time unit of the delay parameter
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

    private Thread[] getAllThreadsInGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int threadCountEstimate = threadGroup.activeCount();
        Thread[] threads = new Thread[threadCountEstimate + 8];
        // one pass
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
