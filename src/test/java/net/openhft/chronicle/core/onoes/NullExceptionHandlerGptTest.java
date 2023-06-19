package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NullExceptionHandlerGptTest extends CoreTestCommon {

    @Test
    public void testNullExceptionHandler() {
        Logger logger = LoggerFactory.getLogger(NullExceptionHandlerTest.class);
        String testMessage = "Test message";
        Exception testException = new RuntimeException("Test exception");

        // Test if NullExceptionHandler is indeed doing nothing (no exceptions should be thrown)
        assertDoesNotThrow(() -> NullExceptionHandler.NOTHING.on(logger, testMessage, testException));
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(NullExceptionHandlerTest.class));
    }
}
