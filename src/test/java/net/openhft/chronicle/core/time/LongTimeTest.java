/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LongTimeTest extends CoreTestCommon {
    @Test
    public void secs() {
        long epoch = LongTime.EPOCH_SECS;
        assertTrue(LongTime.isSecs(epoch), "secs: L15");
        assertTrue(LongTime.isSecs(LongTime.MAX_SECS), "secs: L16");
        assertFalse(LongTime.isSecs(LongTime.EPOCH_MILLIS), "secs: L17");
        assertTrue(LongTime.isSecs(LongTime.EPOCH_MILLIS / 1000), "secs: L18");
        assertEquals(epoch, LongTime.toSecs(LongTime.toSecs(epoch)), "secs: L19");
        assertEquals(epoch, LongTime.toSecs(LongTime.toMillis(epoch)), "secs: L20");
        assertEquals(epoch, LongTime.toSecs(LongTime.toMicros(epoch)), "secs: L21");
        assertEquals(epoch, LongTime.toSecs(LongTime.toNanos(epoch)), "secs: L22");
    }

    @Test
    public void millis() {
        long epoch = LongTime.EPOCH_MILLIS;
        assertTrue(LongTime.isMillis(epoch), "millis: L28");
        assertTrue(LongTime.isMillis(LongTime.MAX_MILLIS), "millis: L29");
        assertFalse(LongTime.isMillis(LongTime.EPOCH_MICROS), "millis: L30");
        assertTrue(LongTime.isMillis(LongTime.MAX_SECS * 1000), "millis: L31");
        assertTrue(LongTime.isMillis(LongTime.EPOCH_MICROS / 1000), "millis: L32");
        assertEquals(epoch - epoch % 1000, LongTime.toMillis(LongTime.toSecs(epoch)), "millis: L33");
        assertEquals(epoch, LongTime.toMillis(LongTime.toMillis(epoch)), "millis: L34");
        assertEquals(epoch, LongTime.toMillis(LongTime.toMicros(epoch)), "millis: L35");
        assertEquals(epoch, LongTime.toMillis(LongTime.toNanos(epoch)), "millis: L36");
    }

    @Test
    public void micros() {
        long epoch = LongTime.EPOCH_MICROS;
        assertTrue(LongTime.isMicros(epoch), "micros: L42");
        assertTrue(LongTime.isMicros(LongTime.MAX_MICROS), "micros: L43");
        assertFalse(LongTime.isMicros(LongTime.EPOCH_NANOS), "micros: L44");
        assertTrue(LongTime.isMicros(LongTime.MAX_MILLIS * 1000), "micros: L45");
        assertTrue(LongTime.isMicros(LongTime.EPOCH_NANOS / 1000), "micros: L46");
        assertEquals(epoch - epoch % 1000000, LongTime.toMicros(LongTime.toSecs(epoch)), "micros: L47");
        assertEquals(epoch - epoch % 1000, LongTime.toMicros(LongTime.toMillis(epoch)), "micros: L48");
        assertEquals(epoch, LongTime.toMicros(LongTime.toMicros(epoch)), "micros: L49");
        assertEquals(epoch, LongTime.toMicros(LongTime.toNanos(epoch)), "micros: L50");
    }

    @Test
    public void nanos() {
        long epoch = LongTime.EPOCH_NANOS;
        assertTrue(LongTime.isNanos(epoch), "nanos: L56");
        assertTrue(LongTime.isNanos(LongTime.MAX_NANOS), "nanos: L57");
        assertTrue(LongTime.isNanos(LongTime.MAX_MICROS * 1000), "nanos: L58");
        assertEquals(epoch - epoch % 1000000000, LongTime.toNanos(LongTime.toSecs(epoch)), "nanos: L59");
        assertEquals(epoch - epoch % 1000000, LongTime.toNanos(LongTime.toMillis(epoch)), "nanos: L60");
        assertEquals(epoch - epoch % 1000, LongTime.toNanos(LongTime.toMicros(epoch)), "nanos: L61");
        assertEquals(epoch, LongTime.toNanos(LongTime.toNanos(epoch)), "nanos: L62");
    }
}
