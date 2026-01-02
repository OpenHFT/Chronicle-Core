/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassUtilSetAccessibleTest {

    @Test
    @DisplayName("Get method 0 on non public class makes method usable")
    void getMethod0OnNonPublicClassMakesMethodUsable() throws Exception {
        Method m = ClassUtil.getMethod0(PkgClass.class, "greet", new Class<?>[0], true);
        assertNotNull(m, "reflection should find method");
        // The method should now be invokable despite being private
        String s = (String) m.invoke(new PkgClass());
        assertEquals("ok", s, "invoked method should return expected greeting string");
    }

    // package-private class with a private method to trigger setAccessible branch
    static class PkgClass {
        @SuppressWarnings("unused")
        private String greet() {
            return "ok";
        }
    }
}
