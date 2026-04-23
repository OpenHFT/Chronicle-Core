/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.test.RecordingExceptionHandlerStub;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChainedExceptionHandlerTest {

    private RecordingExceptionHandlerStub handler1;
    private RecordingExceptionHandlerStub handler2;
    private ChainedExceptionHandler chainedHandler;

    @BeforeEach
    void setUp() {
        handler1 = new RecordingExceptionHandlerStub();
        handler2 = new RecordingExceptionHandlerStub();
        chainedHandler = new ChainedExceptionHandler(handler1, handler2);
    }

    @Test
    void testChainExecution() {
        Throwable throwable = new RuntimeException("Test");
        chainedHandler.on(Exception.class, "Test message", throwable);

        assertEquals(1, handler1.eventCount());
        assertEquals(Exception.class, handler1.event(0).clazz());
        assertEquals("Test message", handler1.event(0).message());
        assertSame(throwable, handler1.event(0).thrown());
        assertEquals(1, handler2.eventCount());
        assertEquals(Exception.class, handler2.event(0).clazz());
        assertEquals("Test message", handler2.event(0).message());
        assertSame(throwable, handler2.event(0).thrown());
    }

    @Test
    void onWithClassShouldCallEachHandler() {
        List<String> order = new ArrayList<>();
        ExceptionHandler firstHandler = new OrderedClassExceptionHandler(order, "first");
        ExceptionHandler secondHandler = new OrderedClassExceptionHandler(order, "second");
        ChainedExceptionHandler chained = new ChainedExceptionHandler(firstHandler, secondHandler);

        Class<?> clazz = String.class;
        String message = "Test message";
        Throwable thrown = new RuntimeException();

        chained.on(clazz, message, thrown);

        assertEquals(Arrays.asList("first", "second"), order);
    }

    @Test
    void onShouldCatchExceptionsFromHandlers() {
        ExceptionHandler faultyHandler = (clazz, msg, thr) -> { throw new RuntimeException("Handler error"); };
        ChainedExceptionHandler chained = new ChainedExceptionHandler(faultyHandler);

        // This call should not throw an exception
        assertDoesNotThrow(() -> chained.on(String.class, "message", new RuntimeException()));
    }

    private static final class OrderedClassExceptionHandler implements ExceptionHandler {
        private final List<String> order;
        private final String name;

        private OrderedClassExceptionHandler(List<String> order, String name) {
            this.order = order;
            this.name = name;
        }

        @Override
        public void on(@NotNull Class<?> clazz, String message, Throwable thrown) {
            order.add(name);
        }

        @Override
        public void on(org.slf4j.@NotNull Logger logger, String message, Throwable thrown) {
            throw new AssertionError("Logger overload should not be used");
        }
    }
}
