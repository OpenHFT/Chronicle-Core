package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RecordingExceptionHandlerGptTest {
    @Test
    public void testRecordingExceptionHandler() {
        Logger logger = LoggerFactory.getLogger(RecordingExceptionHandlerGptTest.class);
        String testMessage = "Test message";
        Exception testException = new RuntimeException("Test exception");
        Map<ExceptionKey, Integer> exceptionKeyCountMap = new HashMap<>();

        RecordingExceptionHandler handler = new RecordingExceptionHandler(LogLevel.ERROR, exceptionKeyCountMap, true);
        handler.on(logger, testMessage, testException);

        ExceptionKey expectedKey = new ExceptionKey(LogLevel.ERROR, Logger.class, logger.getName() + ": " + testMessage, testException);
        assertTrue(exceptionKeyCountMap.containsKey(expectedKey));
        assertEquals(1, exceptionKeyCountMap.get(expectedKey));
    }

    @Test
    public void testRecordingExceptionHandlerWithoutThrowable() {
        Logger logger = LoggerFactory.getLogger(RecordingExceptionHandlerGptTest.class);
        String testMessage = "Test message without exception";
        Map<ExceptionKey, Integer> exceptionKeyCountMap = new HashMap<>();

        RecordingExceptionHandler handler = new RecordingExceptionHandler(LogLevel.ERROR, exceptionKeyCountMap, true);
        handler.on(logger, testMessage, null);

        ExceptionKey expectedKey = new ExceptionKey(LogLevel.ERROR, Logger.class, logger.getName() + ": " + testMessage, null);
        assertFalse(exceptionKeyCountMap.containsKey(expectedKey));
    }
}
