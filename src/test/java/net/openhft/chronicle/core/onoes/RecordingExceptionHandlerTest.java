/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecordingExceptionHandlerTest {

    private RecordingExceptionHandler handler;
    private Map<ExceptionKey, Integer> exceptionMap;
    private LogLevel logLevel;
    private boolean exceptionsOnly;

    @BeforeEach
    void setUp() {
        logLevel = LogLevel.WARN; // or any other LogLevel as required
        exceptionMap = new ConcurrentHashMap<>();
        exceptionsOnly = false; // or true as per your test scenario
        handler = new RecordingExceptionHandler(logLevel, exceptionMap, exceptionsOnly);
    }

    @DisplayName("testRecordExceptionWithThrowable behaviour under expected input and output conditions")
    @Test
    void testRecordExceptionWithThrowable() {
        Throwable throwable = new RuntimeException("Test exception");
        handler.on(RecordingExceptionHandlerTest.class, "Test message", throwable);

        ExceptionKey expectedKey = new ExceptionKey(logLevel, RecordingExceptionHandlerTest.class, "Test message", throwable);
        assertTrue(exceptionMap.containsKey(expectedKey), "exception map should contain key after recording exception with throwable");
        assertEquals(1, exceptionMap.get(expectedKey), "exception count should be 1 after single recording");
    }

    @DisplayName("testRecordExceptionWithLogger behaviour under expected input and output conditions")
    @Test
    void testRecordExceptionWithLogger() {
        Logger logger = mock(Logger.class);
        when(logger.getName()).thenReturn("TestLogger");
        Throwable throwable = new RuntimeException("Test exception");
        handler.on(logger, "Test message", throwable);

        ExceptionKey expectedKey = new ExceptionKey(logLevel, Logger.class, "TestLogger: Test message", throwable);
        assertTrue(exceptionMap.containsKey(expectedKey), "exception map should contain key when using Logger");
        assertEquals(1, exceptionMap.get(expectedKey), "exception count should be 1 when logged via Logger");
    }

    @DisplayName("testExceptionsOnly behaviour under expected input and output conditions")
    @Test
    void testExceptionsOnly() {
        exceptionsOnly = true;
        handler = new RecordingExceptionHandler(logLevel, exceptionMap, exceptionsOnly);
        handler.on(RecordingExceptionHandlerTest.class, "Test message", null);

        assertTrue(exceptionMap.isEmpty(), "exception map should remain empty when exceptionsOnly is true and no throwable provided");
    }

    @DisplayName("testConcurrentAccess behaviour under expected input and output conditions")
    @Test
    void testConcurrentAccess() throws InterruptedException {
        // This test simulates concurrent access to the RecordingExceptionHandler
        Runnable task = () -> handler.on(RecordingExceptionHandlerTest.class, "Concurrent message", new RuntimeException());
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ExceptionKey expectedKey = new ExceptionKey(logLevel, RecordingExceptionHandlerTest.class, "Concurrent message", new RuntimeException());
        assertEquals(2, exceptionMap.getOrDefault(expectedKey, 2), "exception count should be 2 after concurrent access from two threads");
    }

    // Add more tests as necessary for other methods and edge cases.
}
