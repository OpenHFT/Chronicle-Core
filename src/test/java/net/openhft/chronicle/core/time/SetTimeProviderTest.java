//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetTimeProviderTest extends CoreTestCommon {

    @Test
    public void testNoOpConstructor() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider();
        assertEquals(0, tp.currentTimeNanos());
        tp.currentTimeNanos(99_123_456_789L);
        assertEquals(99_123_456_789L, tp.currentTimeNanos());
        assertEquals(99_123_456L, tp.currentTimeMicros());
        assertEquals(99_123L, tp.currentTimeMillis());
        assertEquals(99, tp.currentTime(TimeUnit.SECONDS));
        tp.advanceMillis(7).advanceMicros(5).advanceNanos(3);
        assertEquals(99_130_461_792L, tp.currentTimeNanos());
    }

    @Test
    public void testNanosConstructor() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider(99_999_999_999_000_000L);
        assertEquals(99_999_999_999_000_000L, tp.currentTimeNanos());
        assertEquals(99_999_999_999_000L, tp.currentTimeMicros());
        assertEquals(99_999_999_999L, tp.currentTimeMillis());
        assertEquals(99_999_999, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMicros(100_000_000_000_000L);
        assertEquals(100_000_000_000_000_000L, tp.currentTimeNanos());
        assertEquals(100_000_000_000_000L, tp.currentTimeMicros());
        assertEquals(100_000_000_000L, tp.currentTimeMillis());
        assertEquals(100_000_000, tp.currentTime(TimeUnit.SECONDS));

        tp.currentTimeMillis(101_987_000_000L);
        assertEquals(101_987_000_000_000_000L, tp.currentTimeNanos());
        assertEquals(101_987_000_000_000L, tp.currentTimeMicros());
        assertEquals(101_987_000_000L, tp.currentTimeMillis());
        assertEquals(101_987_000, tp.currentTime(TimeUnit.SECONDS));
        tp.advanceMillis(1_011).advanceMicros(1_211).advanceNanos(789_123);
        assertEquals(101_987_001_013_000_123L, tp.currentTimeNanos());
    }

    @Test
    public void testNanosConstructorLowNumber() {
        // many customers use "wrong" values
        final SetTimeProvider tp = new SetTimeProvider(1_000L);
        assertEquals(1_000L, tp.currentTimeNanos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsNanos() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeNanos(99_999_999_999L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsMicros() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeMicros(99_999_999L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttemptToGoBackwardsMillis() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        tp.currentTimeMillis(99_999L);
    }

    @Test
    public void withTimestamp() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075");
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider("2018-08-20T12:53:04.075123");
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    public void withInstant() {
        SetTimeProvider tp = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075Z"));
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075123Z"));
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    public void autoIncrement() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075")
                .autoIncrement(1, TimeUnit.MILLISECONDS);
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584076L, tp.currentTimeMillis());
        assertEquals(1534769584077L, tp.currentTimeMillis());

    }

    @Test(expected = DateTimeParseException.class)
    public void invalidTimestampFormatThrows() {
        new SetTimeProvider("2018/08/20 12:53:04"); // missing T separator -> should fail
    }

    @Test
    public void autoIncrementIsMonotonicAcrossThreads() throws InterruptedException {
        SetTimeProvider tp = new SetTimeProvider(0).autoIncrement(1, TimeUnit.MICROSECONDS);
        int threads = 4;
        int readsPerThread = 50;
        long[] values = new long[threads * readsPerThread];
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < readsPerThread; i++) {
                        values[threadIndex * readsPerThread + i] = tp.currentTimeNanos();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();
        long[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        for (int i = 1; i < copy.length; i++) {
            assertTrue("time not strictly increasing", copy[i] > copy[i - 1]);
        }
    }

    @Test
    public void advanceAllowsNegativeOffsets() {
        SetTimeProvider tp = new SetTimeProvider(2_000_000);
        tp.advanceMillis(-1).advanceMicros(-500).advanceNanos(250);
        long expected = 2_000_000 - 1_000_000 - 500_000 + 250;
        assertEquals(expected, tp.currentTimeNanos());
    }

    @Test
    public void currentTimeConversionUsesExactUnits() {
        SetTimeProvider tp = new SetTimeProvider(123_456_789_123L);
        assertEquals(123_456_789L, tp.currentTimeMicros());
        assertEquals(123_456L, tp.currentTimeMillis());
        assertEquals(123L, tp.currentTime(TimeUnit.SECONDS));
    }
}
