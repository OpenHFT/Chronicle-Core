/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SetTimeProviderTest extends CoreTestCommon {

    @Test
    void testNoOpConstructor() throws IllegalArgumentException {
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
    void testNanosConstructor() throws IllegalArgumentException {
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
    void testNanosConstructorLowNumber() {
        // many customers use "wrong" values
        final SetTimeProvider tp = new SetTimeProvider(1_000L);
        assertEquals(1_000L, tp.currentTimeNanos());
    }

    @Test
    void testAttemptToGoBackwardsNanos() {
        assertThrows(IllegalArgumentException.class, () -> {
            final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
            tp.currentTimeNanos(99_999_999_999L);
        });
    }

    @Test
    void testAttemptToGoBackwardsMicros() {
        assertThrows(IllegalArgumentException.class, () -> {
            final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
            tp.currentTimeMicros(99_999_999L);
        });
    }

    @Test
    void testAttemptToGoBackwardsMillis() {
        assertThrows(IllegalArgumentException.class, () -> {
            final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
            tp.currentTimeMillis(99_999L);
        });
    }

    @Test
    void withTimestamp() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075");
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider("2018-08-20T12:53:04.075123");
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    void withInstant() {
        SetTimeProvider tp = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075Z"));
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584075000L, tp.currentTimeMicros());
        SetTimeProvider tp2 = new SetTimeProvider(Instant.parse("2018-08-20T12:53:04.075123Z"));
        assertEquals(1534769584075L, tp2.currentTimeMillis());
        assertEquals(1534769584075123L, tp2.currentTimeMicros());
    }

    @Test
    void autoIncrement() {
        SetTimeProvider tp = new SetTimeProvider("2018-08-20T12:53:04.075")
                .autoIncrement(1, TimeUnit.MILLISECONDS);
        assertEquals(1534769584075L, tp.currentTimeMillis());
        assertEquals(1534769584076L, tp.currentTimeMillis());
        assertEquals(1534769584077L, tp.currentTimeMillis());

    }
}
