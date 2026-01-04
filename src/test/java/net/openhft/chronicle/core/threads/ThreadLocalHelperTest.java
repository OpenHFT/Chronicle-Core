/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ThreadLocalHelperTest {

    @Test
    @DisplayName("Get TL with supplier thread local")
    void testGetTLWithSupplier() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value, "first getTL call should create and return value via supplier");
        assertEquals("Value1", ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet()), "getTL should return cached value (supplier not invoked)");
    }

    @Test
    @DisplayName("getSTL caches supplier value in thread local")
    void testGetSTL() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);
        String value = ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet());

        assertEquals("Value1", value, "first getSTL call should create and return value via supplier");
        assertEquals("Value1", ThreadLocalHelper.getSTL(threadLocal, () -> "Value" + counter.incrementAndGet()), "getSTL should return cached value (supplier not invoked)");
    }

    @Test
    @DisplayName("Get TL with function thread local")
    void testGetTLWithFunction() {
        ThreadLocal<WeakReference<AtomicInteger>> threadLocal = new ThreadLocal<>();
        String input = "123";
        AtomicInteger value = ThreadLocalHelper.getTL(threadLocal, input,
                s -> new AtomicInteger(Integer.parseInt(s)));

        assertEquals(123, value.get(), "first getTL call should parse and return value");
        AtomicInteger cached = ThreadLocalHelper.getTL(threadLocal, "456",
                s -> new AtomicInteger(Integer.parseInt(s)));
        assertSame(value, cached, "getTL should return cached value (function not invoked)");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("getTL with referenceQueue and registrar registers weak reference")
    void testGetTLWithReferenceQueueAndRegistrar() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        ReferenceQueue<String> queue = new ReferenceQueue<>();
        List<WeakReference<String>> registered = new ArrayList<>();

        String value = ThreadLocalHelper.getTL(threadLocal, "input", s -> "Created:" + s, queue, registered::add);

        assertEquals("Created:input", value, "getTL should create value using constructor for reference queue");
        assertEquals(1, registered.size(), "registrar should have been called once");
        assertNotNull(registered.get(0), "registered weak reference should not be null");
    }

    @Test
    @DisplayName("getTL with null referenceQueue uses simple weak reference")
    void testGetTLWithNullReferenceQueue() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        List<WeakReference<String>> registered = new ArrayList<>();

        String value = ThreadLocalHelper.getTL(threadLocal, "input", s -> "Created:" + s, null, registered::add);

        assertEquals("Created:input", value, "getTL should create value with null reference queue");
        assertEquals(0, registered.size(), "registrar should not have been called when queue is null");
    }

    @Test
    @DisplayName("getTL with null registrar uses simple weak reference")
    void testGetTLWithNullRegistrar() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        ReferenceQueue<String> queue = new ReferenceQueue<>();

        String value = ThreadLocalHelper.getTL(threadLocal, "input", s -> "Created:" + s, queue, null);

        assertEquals("Created:input", value, "getTL should create value when registrar is null");
    }

    @Test
    @DisplayName("getTL recreates value when weak reference is cleared")
    void testGetTLRecreatesWhenCleared() {
        ThreadLocal<WeakReference<String>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);

        // First call creates value
        String value1 = ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet());
        assertEquals("Value1", value1, "first call should create value from supplier");

        // Clear the weak reference manually
        threadLocal.get().clear();

        // Second call should recreate since weak reference was cleared
        String value2 = ThreadLocalHelper.getTL(threadLocal, () -> "Value" + counter.incrementAndGet());
        assertEquals("Value2", value2, "second call should recreate value after cleared weak reference");
    }

    @Test
    @DisplayName("getTL with function recreates value when weak reference is cleared")
    void testGetTLWithFunctionRecreatesWhenCleared() {
        ThreadLocal<WeakReference<AtomicInteger>> threadLocal = new ThreadLocal<>();
        AtomicInteger counter = new AtomicInteger(0);

        // First call creates value
        AtomicInteger value1 = ThreadLocalHelper.getTL(threadLocal, 10,
                n -> new AtomicInteger(n + counter.incrementAndGet()));
        assertEquals(11, value1.get(), "first call should create value from function");

        // Clear the weak reference manually
        threadLocal.get().clear();

        // Second call should recreate since weak reference was cleared
        AtomicInteger value2 = ThreadLocalHelper.getTL(threadLocal, 10,
                n -> new AtomicInteger(n + counter.incrementAndGet()));
        assertEquals(12, value2.get(),
                "second call should recreate value after cleared weak reference using function");
        assertNotSame(value1, value2,
                "second call should return new AtomicInteger instance after weak reference clear");
    }
}
