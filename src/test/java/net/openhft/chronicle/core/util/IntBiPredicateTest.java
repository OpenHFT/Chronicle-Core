package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntBiPredicateTest {

    @Test
    void testShouldReturnCorrectResult() {
        IntBiPredicate predicate = (t, u) -> t > u;

        assertTrue(predicate.test(2, 1));
        assertFalse(predicate.test(1, 2));
    }

    @Test
    void andShouldCombineTwoPredicates() {
        IntBiPredicate predicate1 = (t, u) -> t > u;
        IntBiPredicate predicate2 = (t, u) -> t % 2 == 0;

        IntBiPredicate combined = predicate1.and(predicate2);

        assertTrue(combined.test(4, 3));
        assertFalse(combined.test(1, 0));
    }

    @Test
    void negateShouldInvertPredicate() {
        IntBiPredicate predicate = (t, u) -> t > u;
        IntBiPredicate negated = predicate.negate();

        assertFalse(negated.test(2, 1));
        assertTrue(negated.test(1, 2));
    }

    @Test
    void orShouldCombineTwoPredicates() {
        IntBiPredicate predicate1 = (t, u) -> t > u;
        IntBiPredicate predicate2 = (t, u) -> u > 3;

        IntBiPredicate combined = predicate1.or(predicate2);

        assertTrue(combined.test(1, 4));
        assertFalse(combined.test(2, 2));
    }
}
