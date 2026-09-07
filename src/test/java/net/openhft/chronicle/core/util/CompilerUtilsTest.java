/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerUtilsTest {
    @Test
    void defineClassShouldThrowAssertionErrorForInvalidClassBytes() {
        ClassLoader classLoader = new ClassLoader() {
        };
        String className = "com.example.MyClass";
        byte[] bytes = new byte[] { /* class file bytes */ };

        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes));
    }

    @Test
    void defineClassShouldThrowAssertionErrorForMalformedClassBytes() {
        ClassLoader classLoader = new ClassLoader() {
        };
        String className = "com.example.MyClass";
        byte[] bytes = new byte[] {0x1, 0x2, 0x3};

        assertThrows(AssertionError.class, () -> CompilerUtils.defineClass(classLoader, className, bytes));
    }
}
