/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CleaningThreadTest extends CoreTestCommon {
    @Test
    public void cleanupThreadLocal() throws InterruptedException {
        String threadName = "ctl-test";
        BlockingQueue<String> ints = new LinkedBlockingQueue<>();
        CleaningThreadLocal<String> counter = CleaningThreadLocal.withCleanup(() -> Thread.currentThread().getName(), ints::add);
        CleaningThread ct = new CleaningThread(() -> assertEquals(threadName, counter.get(), "thread-local value should match the thread name"), threadName);
        ct.start();
        String poll = ints.poll(1, TimeUnit.SECONDS);
        assertEquals(threadName, poll, "thread should have expected name");
    }

    @Test
    public void testRemove() {
        int[] counter = {0};
        CleaningThreadLocal<Integer> ctl = CleaningThreadLocal.withCloseQuietly(() -> counter[0]++);
        assertEquals(0, (int) ctl.get(), "initial get should return first supplier value");
        CleaningThread.performCleanup(Thread.currentThread());
        assertEquals(1, (int) ctl.get(), "get after cleanup should return incremented supplier value");
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
            assertEquals(AffinityLock.BASE_AFFINITY, nestedAffinity[0], "operation result should equal expected value");
        } finally {
            Affinity.setAffinity(affinity);
        }
    }
}
