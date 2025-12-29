/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongTimeTest extends CoreTestCommon {
    @DisplayName("secs behaviour under expected input and output conditions")
    @Test
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

    @DisplayName("millis behaviour under expected input and output conditions")
    @Test
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

    @DisplayName("micros behaviour under expected input and output conditions")
    @Test
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

    @DisplayName("nanos behaviour under expected input and output conditions")
    @Test
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
}
