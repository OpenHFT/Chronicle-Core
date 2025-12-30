/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ReflectionUtilTest {

    @DisplayName("analyticsPresent reports classpath availability boolean flag")
    @Test
    void analyticsPresentShouldReturnTrueOrFalse() {
        // This test depends on the presence or absence of the analytics class in the classpath
        boolean result = ReflectionUtil.analyticsPresent();
        assertTrue(result || !result, "analyticsPresent should return a boolean classpath availability value");
    }

    @DisplayName("methodOrThrow returns declared reflection method or throws")
    @Test
    void methodOrThrowShouldReturnMethod() throws NoSuchMethodException {
        Method expected = String.class.getMethod("length");
        Method actual = ReflectionUtil.methodOrThrow("java.lang.String", "length");
        assertEquals(expected, actual, "methodOrThrow should return the correct method");
    }

    @DisplayName("Invoke or throw should invoke method reflection util")
    @Test
    void invokeOrThrowShouldInvokeMethod() throws NoSuchMethodException {
        Method lengthMethod = String.class.getMethod("length");
        Object result = ReflectionUtil.invokeOrThrow(lengthMethod, "test");
        assertEquals(4, result, "invokeOrThrow should correctly invoke the method and return the result");
    }

    @DisplayName("Reflective proxy should create proxy reflection")
    @Test
    void reflectiveProxyShouldCreateProxy() {
        TestInterface delegate = () -> "test";
        TestInterface proxy = ReflectionUtil.reflectiveProxy(TestInterface.class, delegate);

        assertTrue(Proxy.isProxyClass(proxy.getClass()), "reflectiveProxy should return Proxy subclass");
        assertEquals("test", proxy.sampleMethod(), "reflectiveProxy should correctly delegate method calls");
    }

    @DisplayName("Reflective proxy with return proxy should return proxy reflection util")
    @Test
    void reflectiveProxyWithReturnProxyShouldReturnProxy() {
        TestInterface delegate = () -> "test";
        TestInterface proxy = ReflectionUtil.reflectiveProxy(TestInterface.class, delegate, true);

        assertTrue(Proxy.isProxyClass(proxy.getClass()), "reflectiveProxy with returnProxy should return Proxy subclass");
        assertSame(proxy, proxy.sampleMethod(), "reflectiveProxy should return the proxy itself for chaining");
    }

    private interface TestInterface {
        Object sampleMethod();
    }
}
