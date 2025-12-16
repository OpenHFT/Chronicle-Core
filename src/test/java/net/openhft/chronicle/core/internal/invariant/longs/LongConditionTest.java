/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.invariant.longs;

import org.junit.jupiter.api.Test;

import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class LongConditionTest {

    @Test
    public void basicComparisons() {
        String codeSource = LongCondition.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue(codeSource.contains("/target/classes"), "Expected instrumented class from target/classes but was " + codeSource);

        assertTrue(LongCondition.POSITIVE.test(7), "basicComparisons: L19");
        assertFalse(LongCondition.POSITIVE.test(0), "basicComparisons: L20");

        assertTrue(LongCondition.NEGATIVE.test(-3), "basicComparisons: L22");
        assertFalse(LongCondition.NEGATIVE.test(1), "basicComparisons: L23");

        assertTrue(LongCondition.ZERO.test(0), "basicComparisons: L25");
        assertFalse(LongCondition.ZERO.test(4), "basicComparisons: L26");

        assertTrue(LongCondition.NON_POSITIVE.test(0), "basicComparisons: L28");
        assertTrue(LongCondition.NON_POSITIVE.test(-1), "basicComparisons: L29");
        assertFalse(LongCondition.NON_POSITIVE.test(5), "basicComparisons: L30");

        assertTrue(LongCondition.NON_NEGATIVE.test(0), "basicComparisons: L32");
        assertTrue(LongCondition.NON_NEGATIVE.test(9), "basicComparisons: L33");
        assertFalse(LongCondition.NON_NEGATIVE.test(-2), "basicComparisons: L34");

        assertTrue(LongCondition.NON_ZERO.test(8), "basicComparisons: L36");
        assertFalse(LongCondition.NON_ZERO.test(0), "basicComparisons: L37");
    }

    @Test
    public void rangeAndAlignmentChecks() {
        assertTrue(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE), "rangeAndAlignmentChecks: L42");
        assertFalse(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1L), "rangeAndAlignmentChecks: L43");

        assertTrue(LongCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE), "rangeAndAlignmentChecks: L45");
        assertFalse(LongCondition.SHORT_CONVERTIBLE.test((long) Short.MIN_VALUE - 1), "rangeAndAlignmentChecks: L46");

        assertTrue(LongCondition.EVEN_POWER_OF_TWO.test(1L << 10), "rangeAndAlignmentChecks: L48");
        assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(3), "rangeAndAlignmentChecks: L49");

        assertTrue(LongCondition.SHORT_ALIGNED.test(32), "rangeAndAlignmentChecks: L51");
        assertFalse(LongCondition.SHORT_ALIGNED.test(3), "rangeAndAlignmentChecks: L52");

        assertTrue(LongCondition.INT_ALIGNED.test(64), "rangeAndAlignmentChecks: L54");
        assertFalse(LongCondition.INT_ALIGNED.test(2), "rangeAndAlignmentChecks: L55");

        assertTrue(LongCondition.LONG_ALIGNED.test(128), "rangeAndAlignmentChecks: L57");
        assertFalse(LongCondition.LONG_ALIGNED.test(4), "rangeAndAlignmentChecks: L58");
    }

    @Test
    public void negateMappings() {
        LongPredicate positiveNegated = LongCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1), "negateMappings: L64");
        assertFalse(positiveNegated.test(2), "negateMappings: L65");

        LongPredicate negativeNegated = LongCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(3), "negateMappings: L68");
        assertFalse(negativeNegated.test(-2), "negateMappings: L69");

        LongPredicate zeroNegated = LongCondition.ZERO.negate();
        assertTrue(zeroNegated.test(5), "negateMappings: L72");
        assertFalse(zeroNegated.test(0), "negateMappings: L73");

        LongPredicate nonPositiveNegated = LongCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(7), "negateMappings: L76");
        assertFalse(nonPositiveNegated.test(-1), "negateMappings: L77");

        LongPredicate nonNegativeNegated = LongCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-3), "negateMappings: L80");
        assertFalse(nonNegativeNegated.test(4), "negateMappings: L81");

        LongPredicate nonZeroNegated = LongCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0), "negateMappings: L84");
        assertFalse(nonZeroNegated.test(9), "negateMappings: L85");

        LongPredicate notShortAligned = LongCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(16), "negateMappings: L88");
        assertTrue(notShortAligned.test(3), "negateMappings: L89");
    }

    @Test
    public void descriptiveToString() {
        assertEquals("> 0", LongCondition.POSITIVE.toString(), "descriptiveToString: L94");
        assertEquals("!= 0", LongCondition.NON_ZERO.toString(), "descriptiveToString: L95");

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", LongCondition.BYTE_CONVERTIBLE.toString(), "descriptiveToString: L97");
    }
}
