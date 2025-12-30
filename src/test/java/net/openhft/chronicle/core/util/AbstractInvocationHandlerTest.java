/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

class ConcreteInvocationHandler extends AbstractInvocationHandler {
    ConcreteInvocationHandler() {
        super(String.class); // Example type
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) {
        return "MockResult";
    }
}

class AbstractInvocationHandlerTest extends CoreTestCommon {

    @DisplayName("Invocation handler forwards close to Closeable")
    @Test
    void testCloseable() throws Throwable {
        AbstractInvocationHandler handler = new ConcreteInvocationHandler();
        Closeable mockCloseable = mock(Closeable.class);
        handler.onClose(mockCloseable);

        Method closeMethod = Closeable.class.getMethod("close");
        handler.invoke(mockCloseable, closeMethod, null);

        verify(mockCloseable, times(1)).close();
    }

    @DisplayName("Method handle for proxy abstract invocation")
    @Test
    void testMethodHandleForProxy() throws Throwable {
        assumeTrue(Jvm.majorVersion() >= 17);
        AbstractInvocationHandler handler = new ConcreteInvocationHandler();
        Method exampleMethod = String.class.getMethod("length");

        assertNotNull(handler.methodHandleForProxy("example", exampleMethod), "methodHandleForProxy should return a non-null method handle");
    }

    @DisplayName("Invocation handler intercepts interface method calls")
    @Test
    void testInvoke() {
        final List<String> messages = new ArrayList<>();
        final Consumer<String> consumer = messages::add;
        final CallMe mocked = Mocker.intercepting(CallMe.class, "", consumer);
        mocked.method1();
        mocked.method2();
        assertEquals(2, messages.size(), "interceptor should capture both method invocations");
        assertEquals("method1[]", messages.get(0), "first invocation should be method1 with no arguments");
        assertEquals("method2[]", messages.get(1), "second invocation should be method2 with no arguments");
    }

    @FunctionalInterface
    public interface CallMe {
        void method1();

        default void method2() {
            throw new AssertionError("Don't call me");
        }
    }
}
