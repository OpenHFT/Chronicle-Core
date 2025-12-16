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
public class LongsUsageTest {

    @Test
    public void requireNonNegativeMatchesBytesStores() {
        // Chronicle-Bytes calls Longs.requireNonNegative before copying into native stores
        assertEquals(128L, Longs.requireNonNegative(128L), "requireNonNegativeMatchesBytesStores: L23");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requireNonNegative(-4L));
        assertTrue(ex.getMessage().contains("negative"), "requireNonNegativeMatchesBytesStores: L26");
    }

    @Test
    public void requirePositiveReflectsOSGuards() {
        // OS.map* methods demand strictly positive lengths
        assertEquals(4096L, Longs.requirePositive(4096L), "requirePositiveReflectsOSGuards: L32");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requirePositive(0L));
        assertTrue(ex.getMessage().contains("not positive"), "requirePositiveReflectsOSGuards: L35");
    }

    @Test
    public void alignmentChecksMirrorNativeAccess() {
        long address = 1L << 16; // naturally aligned
        assertEquals(address, Longs.require(LongCondition.LONG_ALIGNED, address, IllegalArgumentException::new), "alignmentChecksMirrorNativeAccess: L41");

        IllegalArgumentException misAligned = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.LONG_ALIGNED, address + 3, IllegalArgumentException::new));
        assertTrue(misAligned.getMessage().contains(LongCondition.LONG_ALIGNED.toString()), "alignmentChecksMirrorNativeAccess: L45");
    }

    @Test
    public void requireAppliesPredicateForByteConvertibleValues() {
        long withinByte = 120L;
        assertEquals(withinByte, Longs.require(LongCondition.BYTE_CONVERTIBLE, withinByte, IllegalArgumentException::new), "requireAppliesPredicateForByteConvertibleValues: L51");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.BYTE_CONVERTIBLE, Byte.MAX_VALUE + 2L, IllegalArgumentException::new));
        assertTrue(ex.getMessage().contains(LongCondition.BYTE_CONVERTIBLE.toString()), "requireAppliesPredicateForByteConvertibleValues: L55");
    }

    @Test
    public void negateBehaviourMatchesDownstreamExpectations() {
        LongPredicate notNonPositive = LongCondition.NON_POSITIVE.negate();
        assertTrue(notNonPositive.test(7), "negateBehaviourMatchesDownstreamExpectations: L61");
        assertFalse(notNonPositive.test(-1), "negateBehaviourMatchesDownstreamExpectations: L62");

        LongPredicate notEvenPowerOfTwo = LongCondition.EVEN_POWER_OF_TWO.negate();
        assertTrue(notEvenPowerOfTwo.test(3), "negateBehaviourMatchesDownstreamExpectations: L65");
        assertFalse(notEvenPowerOfTwo.test(1L << 12), "negateBehaviourMatchesDownstreamExpectations: L66");
    }

    @Test
    public void nonNegativePredicateCachesForRepeatedChecks() {
        AtomicLong counter = new AtomicLong();
        LongPredicate guard = Longs.nonNegative();

        assertTrue(guard.test(counter.getAndIncrement()), "nonNegativePredicateCachesForRepeatedChecks: L74");
        assertFalse(guard.test(-1L), "nonNegativePredicateCachesForRepeatedChecks: L75");
    }
}
