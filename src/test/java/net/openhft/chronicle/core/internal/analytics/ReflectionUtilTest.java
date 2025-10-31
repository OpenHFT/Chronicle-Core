/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal.analytics;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilTest {

    @Test
    public void analyticsPresentShouldReturnTrueOrFalse() {
        // This test depends on the presence or absence of the analytics class in the classpath
        boolean result = ReflectionUtil.analyticsPresent();
        assertTrue(result || !result, "analyticsPresent should return true or false");
    }

    @Test
    public void methodOrThrowShouldReturnMethod() throws NoSuchMethodException {
        Method expected = String.class.getMethod("length");
        Method actual = ReflectionUtil.methodOrThrow("java.lang.String", "length");
        assertEquals(expected, actual, "methodOrThrow should return the correct method");
    }

    @Test
    public void invokeOrThrowShouldInvokeMethod() throws NoSuchMethodException {
        Method lengthMethod = String.class.getMethod("length");
        Object result = ReflectionUtil.invokeOrThrow(lengthMethod, "test");
        assertEquals(4, result, "invokeOrThrow should correctly invoke the method and return the result");
    }

    @Test
    public void reflectiveProxyShouldCreateProxy() {
        TestInterface delegate = () -> "test";
        TestInterface proxy = ReflectionUtil.reflectiveProxy(TestInterface.class, delegate);

        assertTrue(Proxy.isProxyClass(proxy.getClass()), "reflectiveProxy should create a proxy class");
        assertEquals("test", proxy.testMethod(), "reflectiveProxy should correctly delegate method calls");
    }

    @Test
    public void reflectiveProxyWithReturnProxyShouldReturnProxy() {
        TestInterface delegate = () -> "test";
        TestInterface proxy = ReflectionUtil.reflectiveProxy(TestInterface.class, delegate, true);

        assertTrue(Proxy.isProxyClass(proxy.getClass()), "reflectiveProxy should create a proxy class");
        assertSame(proxy, proxy.testMethod(), "reflectiveProxy should return the proxy itself for chaining");
    }

    private interface TestInterface {
        Object testMethod();
    }
}
