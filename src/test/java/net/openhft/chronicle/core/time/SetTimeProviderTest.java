/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class SetTimeProviderTest extends CoreTestCommon {

    @DisplayName("testNoOpConstructor behaviour under expected input and output conditions")
    @Test
    void testNoOpConstructor() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider();
        assertEquals(0, tp.currentTimeNanos(), "default constructor should initialize time to zero");
        tp.currentTimeNanos(99_123_456_789L);
        assertEquals(99_123_456_789L, tp.currentTimeNanos(), "current time in nanos should match the explicitly set value");
        assertEquals(99_123_456L, tp.currentTimeMicros(), "current time in micros should truncate nanos correctly");
        assertEquals(99_123L, tp.currentTimeMillis(), "current time in millis should truncate nanos correctly");
        assertEquals(99, tp.currentTime(TimeUnit.SECONDS), "current time in seconds should truncate nanos correctly");
        tp.advanceMillis(7).advanceMicros(5).advanceNanos(3);
        assertEquals(99_130_461_792L, tp.currentTimeNanos(), "time should advance by cumulative milliseconds, microseconds, and nanoseconds");
    }

    @DisplayName("testNanosConstructor behaviour under expected input and output conditions")
    @Test
    void testNanosConstructor() throws IllegalArgumentException {
        final SetTimeProvider tp = new SetTimeProvider(99_999_999_999_000_000L);
        assertEquals(99_999_999_999_000_000L, tp.currentTimeNanos(), "constructor should initialize time to specified nanoseconds");
        assertEquals(99_999_999_999_000L, tp.currentTimeMicros(), "initial time in micros should correctly truncate constructor nanos value");
        assertEquals(99_999_999_999L, tp.currentTimeMillis(), "initial time in millis should correctly truncate constructor nanos value");
        assertEquals(99_999_999, tp.currentTime(TimeUnit.SECONDS), "initial time in seconds should correctly truncate constructor nanos value");

        tp.currentTimeMicros(100_000_000_000_000L);
        assertEquals(100_000_000_000_000_000L, tp.currentTimeNanos(), "setting time in micros should convert to nanos preserving precision");
        assertEquals(100_000_000_000_000L, tp.currentTimeMicros(), "time in micros should match the value set via currentTimeMicros");
        assertEquals(100_000_000_000L, tp.currentTimeMillis(), "time in millis should truncate from micros-set value");
        assertEquals(100_000_000, tp.currentTime(TimeUnit.SECONDS), "time in seconds should truncate from micros-set value");

        tp.currentTimeMillis(101_987_000_000L);
        assertEquals(101_987_000_000_000_000L, tp.currentTimeNanos(), "setting time in millis should convert to nanos with zero sub-millis precision");
        assertEquals(101_987_000_000_000L, tp.currentTimeMicros(), "time in micros should convert from millis-set value with zero sub-millis precision");
        assertEquals(101_987_000_000L, tp.currentTimeMillis(), "time in millis should match the value set via currentTimeMillis");
        assertEquals(101_987_000, tp.currentTime(TimeUnit.SECONDS), "time in seconds should truncate from millis-set value");
        tp.advanceMillis(1_011).advanceMicros(1_211).advanceNanos(789_123);
        assertEquals(101_987_001_013_000_123L, tp.currentTimeNanos(), "time should advance by chained milliseconds, microseconds, and nanoseconds increments");
    }

    @DisplayName("testNanosConstructorLowNumber behaviour under expected input and output conditions")
    @Test
    void testNanosConstructorLowNumber() {
        // many customers use "wrong" values
        final SetTimeProvider tp = new SetTimeProvider(1_000L);
        assertEquals(1_000L, tp.currentTimeNanos(), "constructor should accept small nanosecond values without validation");
    }

    @DisplayName("testAttemptToGoBackwardsNanos behaviour under expected input and output conditions")
    @Test
    void testAttemptToGoBackwardsNanos() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class,
                () -> tp.currentTimeNanos(99_999_999_999L),
                "setting time backwards in nanos should throw IllegalArgumentException");
    }

    @DisplayName("testAttemptToGoBackwardsMicros behaviour under expected input and output conditions")
    @Test
    void testAttemptToGoBackwardsMicros() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class,
                () -> tp.currentTimeMicros(99_999_999L),
                "setting time backwards in micros should throw IllegalArgumentException");
    }

    @DisplayName("testAttemptToGoBackwardsMillis behaviour under expected input and output conditions")
    @Test
    void testAttemptToGoBackwardsMillis() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class,
                () -> tp.currentTimeMillis(99_999L),
                "setting time backwards in millis should throw IllegalArgumentException");
    }

    @DisplayName("withTimestamp behaviour under expected input and output conditions")
    @Test
    void withTimestamp() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075");
        assertEquals(1534769584075L, tp.currentTimeMillis(), "timestamp constructor should parse millisecond precision correctly");
        assertEquals(1534769584075000L, tp.currentTimeMicros(), "timestamp constructor should convert millisecond precision to micros with zero sub-millis");
        SetTimeProvider tp2 = new SetTimeProvider("2018-08-20T12:53:04.075123");
        assertEquals(1534769584075L, tp2.currentTimeMillis(), "timestamp constructor with microsecond precision should truncate to millis correctly");
        assertEquals(1534769584075123L, tp2.currentTimeMicros(), "timestamp constructor should parse microsecond precision correctly");
    }

    @DisplayName("withInstant behaviour under expected input and output conditions")
    @Test
    void withInstant() {
        SetTimeProvider tp = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075Z"));
        assertEquals(1534769584075L, tp.currentTimeMillis(), "instant constructor should parse millisecond precision correctly");
        assertEquals(1534769584075000L, tp.currentTimeMicros(), "instant constructor should convert millisecond precision to micros with zero sub-millis");
        SetTimeProvider tp2 = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075123Z"));
        assertEquals(1534769584075L, tp2.currentTimeMillis(), "instant constructor with microsecond precision should truncate to millis correctly");
        assertEquals(1534769584075123L, tp2.currentTimeMicros(), "instant constructor should parse microsecond precision correctly");
    }

    @DisplayName("autoIncrement behaviour under expected input and output conditions")
    @Test
    void autoIncrement() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075")
                .autoIncrement(1, TimeUnit.MILLISECONDS);
        assertEquals(1534769584075L, tp.currentTimeMillis(), "first read should return initial time value");
        assertEquals(1534769584076L, tp.currentTimeMillis(), "second read should auto-increment by one millisecond");
        assertEquals(1534769584077L, tp.currentTimeMillis(), "third read should auto-increment by another millisecond");

    }

    @DisplayName("invalidTimestampFormatThrows behaviour under expected input and output conditions")
    @Test
    void invalidTimestampFormatThrows() {
        assertThrows(DateTimeParseException.class,
                () -> new SetTimeProvider("2018/08/20 12:53:04"),
                "constructor should reject invalid timestamp format");
    }

    @DisplayName("autoIncrementIsMonotonicAcrossThreads behaviour under expected input and output conditions")
    @Test
    void autoIncrementIsMonotonicAcrossThreads() throws InterruptedException {
        SetTimeProvider tp = new SetTimeProvider(0).autoIncrement(1, TimeUnit.MICROSECONDS);
        int threads = 4;
        int readsPerThread = 50;
        long[] values = new long[threads * readsPerThread];
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            pool.execute(() -> {
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
            assertTrue(copy[i] > copy[i - 1],
                    "auto-increment with concurrent reads should produce strictly monotonic increasing values i=" + i);
        }
    }

    @DisplayName("advanceAllowsNegativeOffsets behaviour under expected input and output conditions")
    @Test
    void advanceAllowsNegativeOffsets() {
        SetTimeProvider tp = new SetTimeProvider(2_000_000);
        tp.advanceMillis(-1).advanceMicros(-500).advanceNanos(250);
        long expected = 2_000_000 - 1_000_000 - 500_000 + 250;
        assertEquals(expected, tp.currentTimeNanos(), "advance methods should support negative offsets to move time backwards");
    }

    @DisplayName("currentTimeConversionUsesExactUnits behaviour under expected input and output conditions")
    @Test
    void currentTimeConversionUsesExactUnits() {
        SetTimeProvider tp = new SetTimeProvider(123_456_789_123L);
        assertEquals(123_456_789L, tp.currentTimeMicros(), "conversion from nanos to micros should truncate correctly");
        assertEquals(123_456L, tp.currentTimeMillis(), "conversion from nanos to millis should truncate correctly");
        assertEquals(123L, tp.currentTime(TimeUnit.SECONDS), "conversion from nanos to seconds should truncate correctly");
    }
}
