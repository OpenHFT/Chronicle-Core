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

        assertEquals(0, Ints.requireNonNegative(0), "requireNonNegativeAllowsZeroAndPositive: L18");
        assertEquals(42, Ints.requireNonNegative(42), "requireNonNegativeAllowsZeroAndPositive: L19");
    }

    @Test
    public void requireNonNegativeRejectsNegative() {
        try {
            Ints.requireNonNegative(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("negative"), "requireNonNegativeRejectsNegative: L28");
        }
    }

    @Test
    public void assertIfEnabledReturnsTrue() {
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 0), "assertIfEnabledReturnsTrue: L34");
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 8), "assertIfEnabledReturnsTrue: L35");
    }

    @Test
    public void failDescriptionExplainsRequirement() {
        String description = Ints.failDescription(IntCondition.POSITIVE, -7);
        assertTrue(description.contains("-7"), "failDescriptionExplainsRequirement: L41");
        assertTrue(description.contains(">"), "failDescriptionExplainsRequirement: L42");
    }

    @Test
    public void nonNegativePredicateMatchesExpectations() {
        assertTrue(Ints.nonNegative().test(0), "nonNegativePredicateMatchesExpectations: L47");
        assertTrue(Ints.nonNegative().test(3), "nonNegativePredicateMatchesExpectations: L48");
        assertFalse(Ints.nonNegative().test(-3), "nonNegativePredicateMatchesExpectations: L49");
    }
}
