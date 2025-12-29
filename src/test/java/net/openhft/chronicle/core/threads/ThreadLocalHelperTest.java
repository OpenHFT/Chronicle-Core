/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
class ThreadLocalHelperTest {

    @DisplayName("testGetTLWithSupplier behaviour under expected input and output conditions")
    @Test
    void testGetTLWithSupplier() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value, "first getTL call should create and return value via supplier");
        assertEquals("Value1", ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet()), "getTL should return cached value (supplier not invoked)");
    }

    @DisplayName("testGetSTL behaviour under expected input and output conditions")
    @Test
    void testGetSTL() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value, "first getSTL call should create and return value via supplier");
        assertEquals("Value1", ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet()), "getSTL should return cached value (supplier not invoked)");
    }

    @DisplayName("testGetTLWithFunction behaviour under expected input and output conditions")
    @Test
    void testGetTLWithFunction() {
        ThreadLocal<WeakReference<Integer>> threadLocal = new ThreadLocal<>();
        String input = "123";
        Integer value = ThreadLocalHelper.getTL(threadLocal, input, Integer::valueOf);

        assertEquals(123, value, "first getTL call with function should parse and return value");
        assertEquals(123, ThreadLocalHelper.getTL(threadLocal, "456", Integer::valueOf), "getTL should return cached value (function not invoked)");
    }
}
