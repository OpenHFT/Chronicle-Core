/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.io.Closeable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

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
