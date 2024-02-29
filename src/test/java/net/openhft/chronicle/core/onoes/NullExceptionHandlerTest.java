package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NullExceptionHandlerTest {

    @Test
    void onMethodShouldDoNothing() {
        Logger mockLogger = mock(Logger.class);
        Throwable mockThrowable = new RuntimeException("Test exception");

        assertDoesNotThrow(() -> NullExceptionHandler.NOTHING.on(mockLogger, "Test message", mockThrowable));

        // Since the method should do nothing, there should be no interactions with the logger
        verifyNoInteractions(mockLogger);
    }

    @Test
    void isEnabledShouldAlwaysReturnFalse() {
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(String.class));
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(Integer.class));
    }
}
