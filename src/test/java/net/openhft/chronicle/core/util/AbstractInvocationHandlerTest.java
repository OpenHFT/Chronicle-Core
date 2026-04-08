/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class AbstractInvocationHandlerTest extends CoreTestCommon {

    private AbstractInvocationHandler handler;
    private Method exampleMethod;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        handler = new ConcreteInvocationHandler();
        exampleMethod = String.class.getMethod("length");
    }

    @Test
    public void testCloseable() throws Throwable {
        AbstractInvocationHandler handler = new ConcreteInvocationHandler();
        Closeable mockCloseable = mock(Closeable.class);
        handler.onClose(mockCloseable);

        Method closeMethod = Closeable.class.getMethod("close");
        handler.invoke(mockCloseable, closeMethod, null);

        verify(mockCloseable, times(1)).close();
    }

    @Test
    public void testMethodHandleForProxy() throws Throwable {
        assumeTrue(Jvm.majorVersion() >= 17);
        AbstractInvocationHandler handler = new ConcreteInvocationHandler();
        Method exampleMethod = String.class.getMethod("length");

        assertNotNull(handler.methodHandleForProxy("example", exampleMethod));
    }

    @Test
    public void testInvoke() {
        final List<String> messages = new ArrayList<>();
        final Consumer<String> consumer = s -> messages.add(s);
        final CallMe mocked = Mocker.intercepting(CallMe.class, "", consumer);
        mocked.method1();
        mocked.method2();
        assertEquals(2, messages.size());
        assertEquals("method1[]", messages.get(0));
        assertEquals("method2[]", messages.get(1));
    }

    @FunctionalInterface
    public interface CallMe {
        void method1();

        default void method2() {
            throw new AssertionError("Don't call me");
        }
    }
}
