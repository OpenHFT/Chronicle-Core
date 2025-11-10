//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void testUnwrapExceptionHandler() {
        assertSame(defaultHandler, ThreadLocalisedExceptionHandler.unwrap(tlExceptionHandler));
    }

    @Test
    void testIsEnabled() {
        when(defaultHandler.isEnabled(Exception.class)).thenReturn(true);
        assertTrue(tlExceptionHandler.isEnabled(Exception.class));
    }

    @Test
    void threadLocalOverrideTakesPrecedenceAndResetFallsBack() {
        ExceptionHandler override = mock(ExceptionHandler.class);
        RuntimeException boom = new RuntimeException("boom");

        tlExceptionHandler.threadLocalHandler(override);
        tlExceptionHandler.on(ThreadLocalisedExceptionHandlerTest.class, "ctx", boom);

        verify(override).on(ThreadLocalisedExceptionHandlerTest.class, "ctx", boom);
        verifyNoInteractions(defaultHandler);

        tlExceptionHandler.resetThreadLocalHandler();
        tlExceptionHandler.on(ThreadLocalisedExceptionHandlerTest.class, "ctx2", null);

        verify(defaultHandler).on(ThreadLocalisedExceptionHandlerTest.class, "ctx2", null);
    }

    @Test
    void nullThreadLocalHandlerSilentlyDropsLoggerCalls() {
        Logger logger = mock(Logger.class);

        tlExceptionHandler.threadLocalHandler(null);
        tlExceptionHandler.on(logger, "silence", null);

        verifyNoInteractions(defaultHandler);
        verifyNoInteractions(logger);
    }

    @Test
    void preservesThreadInterruptStatusAroundDelegation() {
        RuntimeException boom = new RuntimeException("boom");
        Thread.currentThread().interrupt();

        tlExceptionHandler.on(ThreadLocalisedExceptionHandlerTest.class, "restore", boom);

        assertTrue(Thread.currentThread().isInterrupted(), "interrupt flag must be restored");
        Thread.interrupted(); // clean up for other tests
        verify(defaultHandler).on(ThreadLocalisedExceptionHandlerTest.class, "restore", boom);
    }
}
