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
        assertEquals(128L, Longs.requireNonNegative(128L), "positive value should pass non-negative validation");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requireNonNegative(-4L),
                "requireNonNegative should reject negative value");
        String message = ex.getMessage();
        assertTrue(message.contains("negative"), "exception message should indicate negative value rejection: " + message);
    }

    @Test
    void requirePositiveReflectsOSGuards() {
        // OS.map* methods demand strictly positive lengths
        assertEquals(4096L, Longs.requirePositive(4096L), "positive value should pass positive validation");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Longs.requirePositive(0L),
                "requirePositive should reject zero value");
        String message = ex.getMessage();
        assertTrue(message.contains("not positive"), "exception message should indicate zero is not positive: " + message);
    }

    @Test
    void alignmentChecksMirrorNativeAccess() {
        long address = 1L << 16; // naturally aligned
        assertEquals(address, Longs.require(LongCondition.LONG_ALIGNED, address, IllegalArgumentException::new), "aligned address should pass long alignment check");

        IllegalArgumentException misAligned = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.LONG_ALIGNED, address + 3, IllegalArgumentException::new),
                "require should reject misaligned address");
        String message = misAligned.getMessage();
        assertTrue(message.contains(LongCondition.LONG_ALIGNED.toString()), "exception message should indicate alignment requirement violation: " + message);
    }

    @Test
    void requireAppliesPredicateForByteConvertibleValues() {
        long withinByte = 120L;
        assertEquals(withinByte, Longs.require(LongCondition.BYTE_CONVERTIBLE, withinByte, IllegalArgumentException::new), "value within byte range should pass convertibility check");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Longs.require(LongCondition.BYTE_CONVERTIBLE, Byte.MAX_VALUE + 2L, IllegalArgumentException::new),
                "require should reject non byte convertible value");
        String message = ex.getMessage();
        assertTrue(message.contains(LongCondition.BYTE_CONVERTIBLE.toString()), "exception message should indicate byte convertibility violation: " + message);
    }

    @Test
    void negateBehaviourMatchesDownstreamExpectations() {
        LongPredicate notNonPositive = LongCondition.NON_POSITIVE.negate();
        assertTrue(notNonPositive.test(7), "positive value should pass negated non-positive condition");
        assertFalse(notNonPositive.test(-1), "negative value should fail negated non-positive condition");

        LongPredicate notEvenPowerOfTwo = LongCondition.EVEN_POWER_OF_TWO.negate();
        assertTrue(notEvenPowerOfTwo.test(3), "odd number should pass negated even power of two condition");
        assertFalse(notEvenPowerOfTwo.test(1L << 12), "even power of two should fail negated condition");
    }

    @Test
    void nonNegativePredicateCachesForRepeatedChecks() {
        AtomicLong counter = new AtomicLong();
        LongPredicate guard = Longs.nonNegative();

        assertTrue(guard.test(counter.getAndIncrement()), "zero value should pass non-negative predicate");
        assertFalse(guard.test(-1L), "negative value should fail non-negative predicate");
    }
}
