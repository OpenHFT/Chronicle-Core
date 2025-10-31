package net.openhft.chronicle.core.internal.invariant.longs;

import org.junit.jupiter.api.Test;

import java.util.function.LongPredicate;

import static org.junit.jupiter.api.Assertions.*;

class LongConditionTest {

    @Test
    void evaluatesBasicPredicates() {
        assertTrue(LongCondition.POSITIVE.test(5));
        assertFalse(LongCondition.POSITIVE.test(0));

        assertTrue(LongCondition.NEGATIVE.test(-3));
        assertFalse(LongCondition.NEGATIVE.test(1));

        assertTrue(LongCondition.ZERO.test(0));
        assertFalse(LongCondition.ZERO.test(2));
    }

    @Test
    void rangeBasedConditionsRespectBounds() {
        assertTrue(LongCondition.BYTE_CONVERTIBLE.test(Byte.MIN_VALUE));
        assertTrue(LongCondition.BYTE_CONVERTIBLE.test(Byte.MAX_VALUE));
        assertFalse(LongCondition.BYTE_CONVERTIBLE.test(Byte.MIN_VALUE - 1L));

        assertTrue(LongCondition.SHORT_CONVERTIBLE.test(Short.MAX_VALUE));
        assertFalse(LongCondition.SHORT_CONVERTIBLE.test(Short.MAX_VALUE + 1L));
    }

    @Test
    void powerOfTwoConditionRecognisesSingleBits() {
        assertTrue(LongCondition.EVEN_POWER_OF_TWO.test(1L << 10));
        assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(0));
        assertFalse(LongCondition.EVEN_POWER_OF_TWO.test(3));
    }

    @Test
    void alignmentConditionsRequireExpectedMask() {
        assertTrue(LongCondition.SHORT_ALIGNED.test(8));
        assertFalse(LongCondition.SHORT_ALIGNED.test(3));

        assertTrue(LongCondition.INT_ALIGNED.test(16));
        assertFalse(LongCondition.INT_ALIGNED.test(10));

        assertTrue(LongCondition.LONG_ALIGNED.test(32));
        assertFalse(LongCondition.LONG_ALIGNED.test(18));
    }

    @Test
    void negateReturnsMatchingCounterparts() {
        assertSame(LongCondition.NON_POSITIVE, LongCondition.POSITIVE.negate());
        assertSame(LongCondition.NON_NEGATIVE, LongCondition.NEGATIVE.negate());
        assertSame(LongCondition.NON_ZERO, LongCondition.ZERO.negate());
        assertSame(LongCondition.POSITIVE, LongCondition.NON_POSITIVE.negate());
        assertSame(LongCondition.NEGATIVE, LongCondition.NON_NEGATIVE.negate());
        assertSame(LongCondition.ZERO, LongCondition.NON_ZERO.negate());

        LongPredicate negated = LongCondition.EVEN_POWER_OF_TWO.negate();
        assertFalse(negated.test(8));
        assertTrue(negated.test(3));
    }
}
