/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class IntsTest {

    @Test
    void requireNonNegativeAllowsZeroAndPositive() {
        String codeSource = Ints.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue(codeSource.contains("/target/classes"), "Expected Ints to be loaded from target/classes but was " + codeSource);

        assertEquals(0, Ints.requireNonNegative(0), "requireNonNegative should accept zero value");
        assertEquals(42, Ints.requireNonNegative(42), "requireNonNegative should accept positive value 42");
    }

    @Test
    void requireNonNegativeRejectsNegative() {
        try {
            Ints.requireNonNegative(-1);
            fail("requireNonNegative should throw IllegalArgumentException for negative input");
        } catch (IllegalArgumentException iae) {
            String message = iae.getMessage();
            assertTrue(message.contains("negative"), "exception message should contain \"negative\": " + message);
        }
    }

    @Test
    void assertIfEnabledReturnsTrue() {
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 0), "assertIfEnabled should return true for zero with NON_NEGATIVE condition");
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 8), "assertIfEnabled should return true for positive value with NON_NEGATIVE condition");
    }

    @Test
    void failDescriptionExplainsRequirement() {
        String description = Ints.failDescription(IntCondition.POSITIVE, -7);
        assertTrue(description.contains("-7"), description + " should include the failing value");
        assertTrue(description.contains(">"), description + " should include the comparison operator");
    }

    @Test
    void nonNegativePredicateMatchesExpectations() {
        assertTrue(Ints.nonNegative().test(0), "nonNegative predicate should accept zero");
        assertTrue(Ints.nonNegative().test(3), "nonNegative predicate should accept positive values");
        assertFalse(Ints.nonNegative().test(-3), "nonNegative predicate should reject negative values");
    }
}
