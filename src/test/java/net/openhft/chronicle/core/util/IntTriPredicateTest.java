package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntTriPredicateTest {

    @Test
    void testShouldReturnCorrectResult() {
        IntTriPredicate predicate = (t, u, v) -> t > u && u > v;

        assertTrue(predicate.test(3, 2, 1));
        assertFalse(predicate.test(1, 2, 3));
    }

    @Test
    void andShouldCombineTwoPredicates() {
        IntTriPredicate predicate1 = (t, u, v) -> t > u;
        IntTriPredicate predicate2 = (t, u, v) -> u > v;

        IntTriPredicate combined = predicate1.and(predicate2);

        assertTrue(combined.test(3, 2, 1));
        assertFalse(combined.test(1, 2, 3));
    }

    @Test
    void negateShouldInvertPredicate() {
        IntTriPredicate predicate = (t, u, v) -> t > u && u > v;
        IntTriPredicate negated = predicate.negate();

        assertFalse(negated.test(3, 2, 1));
        assertTrue(negated.test(1, 2, 3));
    }

    @Test
    void orShouldCombineTwoPredicates() {
        IntTriPredicate predicate1 = (t, u, v) -> t > u;
        IntTriPredicate predicate2 = (t, u, v) -> u > v;

        IntTriPredicate combined = predicate1.or(predicate2);

        assertTrue(combined.test(2, 3, 1));
        assertTrue(combined.test(3, 1, 2));
        assertFalse(combined.test(1, 2, 3));
    }
}
