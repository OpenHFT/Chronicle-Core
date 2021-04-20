package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import org.junit.Test;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class CleaningThreadTest {
    @Test
    public void cleanupThreadLocal() throws InterruptedException {
        String threadName = "ctl-test";
        BlockingQueue<String> ints = new LinkedBlockingQueue<>();
        CleaningThreadLocal<String> counter = CleaningThreadLocal.withCleanup(() -> Thread.currentThread().getName(), ints::add);
        CleaningThread ct = new CleaningThread(() -> assertEquals(threadName, counter.get()), threadName);
        ct.start();
        String poll = ints.poll(1, TimeUnit.SECONDS);
        assertEquals(threadName, poll);
    }

    @Test
    public void testRemove() {
        int[] counter = {0};
        CleaningThreadLocal<Integer> ctl = CleaningThreadLocal.withCloseQuietly(() -> counter[0]++);
        assertEquals(0, (int) ctl.get());
        CleaningThread.performCleanup(Thread.currentThread());
        assertEquals(1, (int) ctl.get());
    }

    @Test
    public void resetThreadAffinity() throws InterruptedException {
        final BitSet affinity = Affinity.getAffinity();
        assumeTrue(affinity.cardinality() > 2);
        assumeTrue(AffinityLock.BASE_AFFINITY.cardinality() > 2);
        try {
            Affinity.setAffinity(1);
            BitSet[] nestedAffinity = {null};
            CleaningThread ct = new CleaningThread(() -> nestedAffinity[0] = Affinity.getAffinity());
            ct.start();
            ct.join();
            assertEquals(AffinityLock.BASE_AFFINITY, nestedAffinity[0]);
        } finally {
            Affinity.setAffinity(affinity);
        }
    }
}