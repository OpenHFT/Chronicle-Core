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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CleaningThreadTest extends CoreTestCommon {
    @Test
    void cleanupThreadLocal() throws InterruptedException {
        String threadName = "ctl-test";
        BlockingQueue<String> ints = new LinkedBlockingQueue<>();
        CleaningThreadLocal<String> counter = CleaningThreadLocal.withCleanup(() -> Thread.currentThread().getName(), ints::add);
        CleaningThread ct = new CleaningThread(() -> assertEquals(threadName, counter.get()), threadName);
        ct.start();
        String poll = ints.poll(1, TimeUnit.SECONDS);
        assertEquals(threadName, poll);
    }

    @Test
    void testRemove() {
        int[] counter = {0};
        CleaningThreadLocal<Integer> ctl = CleaningThreadLocal.withCloseQuietly(() -> counter[0]++);
        assertEquals(0, (int) ctl.get());
        CleaningThread.performCleanup(Thread.currentThread());
        assertEquals(1, (int) ctl.get());
    }

    @Test
    void resetThreadAffinity() throws InterruptedException {
        final BitSet affinity = Affinity.getAffinity();
        final BitSet baseAffinity = AffinityLock.BASE_AFFINITY;
        int cpu = baseAffinity.nextSetBit(0);
        assertTrue(cpu >= 0, "Base affinity must expose at least one CPU");
        try {
            Affinity.setAffinity(cpu);
            BitSet[] nestedAffinity = {null};
            CleaningThread ct = new CleaningThread(() -> nestedAffinity[0] = Affinity.getAffinity());
            ct.start();
            ct.join();
            assertEquals(baseAffinity, nestedAffinity[0]);
        } finally {
            Affinity.setAffinity(affinity);
        }
    }
}
