/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.invariant.ints;

import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class IntConditionTest {

    @Test
    public void basicComparisons() {
        assertTrue(IntCondition.POSITIVE.test(3), "basicComparisons: L16");
        assertFalse(IntCondition.POSITIVE.test(0), "basicComparisons: L17");

        assertTrue(IntCondition.NEGATIVE.test(-4), "basicComparisons: L19");
        assertFalse(IntCondition.NEGATIVE.test(2), "basicComparisons: L20");

        assertTrue(IntCondition.ZERO.test(0), "basicComparisons: L22");
        assertFalse(IntCondition.ZERO.test(9), "basicComparisons: L23");

        assertTrue(IntCondition.NON_POSITIVE.test(-1), "basicComparisons: L25");
        assertTrue(IntCondition.NON_POSITIVE.test(0), "basicComparisons: L26");
        assertFalse(IntCondition.NON_POSITIVE.test(6), "basicComparisons: L27");

        assertTrue(IntCondition.NON_NEGATIVE.test(0), "basicComparisons: L29");
        assertTrue(IntCondition.NON_NEGATIVE.test(11), "basicComparisons: L30");
        assertFalse(IntCondition.NON_NEGATIVE.test(-3), "basicComparisons: L31");

        assertTrue(IntCondition.NON_ZERO.test(5), "basicComparisons: L33");
        assertFalse(IntCondition.NON_ZERO.test(0), "basicComparisons: L34");
    }

    @Test
    public void rangeAndAlignmentChecks() {
        assertTrue(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE), "rangeAndAlignmentChecks: L39");
        assertFalse(IntCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1), "rangeAndAlignmentChecks: L40");

        assertTrue(IntCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE), "rangeAndAlignmentChecks: L42");
        assertFalse(IntCondition.SHORT_CONVERTIBLE.test((int) Short.MIN_VALUE - 1), "rangeAndAlignmentChecks: L43");

        assertTrue(IntCondition.EVEN_POWER_OF_TWO.test(1 << 5), "rangeAndAlignmentChecks: L45");
        assertFalse(IntCondition.EVEN_POWER_OF_TWO.test(6), "rangeAndAlignmentChecks: L46");

        assertTrue(IntCondition.SHORT_ALIGNED.test(64), "rangeAndAlignmentChecks: L48");
        // 11 is not divisible by 2, hence not short-aligned
        assertFalse(IntCondition.SHORT_ALIGNED.test(11), "rangeAndAlignmentChecks: L50");

        assertTrue(IntCondition.INT_ALIGNED.test(128), "rangeAndAlignmentChecks: L52");
        assertFalse(IntCondition.INT_ALIGNED.test(6), "rangeAndAlignmentChecks: L53");

        assertTrue(IntCondition.LONG_ALIGNED.test(256), "rangeAndAlignmentChecks: L55");
        assertFalse(IntCondition.LONG_ALIGNED.test(4), "rangeAndAlignmentChecks: L56");
    }

    @Test
    public void negateMappings() {
        IntPredicate positiveNegated = IntCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1), "negateMappings: L62");
        assertFalse(positiveNegated.test(3), "negateMappings: L63");

        IntPredicate negativeNegated = IntCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(4), "negateMappings: L66");
        assertFalse(negativeNegated.test(-2), "negateMappings: L67");

        IntPredicate zeroNegated = IntCondition.ZERO.negate();
        assertTrue(zeroNegated.test(6), "negateMappings: L70");
        assertFalse(zeroNegated.test(0), "negateMappings: L71");

        IntPredicate nonPositiveNegated = IntCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(8), "negateMappings: L74");
        assertFalse(nonPositiveNegated.test(-3), "negateMappings: L75");

        IntPredicate nonNegativeNegated = IntCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-5), "negateMappings: L78");
        assertFalse(nonNegativeNegated.test(2), "negateMappings: L79");

        IntPredicate nonZeroNegated = IntCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0), "negateMappings: L82");
        assertFalse(nonZeroNegated.test(7), "negateMappings: L83");

        IntPredicate notShortAligned = IntCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(32), "negateMappings: L86");
        assertTrue(notShortAligned.test(7), "negateMappings: L87");
    }

    @Test
    public void descriptiveToString() {
        assertEquals("> 0", IntCondition.POSITIVE.toString(), "descriptiveToString: L92");
        assertEquals("!= 0", IntCondition.NON_ZERO.toString(), "descriptiveToString: L93");

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", IntCondition.BYTE_CONVERTIBLE.toString(), "descriptiveToString: L95");
    }
}
