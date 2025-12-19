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

        assertTrue(LongCondition.POSITIVE.test(7), "positive condition should accept value greater than zero");
        assertFalse(LongCondition.POSITIVE.test(0), "positive condition should reject zero");

        assertTrue(LongCondition.NEGATIVE.test(-3), "negative condition should accept value less than zero");
        assertFalse(LongCondition.NEGATIVE.test(1), "negative condition should reject positive value");

        assertTrue(LongCondition.ZERO.test(0), "zero condition should accept zero value");
        assertFalse(LongCondition.ZERO.test(4), "zero condition should reject non-zero value");

        assertTrue(LongCondition.NON_POSITIVE.test(0), "non-positive condition should accept zero");
        assertTrue(LongCondition.NON_POSITIVE.test(-1), "non-positive condition should accept negative value");
        assertFalse(LongCondition.NON_POSITIVE.test(5), "non-positive condition should reject positive value");

        assertTrue(LongCondition.NON_NEGATIVE.test(0), "non-negative condition should accept zero");
        assertTrue(LongCondition.NON_NEGATIVE.test(9), "non-negative condition should accept positive value");
        assertFalse(LongCondition.NON_NEGATIVE.test(-2), "non-negative condition should reject negative value");

        assertTrue(LongCondition.NON_ZERO.test(8), "non-zero condition should accept non-zero value");
        assertFalse(LongCondition.NON_ZERO.test(0), "non-zero condition should reject zero");
    }

    @Test
    public void rangeAndAlignmentChecks() {
        assertTrue(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE), "byte convertible condition should accept value within byte range");
        assertFalse(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE + 1L), "byte convertible condition should reject value exceeding byte maximum");

        assertTrue(LongCondition.SHORT_CONVERTIBLE.test(Short.MIN_VALUE), "short convertible condition should accept value within short range");
        assertFalse(LongCondition.SHORT_CONVERTIBLE.test((long) Short.MIN_VALUE - 1), "short convertible condition should reject value below short minimum");

        assertTrue(LongCondition.EVEN_POWER_OF_TWO.test(1L << 10), "even power of two condition should accept power of two value");
        assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(3), "even power of two condition should reject non-power-of-two value");

        assertTrue(LongCondition.SHORT_ALIGNED.test(32), "short aligned condition should accept value aligned to short boundary");
        assertFalse(LongCondition.SHORT_ALIGNED.test(3), "short aligned condition should reject unaligned value");

        assertTrue(LongCondition.INT_ALIGNED.test(64), "int aligned condition should accept value aligned to int boundary");
        assertFalse(LongCondition.INT_ALIGNED.test(2), "int aligned condition should reject unaligned value");

        assertTrue(LongCondition.LONG_ALIGNED.test(128), "long aligned condition should accept value aligned to long boundary");
        assertFalse(LongCondition.LONG_ALIGNED.test(4), "long aligned condition should reject unaligned value");
    }

    @Test
    public void negateMappings() {
        LongPredicate positiveNegated = LongCondition.POSITIVE.negate();
        assertTrue(positiveNegated.test(-1), "negated positive condition should accept non-positive value");
        assertFalse(positiveNegated.test(2), "negated positive condition should reject positive value");

        LongPredicate negativeNegated = LongCondition.NEGATIVE.negate();
        assertTrue(negativeNegated.test(3), "negated negative condition should accept non-negative value");
        assertFalse(negativeNegated.test(-2), "negated negative condition should reject negative value");

        LongPredicate zeroNegated = LongCondition.ZERO.negate();
        assertTrue(zeroNegated.test(5), "negated zero condition should accept non-zero value");
        assertFalse(zeroNegated.test(0), "negated zero condition should reject zero");

        LongPredicate nonPositiveNegated = LongCondition.NON_POSITIVE.negate();
        assertTrue(nonPositiveNegated.test(7), "negated non-positive condition should accept positive value");
        assertFalse(nonPositiveNegated.test(-1), "negated non-positive condition should reject non-positive value");

        LongPredicate nonNegativeNegated = LongCondition.NON_NEGATIVE.negate();
        assertTrue(nonNegativeNegated.test(-3), "negated non-negative condition should accept negative value");
        assertFalse(nonNegativeNegated.test(4), "negated non-negative condition should reject non-negative value");

        LongPredicate nonZeroNegated = LongCondition.NON_ZERO.negate();
        assertTrue(nonZeroNegated.test(0), "negated non-zero condition should accept zero");
        assertFalse(nonZeroNegated.test(9), "negated non-zero condition should reject non-zero value");

        LongPredicate notShortAligned = LongCondition.SHORT_ALIGNED.negate();
        assertFalse(notShortAligned.test(16), "negated short aligned condition should reject aligned value");
        assertTrue(notShortAligned.test(3), "negated short aligned condition should accept unaligned value");
    }

    @Test
    public void descriptiveToString() {
        assertEquals("> 0", LongCondition.POSITIVE.toString(), "positive condition should have descriptive string representation");
        assertEquals("!= 0", LongCondition.NON_ZERO.toString(), "non-zero condition should have descriptive string representation");

        assertEquals("in [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]", LongCondition.BYTE_CONVERTIBLE.toString(), "byte convertible condition should show valid range in string representation");
    }
}
