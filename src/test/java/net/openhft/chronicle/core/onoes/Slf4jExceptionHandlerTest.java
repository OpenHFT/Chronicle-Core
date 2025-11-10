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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        Logger bad = mock(Logger.class);
        doThrow(boom)
                .when(bad)
                .error(anyString(), same(boom));

        // 3. Writes to stderr, but doesn't throw an exception
        Slf4jExceptionHandler.ERROR.on(bad, "msg", boom);
        assertTrue(true); // if we reach here, the test passes
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

    @Test
    void fallbackWritesPlainAsciiWhenLoggerThrows() throws Exception {
        RuntimeException original = new RuntimeException("original");
        Logger failingLogger = mock(Logger.class);
        doThrow(new IllegalStateException("logger offline"))
                .when(failingLogger)
                .warn("plain-message", original);

        PrintStream previousErr = System.err;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        try (PrintStream replacement = new PrintStream(capture, true, StandardCharsets.ISO_8859_1.name())) {
            System.setErr(replacement);
            Slf4jExceptionHandler.WARN.on(failingLogger, "plain-message", original);
        } finally {
            System.setErr(previousErr);
        }

        String stderr = capture.toString(StandardCharsets.ISO_8859_1.name());
        assertTrue(stderr.contains("Failed to write to logger"));
        assertTrue(stderr.contains("plain-message"));
        assertFalse(stderr.contains("\u001B"), "ANSI escape codes must never be emitted");
    }
}
