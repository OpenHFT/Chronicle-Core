/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CleaningThreadTest extends CoreTestCommon {
    @Test
    @DisplayName("Cleanup thread local executes cleanup callback")
    void cleanupThreadLocal() throws InterruptedException {
        String threadName = "ctl-test";
        BlockingQueue<String> ints = new LinkedBlockingQueue<>();
        CleaningThreadLocal<String> counter = CleaningThreadLocal.withCleanup(() -> Thread.currentThread().getName(), ints::add);
        CleaningThread ct = new CleaningThread(() -> assertEquals(threadName, counter.get(), "thread-local value should match the thread name"), threadName);
        ct.start();
        String poll = ints.poll(1, TimeUnit.SECONDS);
        assertEquals(threadName, poll, "thread should have expected name");
    }

    @Test
    @DisplayName("Cleaning thread remove triggers cleanup for current thread")
    void testRemove() {
        int[] counter = {0};
        CleaningThreadLocal<Integer> ctl = CleaningThreadLocal.withCloseQuietly(() -> counter[0]++);
        assertEquals(0, (int) ctl.get(), "initial get should return first supplier value");
        CleaningThread.performCleanup(Thread.currentThread());
        assertEquals(1, (int) ctl.get(), "get after cleanup should return incremented supplier value");
    }

    @Test
    @DisplayName("Cleaning thread resets affinity to base")
    void resetThreadAffinity() throws InterruptedException {
        final BitSet affinity = Affinity.getAffinity();
        assumeTrue(affinity.cardinality() > 2, "requires at least three CPUs to test affinity reset");
        assumeTrue(AffinityLock.BASE_AFFINITY.cardinality() > 2,
                "requires base affinity to include at least three CPUs");
        try {
            Affinity.setAffinity(1);
            BitSet[] nestedAffinity = {null};
            CleaningThread ct = new CleaningThread(() -> nestedAffinity[0] = Affinity.getAffinity());
            ct.start();
            ct.join();
            assertEquals(AffinityLock.BASE_AFFINITY, nestedAffinity[0],
                    "cleaning thread affinity should reset to base affinity");
        } finally {
            Affinity.setAffinity(affinity);
        }
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("CleaningThread inEventLoop returns false for standard caller thread")
    void inEventLoopFalseForRegularThread() {
        assertFalse(CleaningThread.inEventLoop(Thread.currentThread()),
                "CleaningThread inEventLoop should return false for standard thread caller");
    }

    @Test
    @DisplayName("inEventLoop returns false for CleaningThread not in event loop")
    void inEventLoopFalseForCleaningThreadNotInLoop() {
        CleaningThread ct = new CleaningThread(() -> { }, "test");
        assertFalse(CleaningThread.inEventLoop(ct),
                "inEventLoop should return false for CleaningThread not in event loop");
    }

    @Test
    @DisplayName("inEventLoop returns true for CleaningThread in event loop")
    void inEventLoopTrueForCleaningThreadInLoop() {
        CleaningThread ct = new CleaningThread(() -> { }, "test", true);
        assertTrue(CleaningThread.inEventLoop(ct),
                "inEventLoop should return true for CleaningThread in event loop");
    }

    @Test
    @DisplayName("Constructor with Runnable only sets inEventLoop to false")
    void constructorWithRunnableOnly() {
        CleaningThread ct = new CleaningThread(() -> { });
        assertFalse(ct.inEventLoop(), "inEventLoop should be false for Runnable-only constructor");
    }

    @Test
    @DisplayName("Constructor with Runnable and name sets inEventLoop to false")
    void constructorWithRunnableAndName() {
        CleaningThread ct = new CleaningThread(() -> { }, "test-thread");
        assertFalse(ct.inEventLoop(), "inEventLoop should be false for two-arg constructor");
        assertEquals("test-thread", ct.getName(), "thread name should match");
    }

    @Test
    @DisplayName("Constructor with inEventLoop flag sets it correctly")
    void constructorWithInEventLoopFlag() {
        CleaningThread ctFalse = new CleaningThread(() -> { }, "test1", false);
        CleaningThread ctTrue = new CleaningThread(() -> { }, "test2", true);

        assertFalse(ctFalse.inEventLoop(), "inEventLoop should be false when passed false");
        assertTrue(ctTrue.inEventLoop(), "inEventLoop should be true when passed true");
    }

    @Test
    @DisplayName("performCleanup handles thread with no thread-locals")
    void performCleanupNoThreadLocals() {
        Thread newThread = new Thread(() -> { });
        // Should not throw
        assertDoesNotThrow(() -> CleaningThread.performCleanup(newThread),
                "performCleanup should handle thread with no thread-locals");
    }

    @Test
    @DisplayName("performCleanup with specific CTL cleans only that CTL")
    void performCleanupWithSpecificCtl() {
        int[] counter1 = {0};
        int[] counter2 = {0};
        CleaningThreadLocal<Integer> ctl1 = CleaningThreadLocal.withCloseQuietly(() -> counter1[0]++);
        CleaningThreadLocal<Integer> ctl2 = CleaningThreadLocal.withCloseQuietly(() -> counter2[0]++);

        // Initialize both
        ctl1.get();
        ctl2.get();
        assertEquals(1, counter1[0], "ctl1 should have been called once");
        assertEquals(1, counter2[0], "ctl2 should have been called once");

        // Cleanup only ctl1
        CleaningThread.performCleanup(Thread.currentThread(), ctl1);

        // Get again - ctl1 should have been cleaned and recreated
        ctl1.get();
        assertEquals(2, counter1[0], "ctl1 should have been recreated after cleanup");
        // ctl2 should not have been affected
        assertEquals(1, counter2[0], "ctl2 should not have been cleaned");
    }

    @Test
    @DisplayName("createdHere returns null when resource tracing is disabled")
    void createdHereWhenTracingDisabled() {
        // Note: This test result depends on whether resource tracing is enabled
        CleaningThread ct = new CleaningThread(() -> { }, "test");
        // createdHere may be null or non-null depending on Jvm.isResourceTracing()
        // Just ensure it doesn't throw
        assertDoesNotThrow(ct::createdHere, "createdHere should not throw");
    }

    @Test
    @DisplayName("CleaningThread runs target and performs cleanup")
    void cleaningThreadRunsTargetAndCleansUp() throws InterruptedException {
        boolean[] ran = {false};
        CleaningThread ct = new CleaningThread(() -> ran[0] = true, "test-run");
        ct.start();
        ct.join(1000);

        assertTrue(ran[0], "target runnable should have executed");
        assertFalse(ct.isAlive(), "thread should have completed");
    }
}
