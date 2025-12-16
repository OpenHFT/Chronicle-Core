/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassUtilSetAccessibleTest {

    @Test
    void getMethod0OnNonPublicClassMakesMethodUsable() throws Exception {
        Method m = ClassUtil.getMethod0(PkgClass.class, "greet", new Class<?>[0], true);
        assertNotNull(m, "getMethod0OnNonPublicClassMakesMethodUsable: L18");
        // The method should now be invokable despite being private
        String s = (String) m.invoke(new PkgClass());
        assertEquals("ok", s, "getMethod0OnNonPublicClassMakesMethodUsable: L21");
    }

    // package-private class with a private method to trigger setAccessible branch
    static class PkgClass {
        @SuppressWarnings("unused")
        private String greet() {
            return "ok";
        }
    }
}
