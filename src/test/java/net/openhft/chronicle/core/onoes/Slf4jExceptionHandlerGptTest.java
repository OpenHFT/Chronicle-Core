package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class Slf4jExceptionHandlerGptTest {
    @Test
    public void testSlf4jExceptionHandler() {
        Logger logger = LoggerFactory.getLogger(Slf4jExceptionHandlerGptTest.class);
        String testMessage = "Test message";
        Exception testException = new RuntimeException("Test exception");

        // It's hard to assert on the state of the logger after the method call. 
        // The following will simply confirm no exceptions are thrown when calling the `on` method.
        for (Slf4jExceptionHandler handler : Slf4jExceptionHandler.values()) {
            assertDoesNotThrow(() -> handler.on(logger, testMessage, testException));
        }
    }

    @Test
    public void testSlf4jExceptionHandlerLogLevelMapping() {
        // Check that each LogLevel maps to the correct Slf4jExceptionHandler.
        assertEquals(Slf4jExceptionHandler.ERROR, Slf4jExceptionHandler.valueOf(LogLevel.ERROR));
        assertEquals(Slf4jExceptionHandler.WARN, Slf4jExceptionHandler.valueOf(LogLevel.WARN));
        assertEquals(Slf4jExceptionHandler.PERF, Slf4jExceptionHandler.valueOf(LogLevel.PERF));
        assertEquals(Slf4jExceptionHandler.DEBUG, Slf4jExceptionHandler.valueOf(LogLevel.DEBUG));
    }
}
