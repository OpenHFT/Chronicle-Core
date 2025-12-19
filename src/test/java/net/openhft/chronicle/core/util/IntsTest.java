/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntsTest {

    @Test
    public void requireNonNegativeAllowsZeroAndPositive() {
        String codeSource = Ints.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue(codeSource.contains("/target/classes"), "Expected Ints to be loaded from target/classes but was " + codeSource);

        assertEquals(0, Ints.requireNonNegative(0), "requireNonNegative should accept zero");
        assertEquals(42, Ints.requireNonNegative(42), "requireNonNegative should accept positive values");
    }

    @Test
    public void requireNonNegativeRejectsNegative() {
        try {
            Ints.requireNonNegative(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("negative"), "exception message should contain 'negative'");
        }
    }

    @Test
    public void assertIfEnabledReturnsTrue() {
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 0), "assertIfEnabled should return true for zero with NON_NEGATIVE condition");
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 8), "assertIfEnabled should return true for positive value with NON_NEGATIVE condition");
    }

    @Test
    public void failDescriptionExplainsRequirement() {
        String description = Ints.failDescription(IntCondition.POSITIVE, -7);
        assertTrue(description.contains("-7"), "fail description should include the failing value");
        assertTrue(description.contains(">"), "fail description should include the comparison operator");
    }

    @Test
    public void nonNegativePredicateMatchesExpectations() {
        assertTrue(Ints.nonNegative().test(0), "nonNegative predicate should accept zero");
        assertTrue(Ints.nonNegative().test(3), "nonNegative predicate should accept positive values");
        assertFalse(Ints.nonNegative().test(-3), "nonNegative predicate should reject negative values");
    }
}
