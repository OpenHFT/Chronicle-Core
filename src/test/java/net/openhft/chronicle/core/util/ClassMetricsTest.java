/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ClassMetricsTest {

    @Test
    void constructorAndMethodsWorkCorrectly() {
        int expectedOffset = 10;
        int expectedLength = 20;
        ClassMetrics metrics = new ClassMetrics(expectedOffset, expectedLength);

        assertEquals(expectedOffset, metrics.offset(), "offset should match constructor argument");
        assertEquals(expectedLength, metrics.length(), "length should match constructor argument");
    }

    @Test
    void equalsAndHashCode() {
        ClassMetrics metrics1 = new ClassMetrics(10, 20);
        ClassMetrics metrics2 = new ClassMetrics(10, 20);
        ClassMetrics metrics3 = new ClassMetrics(15, 25);

        assertEquals(metrics1, metrics2, "metrics with same offset and length should be equal");
        assertNotEquals(metrics1, metrics3, "metrics with different values should not be equal");

        assertEquals(metrics1.hashCode(), metrics2.hashCode(), "equal objects should have equal hash codes");
        assertNotEquals(metrics1.hashCode(), metrics3.hashCode(), "different objects should have different hash codes");
    }

    @Test
    void testToString() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        String toStringResult = metrics.toString();

        assertTrue(toStringResult.contains("offset=10"), toStringResult + " should include offset=10");
        assertTrue(toStringResult.contains("length=20"), toStringResult + " should include length=20");
    }
}
