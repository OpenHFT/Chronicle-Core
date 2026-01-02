/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThreadLocalisedExceptionHandlerTest {

    private ExceptionHandler defaultHandler;
    private ThreadLocalisedExceptionHandler tlExceptionHandler;

    @BeforeEach
    void setUp() {
        defaultHandler = mock(ExceptionHandler.class);
        tlExceptionHandler = new ThreadLocalisedExceptionHandler(defaultHandler);
    }

    @Test
    @DisplayName("Unwrap returns wrapped default exception handler")
    void testUnwrapExceptionHandler() {
        assertSame(defaultHandler, ThreadLocalisedExceptionHandler.unwrap(tlExceptionHandler), "unwrap should return the wrapped default handler");
    }

    @Test
    @DisplayName("isEnabled delegates to default exception handler")
    void testIsEnabled() {
        when(defaultHandler.isEnabled(Exception.class)).thenReturn(true);
        assertTrue(tlExceptionHandler.isEnabled(Exception.class), "isEnabled should delegate to default handler and return true for Exception class");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("on(Class) with null handler returns early")
    void onClassWithNullHandler() {
        // Set thread-local to null handler which should cause early return
        tlExceptionHandler.threadLocalHandler(NullExceptionHandler.NOTHING);
        tlExceptionHandler.on(Exception.class, "test", null);
        verify(defaultHandler, never()).on(any(Class.class), anyString(), any());
    }

    @Test
    @DisplayName("on(Class) preserves interrupt status")
    void onClassPreservesInterrupt() {
        Thread.currentThread().interrupt();
        try {
            tlExceptionHandler.on(Exception.class, "test", null);
            assertTrue(Thread.currentThread().isInterrupted(),
                    "interrupt status should be preserved after on() call");
        } finally {
            Thread.interrupted(); // Clear interrupt
        }
    }

    @Test
    @DisplayName("on(Logger) with null handler returns early")
    void onLoggerWithNullHandler() {
        Logger logger = LoggerFactory.getLogger(getClass());
        tlExceptionHandler.threadLocalHandler(NullExceptionHandler.NOTHING);
        tlExceptionHandler.on(logger, "test", null);
        verify(defaultHandler, never()).on(any(Logger.class), anyString(), any());
    }

    @Test
    @DisplayName("on(Logger) with IgnoresEverything handler returns early")
    void onLoggerWithIgnoresEverything() {
        Logger logger = LoggerFactory.getLogger(getClass());
        ExceptionHandler ignoring = mock(ExceptionHandler.class, withSettings().extraInterfaces(IgnoresEverything.class));
        tlExceptionHandler.threadLocalHandler(ignoring);
        tlExceptionHandler.on(logger, "test", null);
        verify(ignoring, never()).on(any(Logger.class), anyString(), any());
    }

    @Test
    @DisplayName("on(Logger) preserves interrupt status")
    void onLoggerPreservesInterrupt() {
        Logger logger = LoggerFactory.getLogger(getClass());
        Thread.currentThread().interrupt();
        try {
            tlExceptionHandler.on(logger, "test", null);
            assertTrue(Thread.currentThread().isInterrupted(),
                    "interrupt status should be preserved after on(Logger) call");
        } finally {
            Thread.interrupted(); // Clear interrupt
        }
    }

    @Test
    @DisplayName("unwrap returns same handler if not ThreadLocalisedExceptionHandler")
    void unwrapNonThreadLocalised() {
        ExceptionHandler handler = mock(ExceptionHandler.class);
        assertSame(handler, ThreadLocalisedExceptionHandler.unwrap(handler),
                "unwrap should return same handler for non-ThreadLocalised instance");
    }

    @Test
    @DisplayName("defaultHandler(null) sets NullExceptionHandler.NOTHING")
    void defaultHandlerNull() {
        tlExceptionHandler.defaultHandler(null);
        assertSame(NullExceptionHandler.NOTHING, tlExceptionHandler.defaultHandler(),
                "setting null should use NullExceptionHandler.NOTHING");
    }

    @Test
    @DisplayName("defaultHandler unwraps ThreadLocalisedExceptionHandler")
    void defaultHandlerUnwraps() {
        ExceptionHandler inner = mock(ExceptionHandler.class);
        ThreadLocalisedExceptionHandler wrapped = new ThreadLocalisedExceptionHandler(inner);
        tlExceptionHandler.defaultHandler(wrapped);
        // The inner handler should be used after unwrapping
        assertSame(inner, tlExceptionHandler.defaultHandler(),
                "defaultHandler should unwrap ThreadLocalisedExceptionHandler");
    }

    @Test
    @DisplayName("defaultHandler rejects recursive ChainedExceptionHandler")
    void defaultHandlerRejectsRecursive() {
        ChainedExceptionHandler chain = mock(ChainedExceptionHandler.class);
        when(chain.chain()).thenReturn(new ExceptionHandler[]{tlExceptionHandler});
        assertThrows(AssertionError.class,
                () -> tlExceptionHandler.defaultHandler(chain),
                "defaultHandler should reject chained handler containing this");
    }

    @Test
    @DisplayName("threadLocalHandler(null) uses NullExceptionHandler.NOTHING")
    @SuppressWarnings("deprecation")
    void threadLocalHandlerNull() {
        tlExceptionHandler.threadLocalHandler(null);
        assertSame(NullExceptionHandler.NOTHING, tlExceptionHandler.threadLocalHandler(),
                "setting null should use NullExceptionHandler.NOTHING");
    }

    @Test
    @DisplayName("threadLocalHandler sets and retrieves handler")
    @SuppressWarnings("deprecation")
    void threadLocalHandlerSetAndGet() {
        ExceptionHandler local = mock(ExceptionHandler.class);
        tlExceptionHandler.threadLocalHandler(local);
        assertSame(local, tlExceptionHandler.threadLocalHandler(),
                "threadLocalHandler should return the set handler");
    }

    @Test
    @DisplayName("resetThreadLocalHandler clears thread-local")
    @SuppressWarnings("deprecation")
    void resetThreadLocalHandlerClears() {
        ExceptionHandler local = mock(ExceptionHandler.class);
        tlExceptionHandler.threadLocalHandler(local);
        tlExceptionHandler.resetThreadLocalHandler();
        assertNull(tlExceptionHandler.threadLocalHandler(),
                "threadLocalHandler should be null after reset");
    }

    @Test
    @DisplayName("isEnabled delegates to NullExceptionHandler")
    void isEnabledWithNullHandler() {
        tlExceptionHandler.resetThreadLocalHandler();
        // Set default handler to NullExceptionHandler.NOTHING
        tlExceptionHandler.defaultHandler(NullExceptionHandler.NOTHING);
        // NullExceptionHandler.isEnabled returns false
        assertFalse(tlExceptionHandler.isEnabled(Exception.class),
                "isEnabled should delegate to NullExceptionHandler which returns false");
    }

    @Test
    @DisplayName("Thread-local handler overrides default")
    void threadLocalOverridesDefault() {
        ExceptionHandler local = mock(ExceptionHandler.class);
        when(local.isEnabled(any())).thenReturn(false);
        when(defaultHandler.isEnabled(any())).thenReturn(true);

        tlExceptionHandler.threadLocalHandler(local);

        assertFalse(tlExceptionHandler.isEnabled(Exception.class),
                "thread-local handler should override default");
        verify(local).isEnabled(Exception.class);
        verify(defaultHandler, never()).isEnabled(any());
    }

    @Test
    @DisplayName("defaultHandler getter returns the current default")
    void defaultHandlerGetter() {
        assertSame(defaultHandler, tlExceptionHandler.defaultHandler(),
                "defaultHandler() should return the current default handler");
    }
}
