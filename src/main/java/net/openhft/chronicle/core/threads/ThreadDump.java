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
 * This class provides functionality for collecting stack traces of threads and detecting
 * unexpected thread creation, which can be useful in testing and debugging scenarios.
 * It allows capturing the state of all current threads at the time of its creation and
 * asserting that no new threads have been started beyond those that are expected.
 * </p>
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
     * present at the time of creation.
     * <p>
     * Additionally, some default thread names are ignored for the purpose of
     * thread existence checks, such as those used internally by testing frameworks
     * or JVM processes.
     * </p>
     */
    public ThreadDump() {
        this.threads = new HashSet<>(Arrays.asList(getAllThreadsInGroup()));
        // Add default ignored threads which are typically managed by JVM or test frameworks.
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
     * @return the stack trace for the given thread, or null if no stack trace has been recorded
     */
    public static StackTrace createdHereFor(Thread thread) {
        return THREAD_STACK_TRACE_MAP.get(thread);
    }

    /**
     * Checks if the provided string starts with any of the specified prefixes.
     *
     * @param str  the string to check
     * @param strs the prefixes to compare against
     * @return true if the string starts with any of the provided prefixes, false otherwise
     */
    private static boolean startsWith(String str, String... strs) {
        for (String s : strs) {
            if (str.startsWith(s))
                return true;
        }
        return false;
    }

    /**
     * Marks a thread to be ignored by the {@link #assertNoNewThreads()} check.
     * <p>
     * Ignored threads will not trigger an assertion error when the thread existence
     * check is performed. This is useful for threads that are known to exist and are not
     * relevant to the current testing context.
     * </p>
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
     * </p>
     */
    public void assertNoNewThreads() {
        assertNoNewThreads(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Asserts that no new threads are running beyond the ones that existed at the time
     * this ThreadDump instance was created. This method waits for a specified delay
     * for threads to terminate before throwing an AssertionError if new threads are detected.
     * <p>
     * This method can be particularly useful in testing scenarios to confirm that all
     * expected threads have completed their execution and no new or unexpected threads
     * have been started.
     * </p>
     *
     * @param delay     the extra time to wait for threads to terminate
     * @param delayUnit the time unit of the delay parameter
     * @throws AssertionError if new threads are detected after the delay
     */
    public void assertNoNewThreads(int delay, @NotNull TimeUnit delayUnit) {
        int last = Jvm.isArm() ? 18 : 14; // Number of retries, higher on ARM due to different performance characteristics
        long delayMillis = (delayUnit.toMillis(delay) + last - 1) / last; // Calculate delay per retry
        for (int i = 1; i <= last; i++) {
            Thread.yield(); // Hint to the scheduler that it can run other threads
            Thread[] group = getAllThreadsInGroup();
            List<Thread> extra = i == last ? new ArrayList<>() : null; // List of unexpected threads
            boolean ok = true; // Flag indicating if all threads are as expected

            for (Thread t : group) {
                if (t != null && t.isAlive() && !this.threads.contains(t) && isExtra(t.getName())) {
                    // Found a thread that is not expected to be alive
                    ok = false;
                    if (i == last) {
                        extra.add(t); // Add to list of unexpected threads if this is the last retry
                    } else {
                        break; // Exit early if we're not on the last retry
                    }
                }
            }

            if (ok)
                return; // All threads are as expected, return without error

            if (i == last) {
                if (extra.isEmpty())
                    break;

                // Construct and throw an AssertionError detailing the unexpected threads
                AssertionError assertionError = new AssertionError("Threads still running " + extra);
                for (Thread thread : extra) {
                    addThreadErrorDetails(assertionError, thread);
                }
                throw assertionError;
            }

            // Pause before the next retry to give threads time to terminate
            Jvm.pause(delayMillis + (1L << (i / 2)));
        }
    }

    /**
     * Retrieves all threads in the current thread's thread group.
     * <p>
     * This method may overestimate the number of threads, so additional space is allocated.
     * It returns an array of threads, some of which may be null.
     * </p>
     *
     * @return an array containing all threads in the current thread group
     */
    private Thread[] getAllThreadsInGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int threadCountEstimate = threadGroup.activeCount(); // Estimate the number of active threads
        Thread[] threads = new Thread[threadCountEstimate + 8]; // Allocate extra space for safety
        threadGroup.enumerate(threads); // Populate the array with the actual threads
        return threads;
    }

    /**
     * Determines if a thread should be considered 'extra' based on its name.
     * <p>
     * 'Extra' threads are those not initially present or explicitly ignored, indicating
     * potential unexpected activity in the system under test.
     * </p>
     *
     * @param name the name of the thread to check
     * @return true if the thread is considered extra, false otherwise
     */
    private boolean isExtra(String name) {
        // Check various conditions to determine if the thread should be considered 'extra'
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

    /**
     * Adds details about a specific thread to an AssertionError.
     * <p>
     * This includes the stack trace of the thread, which can help in diagnosing
     * why a particular thread was unexpectedly found to be running.
     * </p>
     *
     * @param assertionError the assertion error to which the thread details will be added
     * @param thread         the thread whose details are being added
     */
    private void addThreadErrorDetails(AssertionError assertionError, Thread thread) {
        StackTrace stackTrace0 = ThreadDump.createdHereFor(thread);
        StackTrace st = new StackTrace(thread.toString(), stackTrace0);

        StackTraceElement[] stackTrace = thread.getStackTrace();
        st.setStackTrace(stackTrace);

        assertionError.addSuppressed(st);
    }
}
