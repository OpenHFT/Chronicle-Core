package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LongBiPredicateTest {

    @Test
    void testShouldReturnCorrectResult() {
        LongBiPredicate predicate = (t, u) -> t > u;

        assertTrue(predicate.test(3L, 2L));
        assertFalse(predicate.test(2L, 3L));
    }

    @Test
    void andShouldCombineTwoPredicates() {
        LongBiPredicate predicate1 = (t, u) -> t > u;
        LongBiPredicate predicate2 = (t, u) -> t % 2 == 0;

        LongBiPredicate combined = predicate1.and(predicate2);

        assertTrue(combined.test(4L, 3L));
        assertFalse(combined.test(3L, 2L));
    }

    @Test
    void negateShouldInvertPredicate() {
        LongBiPredicate predicate = (t, u) -> t > u;
        LongBiPredicate negated = predicate.negate();

        assertFalse(negated.test(3L, 2L));
        assertTrue(negated.test(2L, 3L));
    }

    @Test
    void orShouldCombineTwoPredicates() {
        LongBiPredicate predicate1 = (t, u) -> t > u;
        LongBiPredicate predicate2 = (t, u) -> u > 2;

        LongBiPredicate combined = predicate1.or(predicate2);

        assertTrue(combined.test(1L, 3L));
        assertFalse(combined.test(2L, 2L));
    }
}
