/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.test.RecordingLogger;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class Slf4jExceptionHandlerTest {

    private RecordingLogger logger;

    @BeforeEach
    void setUp() {
        logger = new RecordingLogger("Slf4jExceptionHandlerTest");
    }

    @Test
    void testErrorLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.ERROR.on(logger.logger(), "Error message", throwable);

        assertEquals(1, logger.callCount("error"));
    }

    @Test
    void testWarnLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.WARN.on(logger.logger(), "Warn message", throwable);

        assertEquals(1, logger.callCount("warn"));
    }

    @Test
    void testPerfLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.PERF.on(logger.logger(), "Perf message", throwable);

        assertEquals(1, logger.callCount("info"));
    }

    @Test
    void testDebugLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.DEBUG.on(logger.logger(), "Debug message", throwable);

        assertEquals(1, logger.callCount("debug"));
    }

    @Test
    void testValueOfLogLevel() {
        assertEquals(Slf4jExceptionHandler.ERROR, Slf4jExceptionHandler.valueOf(LogLevel.ERROR));
        assertEquals(Slf4jExceptionHandler.WARN, Slf4jExceptionHandler.valueOf(LogLevel.WARN));
        assertEquals(Slf4jExceptionHandler.PERF, Slf4jExceptionHandler.valueOf(LogLevel.PERF));
        assertEquals(Slf4jExceptionHandler.DEBUG, Slf4jExceptionHandler.valueOf(LogLevel.DEBUG));
    }

    @Test
    void testDirectLoggerOverrideThrowsOnce() {
        // 1. Create a real exception instance to throw
        RuntimeException boom = new RuntimeException("boom");

        // 2. Make a logger that throws when error(String, Throwable) is invoked
        RecordingLogger bad = new RecordingLogger("bad");
        bad.throwFrom("error", boom);

        // 3. Writes to stderr, but doesn't throw an exception
        assertDoesNotThrow(() -> Slf4jExceptionHandler.ERROR.on(bad.logger(), "msg", boom));
        assertEquals(1, bad.callCount("error"));
    }

    @Test
    void testOnClassSucceedsUnderNormalConditions() {
        // Should never throw (uses the same DEFAULT logger, which is healthy)
        assertDoesNotThrow(() ->
                Slf4jExceptionHandler.WARN.on(Slf4jExceptionHandlerTest.class, "all good", null)
        );
        assertDoesNotThrow(() ->
                Slf4jExceptionHandler.ERROR.on(Slf4jExceptionHandlerTest.class, "all good", null)
        );
    }
}
