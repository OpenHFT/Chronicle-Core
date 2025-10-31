/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
