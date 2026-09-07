/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.longs.LongCondition;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests mirror the guard patterns used in peer Chronicle modules (e.g. Chronicle-Bytes)
 * to validate offsets, lengths and alignment before calling into native memory code paths.
 */
class LongsUsageTest {

    @Test
    void requireNonNegativeMatchesBytesStores() {
        // Chronicle-Bytes calls Longs.requireNonNegative before copying into native stores
        assertEquals(128L, Longs.requireNonNegative(128L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requireNonNegative(-4L));
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    void requirePositiveReflectsOSGuards() {
        // OS.map* methods demand strictly positive lengths
        assertEquals(4096L, Longs.requirePositive(4096L));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requirePositive(0L));
        assertTrue(ex.getMessage().contains("not positive"));
    }

    @Test
    void alignmentChecksMirrorNativeAccess() {
        long address = 1L << 16; // naturally aligned
        assertEquals(address, Longs.require(LongCondition.LONG_ALIGNED, address, IllegalArgumentException::new));

        IllegalArgumentException misAligned = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.LONG_ALIGNED, address + 3, IllegalArgumentException::new));
        assertTrue(misAligned.getMessage().contains(LongCondition.LONG_ALIGNED.toString()));
    }

    @Test
    void requireAppliesPredicateForByteConvertibleValues() {
        long withinByte = 120L;
        assertEquals(withinByte, Longs.require(LongCondition.BYTE_CONVERTIBLE, withinByte, IllegalArgumentException::new));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.BYTE_CONVERTIBLE, Byte.MAX_VALUE + 2L, IllegalArgumentException::new));
        assertTrue(ex.getMessage().contains(LongCondition.BYTE_CONVERTIBLE.toString()));
    }

    @Test
    void negateBehaviourMatchesDownstreamExpectations() {
        LongPredicate notNonPositive = LongCondition.NON_POSITIVE.negate();
        assertTrue(notNonPositive.test(7));
        assertFalse(notNonPositive.test(-1));

        LongPredicate notEvenPowerOfTwo = LongCondition.EVEN_POWER_OF_TWO.negate();
        assertTrue(notEvenPowerOfTwo.test(3));
        assertFalse(notEvenPowerOfTwo.test(1L << 12));
    }

    @Test
    void nonNegativePredicateCachesForRepeatedChecks() {
        AtomicLong counter = new AtomicLong();
        LongPredicate guard = Longs.nonNegative();

        assertTrue(guard.test(counter.getAndIncrement()));
        assertFalse(guard.test(-1L));
    }
}
