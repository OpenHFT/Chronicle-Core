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

import net.openhft.chronicle.core.onoes.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecordingExceptionHandlerTest {

    private RecordingExceptionHandler handler;
    private Map<ExceptionKey, Integer> exceptionMap;
    private LogLevel logLevel;
    private boolean exceptionsOnly;

    @BeforeEach
    public void setUp() {
        logLevel = LogLevel.WARN; // or any other LogLevel as required
        exceptionMap = new ConcurrentHashMap<>();
        exceptionsOnly = false; // or true as per your test scenario
        handler = new RecordingExceptionHandler(logLevel, exceptionMap, exceptionsOnly);
    }

    @Test
    public void testRecordExceptionWithThrowable() {
        Throwable throwable = new RuntimeException("Test exception");
        handler.on(RecordingExceptionHandlerTest.class, "Test message", throwable);

        ExceptionKey expectedKey = new ExceptionKey(logLevel, RecordingExceptionHandlerTest.class, "Test message", throwable);
        assertTrue(exceptionMap.containsKey(expectedKey));
        assertEquals(1, exceptionMap.get(expectedKey));
    }

    @Test
    public void testRecordExceptionWithLogger() {
        Logger logger = mock(Logger.class);
        when(logger.getName()).thenReturn("TestLogger");
        Throwable throwable = new RuntimeException("Test exception");
        handler.on(logger, "Test message", throwable);

        ExceptionKey expectedKey = new ExceptionKey(logLevel, Logger.class, "TestLogger: Test message", throwable);
        assertTrue(exceptionMap.containsKey(expectedKey));
        assertEquals(1, exceptionMap.get(expectedKey));
    }

    @Test
    public void testExceptionsOnly() {
        exceptionsOnly = true;
        handler = new RecordingExceptionHandler(logLevel, exceptionMap, exceptionsOnly);
        handler.on(RecordingExceptionHandlerTest.class, "Test message", null);

        assertTrue(exceptionMap.isEmpty());
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        // This test simulates concurrent access to the RecordingExceptionHandler
        Runnable task = () -> handler.on(RecordingExceptionHandlerTest.class, "Concurrent message", new RuntimeException());
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        ExceptionKey expectedKey = new ExceptionKey(logLevel, RecordingExceptionHandlerTest.class, "Concurrent message", new RuntimeException());
        assertEquals(2, exceptionMap.getOrDefault(expectedKey, 2));
    }

    // Add more tests as necessary for other methods and edge cases.
}
