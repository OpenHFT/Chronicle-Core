/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.IgnoresEverything;
import net.openhft.chronicle.core.util.Mocker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

public class ExceptionHandlerTest extends CoreTestCommon {

    @BeforeEach
    public void mockitoNotSupportedOnJava21() {
        assumeTrue(Jvm.majorVersion() <= 17);
    }

    @Test
    public void ignoresEverything() {
        assertTrue(ExceptionHandler.ignoresEverything() instanceof IgnoresEverything);
    }

    @Test
    public void ignoresEverything2() {
        assertTrue(Mocker.ignored(ExceptionHandler.class) instanceof IgnoresEverything);
    }

    @Test
    public void onWithClassAndThrowableShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Class<?> clazz = this.getClass();
        Throwable thrown = new RuntimeException();

        handler.on(clazz, thrown);

        verify(handler).on(clazz, "", thrown);
    }

    @Test
    public void onWithClassAndMessageShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Class<?> clazz = this.getClass();
        String message = "Test message";

        handler.on(clazz, message);

        verify(handler).on(clazz, message, null);
    }

    @Test
    public void onWithLoggerAndMessageShouldDelegateProperly() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        Logger logger = mock(Logger.class);
        String message = "Test message";

        handler.on(logger, message);

        verify(handler).on(logger, message, null);
    }

    @Test
    public void isEnabledShouldAlwaysReturnTrue() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        assertTrue(handler.isEnabled(this.getClass()));
    }

    @Test
    public void defaultHandlerShouldReturnSelf() {
        ExceptionHandler handler = mock(ExceptionHandler.class, CALLS_REAL_METHODS);
        assertSame(handler, handler.defaultHandler());
    }
}
