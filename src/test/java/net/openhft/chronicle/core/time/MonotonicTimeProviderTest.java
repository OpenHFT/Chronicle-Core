/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MonotonicTimeProviderTest extends CoreTestCommon {
    @Test
    void retainsMaximumAfterWallClockMovesBackwardsThenResumes() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        long first = clock.currentTimeNanos(1_000_000_000L, 10_000L);
        assertEquals(10_000_000_000L, first);
        assertEquals(first, clock.currentTimeNanos(1_010_000_000L, 5_000L));
        assertEquals(first, clock.currentTimeNanos(2_010_000_000L, 6_000L));
        assertTrue(clock.currentTimeNanos(7_010_000_000L, 11_000L) > first);
    }

    @Test
    @Timeout(15)
    void retainsMaximumAcrossOrderedCallsOnDifferentThreads() throws Exception {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        long first = onNewThread(() -> clock.currentTimeNanos(1_000_000_000L, 10_000L));
        long second = onNewThread(() -> clock.currentTimeNanos(1_010_000_000L, 5_000L));
        assertEquals(10_000_000_000L, first);
        assertEquals(first, second, "a later caller must retain the other thread's maximum");
    }

    @Test
    void catchesUpWhenWallClockMovesForwards() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        assertEquals(10_000_000_000L, clock.currentTimeNanos(1_000_000_000L, 10_000L));
        assertEquals(20_000_000_000L, clock.currentTimeNanos(1_010_000_000L, 20_000L));
    }

    @Test
    void allowsDuplicatesAndClampsEstimateAheadOfWallClock() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        assertEquals(10_000_000_000L, clock.currentTimeNanos(1_000_000_000L, 10_000L));
        assertEquals(10_001_000_000L, clock.currentTimeNanos(1_002_000_000L, 10_000L));
        assertEquals(10_001_000_000L, clock.currentTimeNanos(1_002_001_000L, 10_000L));
    }

    @Test
    void supportsNegativeEpochSamples() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        assertEquals(-9_999_000_000L, clock.currentTimeNanos(1_000L, -10_000L));
        assertEquals(-9_998_999_000L, clock.currentTimeNanos(2_000L, -9_999L));
    }

    @Test
    void supportsNegativeNanoTimeOrigin() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        long first = clock.currentTimeNanos(-1_000_000_000L, 10_000L);
        assertEquals(10_000_000_000L, first);
        assertEquals(first + 1_000L, clock.currentTimeNanos(-999_999_000L, 10_000L));
    }

    @Test
    void preservesProgressAcrossNanoTimeSignWrap() {
        MonotonicTimeProvider.Clock clock = new MonotonicTimeProvider.Clock();
        long first = clock.currentTimeNanos(Long.MAX_VALUE - 1_000L, 10_000L);
        assertEquals(first + 2_001L, clock.currentTimeNanos(Long.MIN_VALUE + 1_000L, 10_001L));
    }

    @Test
    void microsPreserveTheNanosOrdering() {
        long before = MonotonicTimeProvider.INSTANCE.currentTimeNanos();
        long micros = MonotonicTimeProvider.INSTANCE.currentTimeMicros();
        long after = MonotonicTimeProvider.INSTANCE.currentTimeNanos();
        assertTrue(micros >= before / 1000);
        assertTrue(micros <= after / 1000);
    }

    private static long onNewThread(Callable<Long> call) throws Exception {
        FutureTask<Long> result = new FutureTask<>(call);
        Thread thread = new Thread(result, "ordered-time-caller");
        thread.start();
        try {
            return result.get(5, TimeUnit.SECONDS);
        } finally {
            thread.interrupt();
            thread.join(5_000);
            assertFalse(thread.isAlive(), "ordered caller did not stop");
        }
    }
}
