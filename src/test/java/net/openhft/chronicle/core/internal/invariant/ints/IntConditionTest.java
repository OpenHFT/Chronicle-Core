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
        assertTrue(IntCondition.POSITIVE.test(3));
        assertFalse(IntCondition.POSITIVE.test(0));

        assertTrue(IntCondition.NEGATIVE.test(-4));
        assertFalse(IntCondition.NEGATIVE.test(2));

        assertTrue(IntCondition.ZERO.test(0));
        assertFalse(IntCondition.ZERO.test(9));

        assertTrue(IntCondition.NON_POSITIVE.test(-1));
        assertTrue(IntCondition.NON_POSITIVE.test(0));
        assertFalse(IntCondition.NON_POSITIVE.test(6));

        assertTrue(IntCondition.NON_NEGATIVE.test(0));
        assertTrue(IntCondition.NON_NEGATIVE.test(11));
        assertFalse(IntCondition.NON_NEGATIVE.test(-3));

        assertTrue(IntCondition.NON_ZERO.test(5));
        assertFalse(IntCondition.NON_ZERO.test(0));
    }

    @Test
    void rangeAndAlignmentChecks() {
        assertTrue(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE));
        assertFalse(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1));

        assertTrue(IntCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE));
        assertFalse(IntCondition.SHORT_CONVERTIBLE.test((int) Short.MIN_VALUE - 1));

        assertTrue(IntCondition.EVEN_POWER_OF_TWO.test(1 << 5));
        assertFalse(IntCondition.EVEN_POWER_OF_TWO.test(6));

        assertTrue(IntCondition.SHORT_ALIGNED.test(64));
        // 11 is not divisible by 2, hence not short-aligned
        assertFalse(IntCondition.SHORT_ALIGNED.test(11));

        assertTrue(IntCondition.INT_ALIGNED.test(128));
        assertFalse(IntCondition.INT_ALIGNED.test(6));

        assertTrue(IntCondition.LONG_ALIGNED.test(256));
        assertFalse(IntCondition.LONG_ALIGNED.test(4));
    }

    @Test
    void negateMappings() {
        IntPredicate positiveNegated = IntCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1));
        assertFalse(positiveNegated.test(3));

        IntPredicate negativeNegated = IntCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(4));
        assertFalse(negativeNegated.test(-2));

        IntPredicate zeroNegated = IntCondition.ZERO.negate();
        assertTrue(zeroNegated.test(6));
        assertFalse(zeroNegated.test(0));

        IntPredicate nonPositiveNegated = IntCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(8));
        assertFalse(nonPositiveNegated.test(-3));

        IntPredicate nonNegativeNegated = IntCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-5));
        assertFalse(nonNegativeNegated.test(2));

        IntPredicate nonZeroNegated = IntCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0));
        assertFalse(nonZeroNegated.test(7));

        IntPredicate notShortAligned = IntCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(32));
        assertTrue(notShortAligned.test(7));
    }

    @Test
    void descriptiveToString() {
        assertEquals("> 0", IntCondition.POSITIVE.toString());
        assertEquals("!= 0", IntCondition.NON_ZERO.toString());

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", IntCondition.BYTE_CONVERTIBLE.toString());
    }
}
