/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.invariant.ints.IntCondition;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntsTest {

    @Test
    public void requireNonNegativeAllowsZeroAndPositive() {
        String codeSource = Ints.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        assertTrue("Expected Ints to be loaded from target/classes but was " + codeSource, codeSource.contains("/target/classes"));

        assertEquals(0, Ints.requireNonNegative(0));
        assertEquals(42, Ints.requireNonNegative(42));
    }

    @Test
    public void requireNonNegativeRejectsNegative() {
        try {
            Ints.requireNonNegative(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("negative"));
        }
    }

    @Test
    public void assertIfEnabledReturnsTrue() {
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 0));
        assertTrue(Ints.assertIfEnabled(IntCondition.NON_NEGATIVE, 8));
    }

    @Test
    public void failDescriptionExplainsRequirement() {
        String description = Ints.failDescription(IntCondition.POSITIVE, -7);
        assertTrue(description.contains("-7"));
        assertTrue(description.contains(">"));
    }

    @Test
    public void nonNegativePredicateMatchesExpectations() {
        assertTrue(Ints.nonNegative().test(0));
        assertTrue(Ints.nonNegative().test(3));
        assertFalse(Ints.nonNegative().test(-3));
    }
}
