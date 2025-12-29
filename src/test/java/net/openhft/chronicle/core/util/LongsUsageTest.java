/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.longs.LongCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests mirror the guard patterns used in peer Chronicle modules (e.g. Chronicle-Bytes)
 * to validate offsets, lengths and alignment before calling into native memory code paths.
 */
class LongsUsageTest {

    @DisplayName("requireNonNegativeMatchesBytesStores behaviour under expected input and output conditions")
    @Test
    void requireNonNegativeMatchesBytesStores() {
        // Chronicle-Bytes calls Longs.requireNonNegative before copying into native stores
        assertEquals(128L, Longs.requireNonNegative(128L), "positive value should pass non-negative validation");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requireNonNegative(-4L),
                "requireNonNegative should throw for negative value");
        assertTrue(ex.getMessage().contains("negative"), "exception message should include \"negative\": " + ex.getMessage());
    }

    @DisplayName("requirePositiveReflectsOSGuards behaviour under expected input and output conditions")
    @Test
    void requirePositiveReflectsOSGuards() {
        // OS.map* methods demand strictly positive lengths
        assertEquals(4096L, Longs.requirePositive(4096L), "positive value should pass positive validation");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requirePositive(0L),
                "requirePositive should throw for zero length");
        assertTrue(ex.getMessage().contains("not positive"), "exception message should include \"not positive\": " + ex.getMessage());
    }

    @DisplayName("alignmentChecksMirrorNativeAccess behaviour under expected input and output conditions")
    @Test
    void alignmentChecksMirrorNativeAccess() {
        long address = 1L << 16; // naturally aligned
        assertEquals(address, Longs.require(LongCondition.LONG_ALIGNED, address, IllegalArgumentException::new), "aligned address should pass long alignment check");

        IllegalArgumentException misAligned = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.LONG_ALIGNED, address + 3, IllegalArgumentException::new),
                "alignment check should throw for misaligned address");
        assertTrue(misAligned.getMessage().contains(LongCondition.LONG_ALIGNED.toString()),
                "exception message should include alignment requirement: " + misAligned.getMessage());
    }

    @DisplayName("requireAppliesPredicateForByteConvertibleValues behaviour under expected input and output conditions")
    @Test
    void requireAppliesPredicateForByteConvertibleValues() {
        long withinByte = 120L;
        assertEquals(withinByte, Longs.require(LongCondition.BYTE_CONVERTIBLE, withinByte, IllegalArgumentException::new), "value within byte range should pass convertibility check");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.BYTE_CONVERTIBLE, Byte.MAX_VALUE + 2L, IllegalArgumentException::new),
                "byte convertibility check should throw for oversized value");
        assertTrue(ex.getMessage().contains(LongCondition.BYTE_CONVERTIBLE.toString()),
                "exception message should include byte convertibility rule: " + ex.getMessage());
    }

    @DisplayName("negateBehaviourMatchesDownstreamExpectations behaviour under expected input and output conditions")
    @Test
    void negateBehaviourMatchesDownstreamExpectations() {
        LongPredicate notNonPositive = LongCondition.NON_POSITIVE.negate();
        assertTrue(notNonPositive.test(7), "positive value should pass negated non-positive condition");
        assertFalse(notNonPositive.test(-1), "negative value should fail negated non-positive condition");

        LongPredicate notEvenPowerOfTwo = LongCondition.EVEN_POWER_OF_TWO.negate();
        assertTrue(notEvenPowerOfTwo.test(3), "odd number should pass negated even power of two condition");
        assertFalse(notEvenPowerOfTwo.test(1L << 12), "even power of two should fail negated condition");
    }

    @DisplayName("nonNegativePredicateCachesForRepeatedChecks behaviour under expected input and output conditions")
    @Test
    void nonNegativePredicateCachesForRepeatedChecks() {
        AtomicLong counter = new AtomicLong();
        LongPredicate guard = Longs.nonNegative();

        assertTrue(guard.test(counter.getAndIncrement()), "zero value should pass non-negative predicate");
        assertFalse(guard.test(-1L), "negative value should fail non-negative predicate");
    }
}
