/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilSetAccessibleTest {

    // package-private class with a private method to trigger setAccessible branch
    static class PkgClass {
        @SuppressWarnings("unused")
        private String greet() {
            return "ok";
        }
    }

    @Test
    void getMethod0OnNonPublicClassMakesMethodUsable() throws Exception {
        Method m = ClassUtil.getMethod0(PkgClass.class, "greet", new Class<?>[0], true);
        assertNotNull(m);
        // The method should now be invokable despite being private
        String s = (String) m.invoke(new PkgClass());
        assertEquals("ok", s);
    }
}
