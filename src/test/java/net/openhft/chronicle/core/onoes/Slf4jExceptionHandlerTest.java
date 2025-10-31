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
