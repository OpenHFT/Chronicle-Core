/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.invariant.ints;

import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.*;

class IntConditionTest {

    @Test
    void basicComparisons() {
        assertTrue(IntCondition.POSITIVE.test(3), "positive value 3 should satisfy positive condition");
        assertFalse(IntCondition.POSITIVE.test(0), "zero should not satisfy positive condition");

        assertTrue(IntCondition.NEGATIVE.test(-4), "negative value -4 should satisfy negative condition");
        assertFalse(IntCondition.NEGATIVE.test(2), "positive value 2 should not satisfy negative condition");

        assertTrue(IntCondition.ZERO.test(0), "zero should satisfy zero condition");
        assertFalse(IntCondition.ZERO.test(9), "non-zero value 9 should not satisfy zero condition");
    }

    @Test
    void basicComparisons2() {
        assertTrue(IntCondition.NON_POSITIVE.test(-1), "negative value -1 should satisfy non-positive condition");
        assertTrue(IntCondition.NON_POSITIVE.test(0), "zero should satisfy non-positive condition");
        assertFalse(IntCondition.NON_POSITIVE.test(6), "positive value 6 should not satisfy non-positive condition");

        assertTrue(IntCondition.NON_NEGATIVE.test(0), "zero should satisfy non-negative condition");
        assertTrue(IntCondition.NON_NEGATIVE.test(11), "positive value 11 should satisfy non-negative condition");
        assertFalse(IntCondition.NON_NEGATIVE.test(-3), "negative value -3 should not satisfy non-negative condition");

        assertTrue(IntCondition.NON_ZERO.test(5), "non-zero value 5 should satisfy non-zero condition");
        assertFalse(IntCondition.NON_ZERO.test(0), "zero should not satisfy non-zero condition");
    }

    @Test
    void rangeAndAlignmentChecks() {
        assertTrue(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE), "byte max value should be byte-convertible");
        assertFalse(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1), "value exceeding byte max should not be byte-convertible");

        assertTrue(IntCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE), "short min value should be short-convertible");
        assertFalse(IntCondition.SHORT_CONVERTIBLE.test((int) Short.MIN_VALUE - 1), "value below short min should not be short-convertible");

        assertTrue(IntCondition.EVEN_POWER_OF_TWO.test(1 << 5), "power of two 32 should satisfy even power of two condition");
        assertFalse(IntCondition.EVEN_POWER_OF_TWO.test(6), "non-power-of-two value 6 should not satisfy even power of two condition");

        assertTrue(IntCondition.SHORT_ALIGNED.test(64), "value 64 divisible by 2 should be short-aligned");
        // 11 is not divisible by 2, hence not short-aligned
        assertFalse(IntCondition.SHORT_ALIGNED.test(11), "odd value 11 should not be short-aligned");

        assertTrue(IntCondition.INT_ALIGNED.test(128), "value 128 divisible by 4 should be int-aligned");
        assertFalse(IntCondition.INT_ALIGNED.test(6), "value 6 not divisible by 4 should not be int-aligned");

        assertTrue(IntCondition.LONG_ALIGNED.test(256), "value 256 divisible by 8 should be long-aligned");
        assertFalse(IntCondition.LONG_ALIGNED.test(4), "value 4 not divisible by 8 should not be long-aligned");
    }

    @Test
    void negateMappings() {
        IntPredicate positiveNegated = IntCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1), "negated positive condition should accept negative value -1");
        assertFalse(positiveNegated.test(3), "negated positive condition should reject positive value 3");

        IntPredicate negativeNegated = IntCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(4), "negated negative condition should accept positive value 4");
        assertFalse(negativeNegated.test(-2), "negated negative condition should reject negative value -2");

        IntPredicate zeroNegated = IntCondition.ZERO.negate();
        assertTrue(zeroNegated.test(6), "negated zero condition should accept non-zero value 6");
        assertFalse(zeroNegated.test(0), "negated zero condition should reject zero");

        IntPredicate nonPositiveNegated = IntCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(8), "negated non-positive condition should accept positive value 8");
        assertFalse(nonPositiveNegated.test(-3), "negated non-positive condition should reject negative value -3");

        IntPredicate nonNegativeNegated = IntCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-5), "negated non-negative condition should accept negative value -5");
        assertFalse(nonNegativeNegated.test(2), "negated non-negative condition should reject positive value 2");
    }

    @Test
    void negateMappings2() {
        IntPredicate nonZeroNegated = IntCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0), "negated non-zero condition should accept zero");
        assertFalse(nonZeroNegated.test(7), "negated non-zero condition should reject non-zero value 7");

        IntPredicate notShortAligned = IntCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(32), "negated short-aligned condition should reject aligned value 32");
        assertTrue(notShortAligned.test(7), "negated short-aligned condition should accept unaligned value 7");
    }

    @Test
    void descriptiveToString() {
        assertEquals("> 0", IntCondition.POSITIVE.toString(), "positive condition should have descriptive string representation");
        assertEquals("!= 0", IntCondition.NON_ZERO.toString(), "non-zero condition should have descriptive string representation");

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", IntCondition.BYTE_CONVERTIBLE.toString(), "byte-convertible condition should show valid byte range in string representation");
    }
}
