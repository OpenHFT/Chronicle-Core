/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

public class ThreadDump {
    public static final String IGNORE_THREAD_IF_IN_NAME = "~";
    static final Map<Thread, StackTrace> THREAD_STACK_TRACE_MAP =
            new WeakIdentityHashMap<>();
    @NotNull
    final transient Set<Thread> threads;
    final Set<String> ignored = new HashSet<>();

    public ThreadDump() {
        this.threads = new HashSet<>(Thread.getAllStackTraces().keySet());
        ignored.add("Time-limited test");
        ignored.add("Attach Listener");
        ignored.add("process reaper");
        ignored.add("junit-jupiter-timeout-watcher");
    }

    public static void add(Thread t, StackTrace stackTrace) {
        if (Jvm.isResourceTracing())
            THREAD_STACK_TRACE_MAP.put(t, stackTrace);
    }

    public static StackTrace createdHereFor(Thread thread) {
        return THREAD_STACK_TRACE_MAP.get(thread);
    }

    public void ignore(String threadName) {
        ignored.add(threadName);
    }

    /**
     * Waits for all new threads to finish execution, up for 50ms.
     * <p>
     * Then, prints 3 warnings after 0ms, 200ms and 650ms. Then, throws an exception after 1450ms.
     */
    public void assertNoNewThreads() {
        assertNoNewThreads(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Waits for all new threads to finish execution, up for the specified amount of time ± 50ms.
     * <p>
     * Then, prints 3 warnings after 0ms, 200ms and 650ms. Then, throws an exception after 1450ms.
     */
    public void assertNoNewThreads(int delay, @NotNull TimeUnit delayUnit) {
        long start = System.nanoTime();
        long delayNanos = delayUnit.toNanos(delay);
        @Nullable Map<Thread, StackTraceElement[]> allStackTraces = null;
        AssertionError ae = null;
        int last = 4;
        for (int i = 1; i <= last; ) {
            Jvm.pause(i * i * 50L);
            allStackTraces = Thread.getAllStackTraces();
            allStackTraces.keySet().removeAll(threads);
            if (allStackTraces.isEmpty())
                return;
            allStackTraces.keySet()
                    .removeIf(next -> ignored.stream()
                            .anyMatch(item -> next.getName().contains(item)));
            allStackTraces.keySet()
                    .removeIf(next -> startsWith(next.getName(), "RMI ", "VM JFR ", "JFR ", "JMX ", "ForkJoinPool.commonPool-worker-"));
            allStackTraces.keySet()
                    .removeIf(next -> next.getName().startsWith("HttpClient-") & next.getName().endsWith("-SelectorManager"));
            allStackTraces.keySet()
                    .removeIf(next -> next.getName().contains(IGNORE_THREAD_IF_IN_NAME));
            if (allStackTraces.isEmpty())
                return;
            if (i == 1 && System.nanoTime() - start < delayNanos) {
                continue;
            }
            i++;
            if (i == last)
                ae = new AssertionError("Threads still running " + allStackTraces.keySet());
            for (@NotNull Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
                @NotNull StringBuilder sb = new StringBuilder();
                Thread thread = threadEntry.getKey();
                sb.append("Thread still running ").append(thread);
                Jvm.trimStackTrace(sb, threadEntry.getValue());
                System.err.println(sb);
                if (i == last) {
                    StackTrace stackTrace = ThreadDump.createdHereFor(thread);
                    StackTrace st = new StackTrace(thread.toString(), stackTrace);
                    st.setStackTrace(threadEntry.getValue());
                    ae.addSuppressed(st);
                }
            }
        }
        throw ae;
    }

    private static boolean startsWith(String str, String... strs) {
        for (String s : strs) {
            if (str.startsWith(s))
                return true;
        }
        return false;
    }
}
