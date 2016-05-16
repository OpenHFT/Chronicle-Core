package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by peter on 09/04/16.
 */
public class ThreadDump {
    final Set<Thread> threads;
    final Set<String> ignored = new HashSet<>();

    public ThreadDump() {
        this.threads = new HashSet<>(Thread.getAllStackTraces().keySet());
        ignored.add("Time-limited test");
        ignored.add("Attach Listener");
        for (int i = 0, max = Runtime.getRuntime().availableProcessors(); i < max; i++)
            ignored.add("ForkJoinPool.commonPool-worker-" + i);
    }

    public void ignore(String threadName) {
        ignored.add(threadName);
    }

    public void assertNoNewThreads() {
        Map<Thread, StackTraceElement[]> allStackTraces = null;
        for (int i = 1; i < 4; i++) {
            Jvm.pause(i * i * 50);
            allStackTraces = Thread.getAllStackTraces();
            allStackTraces.keySet().removeAll(threads);
            if (allStackTraces.isEmpty())
                return;
            for (Iterator<Thread> iter = allStackTraces.keySet().iterator(); iter.hasNext(); ) {
                Thread next = iter.next();
                if (ignored.contains(next.getName()))
                    iter.remove();
            }
            if (allStackTraces.isEmpty())
                return;
            for (Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Thread still running " + threadEntry.getKey());
                Jvm.trimStackTrace(sb, threadEntry.getValue());
                System.err.println(sb);
            }
        }
        throw new AssertionError("Threads still running " + allStackTraces.keySet());
    }
}
