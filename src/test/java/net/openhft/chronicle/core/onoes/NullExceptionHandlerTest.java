/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class NullExceptionHandlerTest {

    @DisplayName("On method should do nothing exception handler")
    @Test
    void onMethodShouldDoNothing() {
        Logger mockLogger = mock(Logger.class);
        Throwable mockThrowable = new RuntimeException("Test exception");

        assertDoesNotThrow(() -> NullExceptionHandler.NOTHING.on(mockLogger, "Test message", mockThrowable),
                "NullExceptionHandler should ignore on call without throwing");

        // Since the method should do nothing, there should be no interactions with the logger
        verifyNoInteractions(mockLogger);
    }

    @DisplayName("Is enabled should always return false exception handler")
    @Test
    void isEnabledShouldAlwaysReturnFalse() {
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(String.class), "isEnabled should return false for String class");
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(Integer.class), "isEnabled should return false for Integer class");
    }
}
