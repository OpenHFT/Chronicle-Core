/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.invariant.longs;

import org.junit.jupiter.api.Test;

import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;

class LongConditionTest {

    @Test
    void basicComparisons() {
        String codeSource = LongCondition.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue(codeSource.contains("/target/classes"), "Expected instrumented class from target/classes but was " + codeSource);

        assertTrue(LongCondition.POSITIVE.test(7));
        assertFalse(LongCondition.POSITIVE.test(0));

        assertTrue(LongCondition.NEGATIVE.test(-3));
        assertFalse(LongCondition.NEGATIVE.test(1));

        assertTrue(LongCondition.ZERO.test(0));
        assertFalse(LongCondition.ZERO.test(4));

        assertTrue(LongCondition.NON_POSITIVE.test(0));
        assertTrue(LongCondition.NON_POSITIVE.test(-1));
        assertFalse(LongCondition.NON_POSITIVE.test(5));

        assertTrue(LongCondition.NON_NEGATIVE.test(0));
        assertTrue(LongCondition.NON_NEGATIVE.test(9));
        assertFalse(LongCondition.NON_NEGATIVE.test(-2));

        assertTrue(LongCondition.NON_ZERO.test(8));
        assertFalse(LongCondition.NON_ZERO.test(0));
    }

    @Test
    void rangeAndAlignmentChecks() {
        assertTrue(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE));
        assertFalse(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1L));

        assertTrue(LongCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE));
        assertFalse(LongCondition.SHORT_CONVERTIBLE.test((long) Short.MIN_VALUE - 1));

        assertTrue(LongCondition.EVEN_POWER_OF_TWO.test(1L << 10));
        assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(3));

        assertTrue(LongCondition.SHORT_ALIGNED.test(32));
        assertFalse(LongCondition.SHORT_ALIGNED.test(3));

        assertTrue(LongCondition.INT_ALIGNED.test(64));
        assertFalse(LongCondition.INT_ALIGNED.test(2));

        assertTrue(LongCondition.LONG_ALIGNED.test(128));
        assertFalse(LongCondition.LONG_ALIGNED.test(4));
    }

    @Test
    void negateMappings() {
        LongPredicate positiveNegated = LongCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1));
        assertFalse(positiveNegated.test(2));

        LongPredicate negativeNegated = LongCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(3));
        assertFalse(negativeNegated.test(-2));

        LongPredicate zeroNegated = LongCondition.ZERO.negate();
        assertTrue(zeroNegated.test(5));
        assertFalse(zeroNegated.test(0));

        LongPredicate nonPositiveNegated = LongCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(7));
        assertFalse(nonPositiveNegated.test(-1));

        LongPredicate nonNegativeNegated = LongCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-3));
        assertFalse(nonNegativeNegated.test(4));

        LongPredicate nonZeroNegated = LongCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0));
        assertFalse(nonZeroNegated.test(9));

        LongPredicate notShortAligned = LongCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(16));
        assertTrue(notShortAligned.test(3));
    }

    @Test
    void descriptiveToString() {
        assertEquals("> 0", LongCondition.POSITIVE.toString());
        assertEquals("!= 0", LongCondition.NON_ZERO.toString());

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", LongCondition.BYTE_CONVERTIBLE.toString());
    }
}
