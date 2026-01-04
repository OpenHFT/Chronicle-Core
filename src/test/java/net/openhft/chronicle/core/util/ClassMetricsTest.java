/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ClassMetricsTest {

    @Test
    @DisplayName("Constructor and methods work correctly metrics")
    void constructorAndMethodsWorkCorrectly() {
        int expectedOffset = 10;
        int expectedLength = 20;
        ClassMetrics metrics = new ClassMetrics(expectedOffset, expectedLength);

        assertEquals(expectedOffset, metrics.offset(), "offset should match constructor argument");
        assertEquals(expectedLength, metrics.length(), "length should match constructor argument");
    }

    @Test
    @DisplayName("Equals and hashCode reflect offset and length")
    void equalsAndHashCode() {
        ClassMetrics metrics1 = new ClassMetrics(10, 20);
        ClassMetrics metrics2 = new ClassMetrics(10, 20);
        ClassMetrics metrics3 = new ClassMetrics(15, 25);

        assertEquals(metrics1, metrics2, "ClassMetrics with same offset and length should be equal");
        assertNotEquals(metrics1, metrics3, "ClassMetrics with different values should not be equal");

        assertEquals(metrics1.hashCode(), metrics2.hashCode(), "equal objects should have equal hash codes");
        assertNotEquals(metrics1.hashCode(), metrics3.hashCode(), "different objects should have different hash codes");
    }

    @Test
    @DisplayName("toString includes offset and length values")
    void testToString() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        String toStringResult = metrics.toString();

        assertTrue(toStringResult.contains("offset=10"), "toString should include \"offset=10\": " + toStringResult);
        assertTrue(toStringResult.contains("length=20"), "toString should include \"length=20\": " + toStringResult);
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("ClassMetrics equals returns true for same instance identity comparison")
    void equalsSameObject() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        assertEquals(metrics, metrics, "same object should be equal to itself");
    }

    @Test
    @DisplayName("ClassMetrics equals returns false for null reference input argument comparison")
    void equalsNull() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        assertNotEquals(null, metrics, "ClassMetrics should not be equal to null");
    }

    @Test
    @DisplayName("ClassMetrics equals returns false for different class type comparison")
    void equalsDifferentClass() {
        ClassMetrics metrics = new ClassMetrics(10, 20);
        assertNotEquals("not a ClassMetrics", metrics, "ClassMetrics should not be equal to a String");
    }

    @Test
    @DisplayName("ClassMetrics equals returns false when offset value differs in metrics comparison")
    void equalsOffsetDiffers() {
        ClassMetrics metrics1 = new ClassMetrics(10, 20);
        ClassMetrics metrics2 = new ClassMetrics(15, 20);
        assertNotEquals(metrics1, metrics2, "ClassMetrics with different offset should not be equal");
    }

    @Test
    @DisplayName("ClassMetrics equals returns false when length value differs in metrics comparison")
    void equalsLengthDiffers() {
        ClassMetrics metrics1 = new ClassMetrics(10, 20);
        ClassMetrics metrics2 = new ClassMetrics(10, 25);
        assertNotEquals(metrics1, metrics2, "ClassMetrics with different length should not be equal");
    }
}
