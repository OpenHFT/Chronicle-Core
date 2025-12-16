/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import org.junit.jupiter.api.BeforeEach;
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

@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate") // JUnit4 annotations require public class
public class AbstractInvocationHandlerTest extends CoreTestCommon {

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        AbstractInvocationHandler handler = new ConcreteInvocationHandler();
        Method exampleMethod = String.class.getMethod("length");
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

        assertNotNull(handler.methodHandleForProxy("example", exampleMethod), "testMethodHandleForProxy: L60");
    }

    @Test
    public void testInvoke() {
        final List<String> messages = new ArrayList<>();
        final Consumer<String> consumer = messages::add;
        final CallMe mocked = Mocker.intercepting(CallMe.class, "", consumer);
        mocked.method1();
        mocked.method2();
        assertEquals(2, messages.size(), "testInvoke: L70");
        assertEquals("method1[]", messages.get(0), "testInvoke: L71");
        assertEquals("method2[]", messages.get(1), "testInvoke: L72");
    }

    @FunctionalInterface
    public interface CallMe {
        void method1();

        default void method2() {
            throw new AssertionError("Don't call me");
        }
    }
}
