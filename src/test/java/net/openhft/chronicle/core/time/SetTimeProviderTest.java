/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
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
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class, () -> tp.currentTimeNanos(99_999_999_999L));
    }

    @Test
    void testAttemptToGoBackwardsMicros() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class, () -> tp.currentTimeMicros(99_999_999L));
    }

    @Test
    void testAttemptToGoBackwardsMillis() {
        final SetTimeProvider tp = new SetTimeProvider(100_000_000_000L);
        assertThrows(IllegalArgumentException.class, () -> tp.currentTimeMillis(99_999L));
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
    void withIsoInstantText() {
        assertEquals(1767225600000L, new SetTimeProvider("2026-01-01T00:00:00Z").currentTimeMillis());
        assertEquals(1534769584075L, new SetTimeProvider("2018-08-20T12:53:04.075Z").currentTimeMillis());
        assertEquals(1534769584075123L, new SetTimeProvider("2018-08-20T12:53:04.075123Z").currentTimeMicros());
        assertEquals(1534769584075123456L, new SetTimeProvider("2018-08-20T12:53:04.075123456Z").currentTimeNanos());
        assertEquals(1534769584075L, new SetTimeProvider("2018/08/20T12:53:04.075Z").currentTimeMillis());
    }

    @Test
    void withOffsetText() {
        assertEquals(1534769584075L, new SetTimeProvider("2018-08-20T13:53:04.075+01:00").currentTimeMillis());
        assertEquals(1534769584075L, new SetTimeProvider("2018-08-20T07:53:04.075-05:00").currentTimeMillis());
        assertEquals(1534769584075L, new SetTimeProvider("2018-08-20T12:53:04.075+00:00").currentTimeMillis());
    }

    @Test
    void withZonedDateTimeText() {
        final ZonedDateTime newYork = Instant.ofEpochMilli(1534769584075L).atZone(ZoneId.of("America/New_York"));
        assertEquals(1534769584075L, new SetTimeProvider(newYork.toString()).currentTimeMillis());
        assertEquals(1534769584075L, new SetTimeProvider("2018/08/20T13:53:04.075+01:00[Europe/London]").currentTimeMillis());
    }

    @Test
    void rejectsInvalidTimestampText() {
        for (String text : new String[]{"2018-08-20", "2018-08-20T25:00:00Z", "2018-08-20T12:53:04+19:00",
                "2018-08-20T12:53:04ZZ", "2018-08-20T12:53:04[Europe/London]", "yesterday"})
            assertThrows(DateTimeParseException.class, () -> new SetTimeProvider(text), text);
    }

    @Test
    void nanosecondRangeIsCheckedForBothConstructors() {
        Instant min = Instant.ofEpochSecond(-9223372037L, 145224192);
        Instant max = Instant.ofEpochSecond(9223372036L, 854775807);
        assertEquals(Long.MIN_VALUE, new SetTimeProvider(min).currentTimeNanos());
        assertEquals(Long.MIN_VALUE, new SetTimeProvider(min.toString()).currentTimeNanos());
        assertEquals(Long.MAX_VALUE, new SetTimeProvider(max).currentTimeNanos());
        assertEquals(Long.MAX_VALUE, new SetTimeProvider(max.toString()).currentTimeNanos());
        for (Instant outside : new Instant[]{min.minusNanos(1), max.plusNanos(1), Instant.MIN, Instant.MAX}) {
            assertThrows(ArithmeticException.class, () -> new SetTimeProvider(outside));
        }
        for (Instant outside : new Instant[]{min.minusNanos(1), max.plusNanos(1)})
            assertThrows(ArithmeticException.class, () -> new SetTimeProvider(outside.toString()));
        assertEquals(-1, new SetTimeProvider("1969-12-31T23:59:59.999999999Z").currentTimeNanos());
    }

    @Test
    void explicitOffsetWinsOverConflictingZoneRules() {
        assertEquals(new SetTimeProvider("2026-01-01T00:00:00Z").currentTimeNanos(),
                new SetTimeProvider("2026-01-01T00:00:00Z[America/New_York]").currentTimeNanos());
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
