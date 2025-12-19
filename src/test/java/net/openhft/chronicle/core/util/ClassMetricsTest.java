/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(metrics1, metrics2, "operation result should equal expected value");
        assertNotEquals(metrics1, metrics3);

        assertEquals(metrics1.hashCode(), metrics2.hashCode(), "equal objects should have equal hash codes");
        assertNotEquals(metrics1.hashCode(), metrics3.hashCode());
    }

    @Test
    void testToString() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        String toStringResult = metrics.toString();

        assertTrue(toStringResult.contains("offset=10"), "toString should include offset field value");
        assertTrue(toStringResult.contains("length=20"), "toString should include length field value");
    }
}
