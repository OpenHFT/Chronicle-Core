/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.annotation.UsedViaReflection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilExtraTest {

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

    @Test
    void getField0FindsPrivateFieldInHierarchy() {
        Field f = ClassUtil.getField0(Child.class, "hidden", true, true);
        assertNotNull(f);
        assertEquals("hidden", f.getName());
    }

    @Test
    void getField0ReturnsNullWhenMissingAndErrorFalse() {
        Field f = ClassUtil.getField0(Child.class, "nope", false, true);
        assertNull(f);
    }

    @Test
    void getField0ThrowsWhenMissingAndErrorTrue() {
        assertThrows(AssertionError.class, () -> ClassUtil.getField0(Child.class, "nope", true, true));
    }

    @Test
    void getMethod0FindsPrivateMethodInHierarchy() {
        Method m = ClassUtil.getMethod0(Child.class, "greet", new Class<?>[0], true);
        assertNotNull(m);
        assertEquals("greet", m.getName());
    }
}
