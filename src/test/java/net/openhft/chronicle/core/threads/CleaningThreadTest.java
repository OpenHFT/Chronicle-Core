/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
        assumeFalse(OS.isMacOSX(), "macOS does not support thread affinity");
        final BitSet affinity = (BitSet) Affinity.getAffinity().clone();
        final BitSet baseAffinity = AffinityLock.BASE_AFFINITY;
        assumeTrue(baseAffinity.cardinality() > 1, "Reset requires a baseline with more than one CPU");
        int cpu = baseAffinity.nextSetBit(0);
        BitSet expectedPinned = new BitSet();
        expectedPinned.set(cpu);
        BitSet[] pinnedAffinity = {null};
        BitSet[] nestedAffinity = {null};
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CleaningThread ct = new CleaningThread(() -> nestedAffinity[0] = Affinity.getAffinity()) {
            @Override
            public void run() {
                BitSet original = (BitSet) Affinity.getAffinity().clone();
                try {
                    // Windows caches affinity per thread: explicitly pin the child
                    // before exercising CleaningThread's reset, rather than its parent.
                    Affinity.setAffinity(cpu);
                    pinnedAffinity[0] = (BitSet) Affinity.getAffinity().clone();
                    super.run();
                } catch (Throwable t) {
                    failure.set(t);
                } finally {
                    Affinity.setAffinity(original);
                }
            }
        };
        ct.start();
        ct.join(TimeUnit.SECONDS.toMillis(10));
        assertFalse(ct.isAlive(), "Cleaning thread did not terminate");
        assertNull(failure.get(), () -> "Cleaning thread failed: " + failure.get());
        assumeTrue(expectedPinned.equals(pinnedAffinity[0]), "Native affinity did not pin the child to the requested CPU");
        assertEquals(baseAffinity, nestedAffinity[0]);
        assertEquals(affinity, Affinity.getAffinity(), "Parent affinity changed");
    }
}
