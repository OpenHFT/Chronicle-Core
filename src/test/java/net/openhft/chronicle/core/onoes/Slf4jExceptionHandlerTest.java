/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class Slf4jExceptionHandlerTest {

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
    }

    @Test
    void testErrorLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.ERROR.on(logger, "Error message", throwable);

        verify(logger).error("Error message", throwable);
    }

    @Test
    void testWarnLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.WARN.on(logger, "Warn message", throwable);

        verify(logger).warn("Warn message", throwable);
    }

    @Test
    void testPerfLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.PERF.on(logger, "Perf message", throwable);

        verify(logger).info("Perf message", throwable);
    }

    @Test
    void testDebugLogLevel() {
        Throwable throwable = new RuntimeException("Test exception");
        Slf4jExceptionHandler.DEBUG.on(logger, "Debug message", throwable);

        verify(logger).debug("Debug message", throwable);
    }

    @Test
    void testValueOfLogLevel() {
        assertEquals(Slf4jExceptionHandler.ERROR, Slf4jExceptionHandler.valueOf(LogLevel.ERROR), "valueOf should return ERROR handler for ERROR log level");
        assertEquals(Slf4jExceptionHandler.WARN, Slf4jExceptionHandler.valueOf(LogLevel.WARN), "valueOf should return WARN handler for WARN log level");
        assertEquals(Slf4jExceptionHandler.PERF, Slf4jExceptionHandler.valueOf(LogLevel.PERF), "valueOf should return PERF handler for PERF log level");
        assertEquals(Slf4jExceptionHandler.DEBUG, Slf4jExceptionHandler.valueOf(LogLevel.DEBUG), "valueOf should return DEBUG handler for DEBUG log level");
    }

    @Test
    void testDirectLoggerOverrideThrowsOnce() {
        // 1. Create a real exception instance to throw
        RuntimeException boom = new RuntimeException("boom");

        // 2. Make a logger that throws when error(String, Throwable) is invoked
        Logger bad = mock(Logger.class);
        doThrow(boom)
                .when(bad)
                .error(anyString(), same(boom));

        // 3. Writes to stderr, but doesn't throw an exception
        Slf4jExceptionHandler.ERROR.on(bad, "msg", boom);
        assertTrue(true, "execution should reach this point without exception"); // if we reach here, the test passes
    }

    @Test
    void testOnClassSucceedsUnderNormalConditions() {
        // Should never throw (uses the same DEFAULT logger, which is healthy)
        assertDoesNotThrow(() ->
                Slf4jExceptionHandler.WARN.on(Slf4jExceptionHandlerTest.class, "all good", null),
                "warn handler should not throw");
        assertDoesNotThrow(() ->
                Slf4jExceptionHandler.ERROR.on(Slf4jExceptionHandlerTest.class, "all good", null),
                "error handler should not throw");
    }
}
