/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class InterruptedRuntimeExceptionTest {

    @DisplayName("defaultConstructorShouldCreateExceptionWithNoMessageOrCause behaviour under expected input and output conditions")
    @Test
    void defaultConstructorShouldCreateExceptionWithNoMessageOrCause() {
        InterruptedRuntimeException exception = new InterruptedRuntimeException();
        assertNull(exception.getMessage(), "exception created with default constructor should have null message");
        assertNull(exception.getCause(), "exception created with default constructor should have null cause");
    }

    @DisplayName("constructorWithMessageShouldSetCorrectMessage behaviour under expected input and output conditions")
    @Test
    void constructorWithMessageShouldSetCorrectMessage() {
        String message = "Interrupted";
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message);
        assertEquals(message, exception.getMessage(), "exception created with message should preserve the provided message");
        assertNull(exception.getCause(), "exception created with message only should have null cause");
    }

    @DisplayName("constructorWithMessageAndCauseShouldSetBothCorrectly behaviour under expected input and output conditions")
    @Test
    void constructorWithMessageAndCauseShouldSetBothCorrectly() {
        String message = "Interrupted";
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message, cause);
        assertEquals(message, exception.getMessage(), "exception created with message and cause should preserve the message");
        assertEquals(cause, exception.getCause(), "exception created with message and cause should preserve the cause");
    }

    @DisplayName("constructorWithCauseShouldSetCauseAndDeriveMessage behaviour under expected input and output conditions")
    @Test
    void constructorWithCauseShouldSetCauseAndDeriveMessage() {
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(cause);
        assertEquals(cause.toString(), exception.getMessage(), "exception created with cause only should derive message from cause toString");
        assertEquals(cause, exception.getCause(), "exception created with cause only should preserve the cause");
    }
}
