/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class IntsTest {

    @Test
    @DisplayName("Require non negative allows zero and positive")
    void requireNonNegativeAllowsZeroAndPositive() {
        String codeSource = Ints.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue(codeSource.contains("/target/classes"), "Expected Ints to be loaded from target/classes but was " + codeSource);

        assertEquals(0, Ints.requireNonNegative(0), "requireNonNegative should accept zero");
        assertEquals(42, Ints.requireNonNegative(42), "requireNonNegative should accept positive values");
    }

    @Test
    @DisplayName("Require non negative rejects negative ints")
    void requireNonNegativeRejectsNegative() {
        try {
            Ints.requireNonNegative(-1);
            fail("requireNonNegative should throw IllegalArgumentException for negative input");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("negative"), "exception message should contain \"negative\": " + iae.getMessage());
        }
    }

    @Test
    @DisplayName("assertIfEnabled returns true for non negative predicate argument")
    void assertIfEnabledReturnsTrue() {
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 0), "assertIfEnabled should return true for zero with NON_NEGATIVE condition");
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 8), "assertIfEnabled should return true for positive value with NON_NEGATIVE condition");
    }

    @Test
    @DisplayName("Fail description includes argument value and operator")
    void failDescriptionExplainsRequirement() {
        String description = Ints.failDescription(IntCondition.POSITIVE, -7);
        assertTrue(description.contains("-7"), "fail description should include \"-7\": " + description);
        assertTrue(description.contains(">"), "fail description should include comparison operator: " + description);
    }

    @Test
    @DisplayName("Non negative predicate matches expectations ints")
    void nonNegativePredicateMatchesExpectations() {
        assertTrue(Ints.nonNegative().test(0), "nonNegative predicate should accept zero");
        assertTrue(Ints.nonNegative().test(3), "nonNegative predicate should accept positive values");
        assertFalse(Ints.nonNegative().test(-3), "nonNegative predicate should reject negative values");
    }
}
