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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        this.threads = new HashSet<>(Thread.getAllStackTraces().keySet());
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
     * @param delay     the maximum time to wait for threads to terminate
     * @param delayUnit the time unit of the delay parameter
     */
    public void assertNoNewThreads(int delay, @NotNull TimeUnit delayUnit) {
        long start = System.nanoTime();
        long delayNanos = delayUnit.toNanos(delay);

        AssertionError assertionError = null;
        int last = 7;
        for (int i = 1; i <= last; ) {
            Jvm.pause(i * i * 50L);
            Map<Thread, StackTraceElement[]> allStackTraces = collectThreadStackTraces();

            if (allStackTraces.isEmpty())
                return;

            if (i == 1 && System.nanoTime() - start < delayNanos) {
                continue;
            }

            i++;
            if (i == last) {
                assertionError = new AssertionError("Threads still running " + allStackTraces.keySet());
            }

            for (Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
                if (i == last) {
                    addThreadErrorDetails(assertionError, threadEntry);
                }
            }
        }

        if (assertionError == null)
            throw new NullPointerException("This should not happen as assertionError should be set.");

        throw assertionError;
    }

    private Map<Thread, StackTraceElement[]> collectThreadStackTraces() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        allStackTraces.keySet().removeAll(threads);

        filterStackTraces(allStackTraces);

        return allStackTraces;
    }

    private void filterStackTraces(Map<Thread, StackTraceElement[]> allStackTraces) {
        allStackTraces.keySet()
                .removeIf(next -> ignored.stream()
                        .anyMatch(item -> next.getName().contains(item)));
        allStackTraces.keySet()
                .removeIf(next -> startsWith(next.getName(), "RMI ", "VM JFR ", "JFR ", "JMX ", "ForkJoinPool.commonPool-worker-", "JVMCI"));
        allStackTraces.keySet()
                .removeIf(next -> next.getName().startsWith("HttpClient-") && next.getName().endsWith("-SelectorManager"));
        allStackTraces.keySet()
                .removeIf(next -> next.getName().contains(IGNORE_THREAD_IF_IN_NAME));
    }

    private void addThreadErrorDetails(AssertionError assertionError, Map.Entry<Thread, StackTraceElement[]> threadEntry) {
        Thread thread = threadEntry.getKey();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread still running ").append(thread);
        Jvm.trimStackTrace(sb, threadEntry.getValue());

        StackTrace stackTrace = ThreadDump.createdHereFor(thread);
        StackTrace st = new StackTrace(thread.toString(), stackTrace);
        st.setStackTrace(threadEntry.getValue());
        assertionError.addSuppressed(st);
    }

}
