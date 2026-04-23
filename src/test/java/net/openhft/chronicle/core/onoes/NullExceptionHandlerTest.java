/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.test.RecordingLogger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullExceptionHandlerTest {

    @Test
    void onMethodShouldDoNothing() {
        RecordingLogger logger = new RecordingLogger("NullExceptionHandlerTest");
        Throwable mockThrowable = new RuntimeException("Test exception");

        assertDoesNotThrow(() -> NullExceptionHandler.NOTHING.on(logger.logger(), "Test message", mockThrowable));

        // Since the method should do nothing, there should be no interactions with the logger
        assertFalse(logger.hasCalls());
    }

    @Test
    void isEnabledShouldAlwaysReturnFalse() {
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(String.class));
        assertFalse(NullExceptionHandler.NOTHING.isEnabled(Integer.class));
    }
}
