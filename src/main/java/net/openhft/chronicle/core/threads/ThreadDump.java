/*
 * Copyright 2016-2020 Chronicle Software
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ThreadDump {
    @NotNull
    final Set<Thread> threads;
    final Set<String> ignored = new HashSet<>();

    public ThreadDump() {
        this.threads = new HashSet<>(Thread.getAllStackTraces().keySet());
        ignored.add("Time-limited test");
        ignored.add("Attach Listener");
        ignored.add("process reaper");
        ignored.add("chronicle-weak-reference-cleaner");
        for (int i = 0, max = Runtime.getRuntime().availableProcessors(); i < max; i++)
            ignored.add("ForkJoinPool.commonPool-worker-" + i);
    }

    public void ignore(String threadName) {
        ignored.add(threadName);
    }

    /**
     * Waits for all new threads to finish execution, up for 50ms.
     * <p>
     * Then, prints 3 warnings after 0ms, 200ms and 650ms. Then, throws an exception after 800ms.
     */
    public void assertNoNewThreads() {
        assertNoNewThreads(0, TimeUnit.NANOSECONDS);
    }

    /**
     * Waits for all new threads to finish execution, up for the specified amount of time ± 50ms.
     * <p>
     * Then, prints 3 warnings after 0ms, 200ms and 650ms. Then, throws an exception after 800ms.
     */
    public void assertNoNewThreads(int delay, @NotNull TimeUnit delayUnit) {
        long start = System.nanoTime();
        long delayNanos = delayUnit.toNanos(delay);
        @Nullable Map<Thread, StackTraceElement[]> allStackTraces = null;
        for (int i = 1; i < 5; ) {
            Jvm.pause(i * i * 50);
            allStackTraces = Thread.getAllStackTraces();
            allStackTraces.keySet().removeAll(threads);
            if (allStackTraces.isEmpty())
                return;
            allStackTraces.keySet().removeIf(next -> ignored.stream().anyMatch(item -> next.getName().contains(item)));
            if (allStackTraces.isEmpty())
                return;
            if (i == 1 && System.nanoTime() - start < delayNanos) {
                continue;
            }
            i++;
            for (@NotNull Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
                @NotNull StringBuilder sb = new StringBuilder();
                sb.append("Thread still running ").append(threadEntry.getKey());
                Jvm.trimStackTrace(sb, threadEntry.getValue());
                System.err.println(sb);
            }
        }
        throw new AssertionError("Threads still running " + allStackTraces.keySet());
    }
}
