/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.annotation.UsedViaReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilExtraTest {

    @DisplayName("getField0FindsPrivateFieldInHierarchy behaviour under expected input and output conditions")
    @Test
    void getField0FindsPrivateFieldInHierarchy() {
        Field f = ClassUtil.getField0(Child.class, "hidden", true, true);
        assertNotNull(f, "reflection should find field");
        assertEquals("hidden", f.getName(), "field name should be 'hidden' when found in parent class");
    }

    @DisplayName("getField0ReturnsNullWhenMissingAndErrorFalse behaviour under expected input and output conditions")
    @Test
    void getField0ReturnsNullWhenMissingAndErrorFalse() {
        Field f = ClassUtil.getField0(Child.class, "nope", false, true);
        assertNull(f, "requested field should not exist when not present");
    }

    @DisplayName("getField0ThrowsWhenMissingAndErrorTrue behaviour under expected input and output conditions")
    @Test
    void getField0ThrowsWhenMissingAndErrorTrue() {
        assertThrows(AssertionError.class, () -> ClassUtil.getField0(Child.class, "nope", true, true),
                "getField0 should throw when missing field is requested with error flag true");
    }

    @DisplayName("getMethod0FindsPrivateMethodInHierarchy behaviour under expected input and output conditions")
    @Test
    void getMethod0FindsPrivateMethodInHierarchy() {
        Method m = ClassUtil.getMethod0(Child.class, "greet", new Class<?>[0], true);
        assertNotNull(m, "reflection should find method");
        assertEquals("greet", m.getName(), "method name should be 'greet' when found in parent class");
    }

    private static class Parent {
        @UsedViaReflection
        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private int hidden = 42;

        @SuppressWarnings("unused")
        private String greet() {
            return "hi";
        }
    }

    private static class Child extends Parent {
    }
}
