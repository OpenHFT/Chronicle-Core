/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ClosedIORuntimeExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage);

        assertEquals(testMessage, exception.getMessage(),
                "The message should match the one provided to the constructor");
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String testMessage = "Test message";
        Throwable testCause = new Throwable("Test cause");
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, testCause);

        assertEquals(testMessage, exception.getMessage(),
                "The message should match the one provided to the constructor");
        assertEquals(testCause, exception.getCause(),
                "The cause should match the one provided to the constructor");
    }

    @Test
    public void testConstructorWithNullCause() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, null);

        assertEquals(testMessage, exception.getMessage(),
                "The message should match the one provided to the constructor");
        assertNull(exception.getCause(), "The cause should be null");
    }
}
