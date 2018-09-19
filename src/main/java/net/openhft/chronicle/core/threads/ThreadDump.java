/*
 * Copyright 2016 higherfrequencytrading.com
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

/*
 * Created by Peter Lawrey on 09/04/16.
 */
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

    public void assertNoNewThreads() {
        @Nullable Map<Thread, StackTraceElement[]> allStackTraces = null;
        for (int i = 1; i < 5; i++) {
            Jvm.pause(i * i * 50);
            allStackTraces = Thread.getAllStackTraces();
            allStackTraces.keySet().removeAll(threads);
            if (allStackTraces.isEmpty())
                return;
            allStackTraces.keySet().removeIf(next -> ignored.stream().anyMatch(item -> next.getName().contains(item)));
            if (allStackTraces.isEmpty())
                return;
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
