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
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ChainedExceptionHandlerTest {

    private ExceptionHandler handler1;
    private ExceptionHandler handler2;
    private ChainedExceptionHandler chainedHandler;

    @BeforeEach
    void setUp() {
        handler1 = mock(ExceptionHandler.class);
        handler2 = mock(ExceptionHandler.class);
        chainedHandler = new ChainedExceptionHandler(handler1, handler2);
    }

    @Test
    void testChainExecution() {
        Throwable throwable = new RuntimeException("Test");
        chainedHandler.on(Exception.class, "Test message", throwable);

        verify(handler1).on(Exception.class, "Test message", throwable);
        verify(handler2).on(Exception.class, "Test message", throwable);
    }

    @Test
    void onWithClassShouldCallEachHandler() {
        ExceptionHandler firstHandler = mock(ExceptionHandler.class);
        ExceptionHandler secondHandler = mock(ExceptionHandler.class);
        ChainedExceptionHandler chained = new ChainedExceptionHandler(firstHandler, secondHandler);

        Class<?> clazz = String.class;
        String message = "Test message";
        Throwable thrown = new RuntimeException();

        chained.on(clazz, message, thrown);

        InOrder inOrder = inOrder(firstHandler, secondHandler);
        inOrder.verify(firstHandler).on(clazz, message, thrown);
        inOrder.verify(secondHandler).on(clazz, message, thrown);
    }

    @Test
    void onShouldCatchExceptionsFromHandlers() {
        ExceptionHandler faultyHandler = (clazz, msg, thr) -> { throw new RuntimeException("Handler error"); };
        ChainedExceptionHandler chained = new ChainedExceptionHandler(faultyHandler);

        // This call should not throw an exception
        assertDoesNotThrow(() -> chained.on(String.class, "message", new RuntimeException()));
    }
}
