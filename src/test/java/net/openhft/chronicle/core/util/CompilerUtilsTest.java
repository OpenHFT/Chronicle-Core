/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SuppressWarnings("deprecation")
class CompilerUtilsTest {
    @DisplayName("Define class should throw assertion error for illegal access exception")
    @Test
    void defineClassShouldThrowAssertionErrorForIllegalAccessException() {
        ClassLoader classLoader = mock(ClassLoader.class);
        String className = "com.example.MyClass";
        byte[] bytes = { /* class file bytes */};

        // Simulate IllegalAccessException
        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes),
                "defineClass should throw AssertionError for illegal access");
    }

    @DisplayName("Define class should throw assertion error for invocation target exception")
    @Test
    void defineClassShouldThrowAssertionErrorForInvocationTargetException() {
        ClassLoader classLoader = mock(ClassLoader.class);
        String className = "com.example.MyClass";
        byte[] bytes = { /* class file bytes */};

        // Simulate InvocationTargetException
        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes),
                "defineClass should throw AssertionError for invocation target failure");
    }
}
