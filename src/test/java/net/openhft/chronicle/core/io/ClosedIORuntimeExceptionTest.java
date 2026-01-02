/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClosedIORuntimeExceptionTest {

    @Test
    @DisplayName("Constructor with message closed io runtime")
    void testConstructorWithMessage() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage);

        assertEquals(testMessage, exception.getMessage(),
                "message-only constructor should set the exception message correctly");
    }

    @Test
    @DisplayName("Constructor with message and cause closed")
    void testConstructorWithMessageAndCause() {
        String testMessage = "Test message";
        Throwable testCause = new Throwable("Test cause");
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, testCause);

        assertEquals(testMessage, exception.getMessage(),
                "message-and-cause constructor should set the exception message correctly");
        assertEquals(testCause, exception.getCause(),
                "message-and-cause constructor should set the cause correctly");
    }

    @Test
    @DisplayName("Constructor with null cause closed io")
    void testConstructorWithNullCause() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, null);

        assertEquals(testMessage, exception.getMessage(),
                "constructor with null cause should still set the exception message correctly");
        assertNull(exception.getCause(), "constructor with null cause should leave cause unset");
    }
}
