/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongTimeTest extends CoreTestCommon {
    @Test
    @DisplayName("Seconds conversion detects unit and preserves epoch")
    void secs() {
        long epoch = LongTime.EPOCH_SECS;
        assertTrue(LongTime.isSecs(epoch), "EPOCH_SECS should be identified as seconds");
        assertTrue(LongTime.isSecs(LongTime.MAX_SECS), "MAX_SECS should be identified as seconds");
        assertFalse(LongTime.isSecs(LongTime.EPOCH_MILLIS), "EPOCH_MILLIS should not be identified as seconds");
        assertTrue(LongTime.isSecs(LongTime.EPOCH_MILLIS / 1000), "EPOCH_MILLIS divided by 1000 should be identified as seconds");
        assertEquals(epoch, LongTime.toSecs(LongTime.toSecs(epoch)), "toSecs should be idempotent when applied to seconds");
        assertEquals(epoch, LongTime.toSecs(LongTime.toMillis(epoch)), "toSecs should convert milliseconds back to original seconds");
        assertEquals(epoch, LongTime.toSecs(LongTime.toMicros(epoch)), "toSecs should convert microseconds back to original seconds");
        assertEquals(epoch, LongTime.toSecs(LongTime.toNanos(epoch)), "toSecs should convert nanoseconds back to original seconds");
    }

    @Test
    @DisplayName("Millis conversion detects unit and truncates precision")
    void millis() {
        long epoch = LongTime.EPOCH_MILLIS;
        assertTrue(LongTime.isMillis(epoch), "EPOCH_MILLIS should be identified as milliseconds");
        assertTrue(LongTime.isMillis(LongTime.MAX_MILLIS), "MAX_MILLIS should be identified as milliseconds");
        assertFalse(LongTime.isMillis(LongTime.EPOCH_MICROS), "EPOCH_MICROS should not be identified as milliseconds");
        assertTrue(LongTime.isMillis(LongTime.MAX_SECS * 1000), "MAX_SECS multiplied by 1000 should be identified as milliseconds");
        assertTrue(LongTime.isMillis(LongTime.EPOCH_MICROS / 1000), "EPOCH_MICROS divided by 1000 should be identified as milliseconds");
        assertEquals(epoch - epoch % 1000, LongTime.toMillis(LongTime.toSecs(epoch)), "toMillis should truncate subsecond precision when converting from seconds");
        assertEquals(epoch, LongTime.toMillis(LongTime.toMillis(epoch)), "toMillis should be idempotent when applied to milliseconds");
        assertEquals(epoch, LongTime.toMillis(LongTime.toMicros(epoch)), "toMillis should convert microseconds back to original milliseconds");
        assertEquals(epoch, LongTime.toMillis(LongTime.toNanos(epoch)), "toMillis should convert nanoseconds back to original milliseconds");
    }

    @Test
    @DisplayName("Micros conversion detects unit and truncates precision")
    void micros() {
        long epoch = LongTime.EPOCH_MICROS;
        assertTrue(LongTime.isMicros(epoch), "EPOCH_MICROS should be identified as microseconds");
        assertTrue(LongTime.isMicros(LongTime.MAX_MICROS), "MAX_MICROS should be identified as microseconds");
        assertFalse(LongTime.isMicros(LongTime.EPOCH_NANOS), "EPOCH_NANOS should not be identified as microseconds");
        assertTrue(LongTime.isMicros(LongTime.MAX_MILLIS * 1000), "MAX_MILLIS multiplied by 1000 should be identified as microseconds");
        assertTrue(LongTime.isMicros(LongTime.EPOCH_NANOS / 1000), "EPOCH_NANOS divided by 1000 should be identified as microseconds");
        assertEquals(epoch - epoch % 1000000, LongTime.toMicros(LongTime.toSecs(epoch)), "toMicros should truncate subsecond precision when converting from seconds");
        assertEquals(epoch - epoch % 1000, LongTime.toMicros(LongTime.toMillis(epoch)), "toMicros should truncate submillisecond precision when converting from milliseconds");
        assertEquals(epoch, LongTime.toMicros(LongTime.toMicros(epoch)), "toMicros should be idempotent when applied to microseconds");
        assertEquals(epoch, LongTime.toMicros(LongTime.toNanos(epoch)), "toMicros should convert nanoseconds back to original microseconds");
    }

    @Test
    @DisplayName("Nanos conversion detects unit and truncates precision")
    void nanos() {
        long epoch = LongTime.EPOCH_NANOS;
        assertTrue(LongTime.isNanos(epoch), "EPOCH_NANOS should be identified as nanoseconds");
        assertTrue(LongTime.isNanos(LongTime.MAX_NANOS), "MAX_NANOS should be identified as nanoseconds");
        assertTrue(LongTime.isNanos(LongTime.MAX_MICROS * 1000), "MAX_MICROS multiplied by 1000 should be identified as nanoseconds");
        assertEquals(epoch - epoch % 1000000000, LongTime.toNanos(LongTime.toSecs(epoch)), "toNanos should truncate subsecond precision when converting from seconds");
        assertEquals(epoch - epoch % 1000000, LongTime.toNanos(LongTime.toMillis(epoch)), "toNanos should truncate submillisecond precision when converting from milliseconds");
        assertEquals(epoch - epoch % 1000, LongTime.toNanos(LongTime.toMicros(epoch)), "toNanos should truncate submicrosecond precision when converting from microseconds");
        assertEquals(epoch, LongTime.toNanos(LongTime.toNanos(epoch)), "toNanos should be idempotent when applied to nanoseconds");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("toSecs handles negative values")
    void toSecsNegative() {
        long negative = -1000L;
        assertEquals(negative, LongTime.toSecs(negative),
                "toSecs should return negative value unchanged");
    }

    @Test
    @DisplayName("toSecs handles different time units")
    void toSecsAllUnits() {
        // Seconds range
        long secs = 1234567890L;
        assertEquals(secs, LongTime.toSecs(secs), "toSecs should keep seconds unchanged");

        // Millis range
        long millis = LongTime.EPOCH_MILLIS + 1000;
        assertEquals(millis / 1000, LongTime.toSecs(millis), "toSecs should convert millis to secs");

        // Micros range
        long micros = LongTime.EPOCH_MICROS + 1_000_000;
        assertEquals(micros / 1_000_000, LongTime.toSecs(micros), "toSecs should convert micros to secs");

        // Nanos range
        long nanos = LongTime.EPOCH_NANOS + 1_000_000_000;
        assertEquals(nanos / 1_000_000_000, LongTime.toSecs(nanos), "toSecs should convert nanos to secs");
    }

    @Test
    @DisplayName("toMillis handles negative values")
    void toMillisNegative() {
        long negative = -1000L;
        assertEquals(negative, LongTime.toMillis(negative),
                "toMillis should return negative value unchanged");
    }

    @Test
    @DisplayName("toMillis handles all time unit ranges")
    void toMillisAllUnits() {
        // Seconds to millis
        long secs = 100L;
        assertEquals(secs * 1000, LongTime.toMillis(secs), "toMillis should convert secs to millis");

        // Millis returns millis
        long millis = LongTime.EPOCH_MILLIS + 500;
        assertEquals(millis, LongTime.toMillis(millis), "toMillis should keep millis unchanged");

        // Micros to millis
        long micros = LongTime.EPOCH_MICROS + 5000;
        assertEquals(micros / 1000, LongTime.toMillis(micros), "toMillis should convert micros to millis");

        // Nanos to millis
        long nanos = LongTime.EPOCH_NANOS + 5_000_000;
        assertEquals(nanos / 1_000_000, LongTime.toMillis(nanos), "toMillis should convert nanos to millis");
    }

    @Test
    @DisplayName("toMicros handles negative values")
    void toMicrosNegative() {
        long negative = -1000L;
        assertEquals(negative, LongTime.toMicros(negative),
                "toMicros should return negative value unchanged");
    }

    @Test
    @DisplayName("toMicros handles all time unit ranges")
    void toMicrosAllUnits() {
        // Seconds to micros
        long secs = 100L;
        assertEquals(secs * 1_000_000, LongTime.toMicros(secs), "toMicros should convert secs to micros");

        // Millis to micros
        long millis = LongTime.EPOCH_MILLIS + 500;
        assertEquals(millis * 1000, LongTime.toMicros(millis), "toMicros should convert millis to micros");

        // Micros returns micros
        long micros = LongTime.EPOCH_MICROS + 500;
        assertEquals(micros, LongTime.toMicros(micros), "toMicros should keep micros unchanged");

        // Nanos to micros
        long nanos = LongTime.EPOCH_NANOS + 5000;
        assertEquals(nanos / 1000, LongTime.toMicros(nanos), "toMicros should convert nanos to micros");
    }

    @Test
    @DisplayName("toNanos handles negative values")
    void toNanosNegative() {
        long negative = -1000L;
        assertEquals(negative, LongTime.toNanos(negative),
                "toNanos should return negative value unchanged");
    }

    @Test
    @DisplayName("toNanos handles all time unit ranges")
    void toNanosAllUnits() {
        // Seconds to nanos
        long secs = 100L;
        assertEquals(secs * 1_000_000_000, LongTime.toNanos(secs), "toNanos should convert secs to nanos");

        // Millis to nanos
        long millis = LongTime.EPOCH_MILLIS + 500;
        assertEquals(millis * 1_000_000, LongTime.toNanos(millis), "toNanos should convert millis to nanos");

        // Micros to nanos
        long micros = LongTime.EPOCH_MICROS + 500;
        assertEquals(micros * 1000, LongTime.toNanos(micros), "toNanos should convert micros to nanos");

        // Nanos returns nanos
        long nanos = LongTime.EPOCH_NANOS + 500;
        assertEquals(nanos, LongTime.toNanos(nanos), "toNanos should keep nanos unchanged");
    }

    @Test
    @DisplayName("Boundary values for isSecs")
    void isSecsBoundary() {
        assertFalse(LongTime.isSecs(-1L), "negative should not be identified as secs");
        assertTrue(LongTime.isSecs(0L), "zero should be identified as secs");
        assertTrue(LongTime.isSecs(LongTime.MAX_SECS), "MAX_SECS should be identified as secs");
        assertFalse(LongTime.isSecs(LongTime.MAX_SECS + 1), "MAX_SECS+1 should not be identified as secs");
    }

    @Test
    @DisplayName("Boundary values for isMillis")
    void isMillisBoundary() {
        assertFalse(LongTime.isMillis(LongTime.EPOCH_MILLIS - 1), "below EPOCH_MILLIS should not be millis");
        assertTrue(LongTime.isMillis(LongTime.EPOCH_MILLIS), "EPOCH_MILLIS should be millis");
        assertTrue(LongTime.isMillis(LongTime.MAX_MILLIS), "MAX_MILLIS should be millis");
        assertFalse(LongTime.isMillis(LongTime.MAX_MILLIS + 1), "above MAX_MILLIS should not be millis");
    }

    @Test
    @DisplayName("Boundary values for isMicros")
    void isMicrosBoundary() {
        assertFalse(LongTime.isMicros(LongTime.EPOCH_MICROS - 1), "below EPOCH_MICROS should not be micros");
        assertTrue(LongTime.isMicros(LongTime.EPOCH_MICROS), "EPOCH_MICROS should be micros");
        assertTrue(LongTime.isMicros(LongTime.MAX_MICROS), "MAX_MICROS should be micros");
        assertFalse(LongTime.isMicros(LongTime.MAX_MICROS + 1), "above MAX_MICROS should not be micros");
    }

    @Test
    @DisplayName("Boundary values for isNanos")
    void isNanosBoundary() {
        assertFalse(LongTime.isNanos(LongTime.EPOCH_NANOS - 1), "below EPOCH_NANOS should not be nanos");
        assertTrue(LongTime.isNanos(LongTime.EPOCH_NANOS), "EPOCH_NANOS should be nanos");
        assertTrue(LongTime.isNanos(Long.MAX_VALUE), "MAX_VALUE should be nanos");
    }
}
