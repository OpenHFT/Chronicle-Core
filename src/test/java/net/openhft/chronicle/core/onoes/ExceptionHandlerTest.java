/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.IgnoresEverything;
import net.openhft.chronicle.core.util.Mocker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerTest extends CoreTestCommon {

    @DisplayName("IgnoresEverything factory returns no op handler")
    @Test
    void ignoresEverything() {
        assertInstanceOf(IgnoresEverything.class, ExceptionHandler.ignoresEverything(),
                "ignoresEverything factory should return a no-op exception handler implementation");
    }

    @DisplayName("Mocker ignored returns no op handler")
    @Test
    void ignoresEverything2() {
        assertInstanceOf(IgnoresEverything.class, Mocker.ignored(ExceptionHandler.class), "Mocker.ignored should return an IgnoresEverything implementation for ExceptionHandler");
    }

    @DisplayName("Handler on class passes logger and throwable")
    @Test
    void onWithClassAndThrowableShouldDelegateProperly() {
        RecordingExceptionHandler handler = new RecordingExceptionHandler();
        Class<?> clazz = this.getClass();
        Throwable thrown = new RuntimeException("onWithClassAndThrowableShouldDelegateProperly");

        handler.on(clazz, thrown);

        assertEquals(clazz.getName(), handler.logger().getName(), "logger name should match class name for on(Class, Throwable)");
        assertEquals("", handler.message(), "default message should be empty");
        assertSame(thrown, handler.thrown(), "thrown should be forwarded for on(Class, Throwable)");
    }

    @DisplayName("Handler on class passes logger and text")
    @Test
    void onWithClassAndMessageShouldDelegateProperly() {
        RecordingExceptionHandler handler = new RecordingExceptionHandler();
        Class<?> clazz = this.getClass();
        String message = "Test message";

        handler.on(clazz, message);

        assertEquals(clazz.getName(), handler.logger().getName(), "logger name should match class name for on(Class, String)");
        assertEquals(message, handler.message(), "message should be forwarded for on(Class, String)");
        assertNull(handler.thrown(), "thrown should be null for on(Class, String)");
    }

    @DisplayName("Handler on logger passes logger and text")
    @Test
    void onWithLoggerAndMessageShouldDelegateProperly() {
        RecordingExceptionHandler handler = new RecordingExceptionHandler();
        Logger logger = LoggerFactory.getLogger("test");
        String message = "Test message";

        handler.on(logger, message);

        assertSame(logger, handler.logger(), "logger instance should be forwarded for on(Logger, String)");
        assertEquals(message, handler.message(), "message should be forwarded for on(Logger, String)");
        assertNull(handler.thrown(), "thrown should be null for on(Logger, String)");
    }

    @DisplayName("Handler isEnabled stays active for any class")
    @Test
    void isEnabledShouldAlwaysReturnTrue() {
        ExceptionHandler handler = new RecordingExceptionHandler();
        assertTrue(handler.isEnabled(this.getClass()), "isEnabled should return true by default for any class");
    }

    @DisplayName("Default handler returns same singleton handler instance")
    @Test
    void defaultHandlerShouldReturnSelf() {
        ExceptionHandler handler = new RecordingExceptionHandler();
        assertSame(handler, handler.defaultHandler(), "defaultHandler should return the handler itself");
    }

    private static final class RecordingExceptionHandler implements ExceptionHandler {
        private Logger logger;
        private String message;
        private Throwable thrown;

        @Override
        public void on(Logger logger, String message, Throwable thrown) {
            this.logger = logger;
            this.message = message;
            this.thrown = thrown;
        }

        Logger logger() {
            return logger;
        }

        String message() {
            return message;
        }

        Throwable thrown() {
            return thrown;
        }
    }
}
