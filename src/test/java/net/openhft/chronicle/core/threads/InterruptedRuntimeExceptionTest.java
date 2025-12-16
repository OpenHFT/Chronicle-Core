/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterruptedRuntimeExceptionTest {

    @Test
    void defaultConstructorShouldCreateExceptionWithNoMessageOrCause() {
        InterruptedRuntimeException exception = new InterruptedRuntimeException();
        assertNull(exception.getMessage(), "defaultConstructorShouldCreateExceptionWithNoMessageOrCause: L14");
        assertNull(exception.getCause(), "defaultConstructorShouldCreateExceptionWithNoMessageOrCause: L15");
    }

    @Test
    void constructorWithMessageShouldSetCorrectMessage() {
        String message = "Interrupted";
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message);
        assertEquals(message, exception.getMessage(), "constructorWithMessageShouldSetCorrectMessage: L22");
        assertNull(exception.getCause(), "constructorWithMessageShouldSetCorrectMessage: L23");
    }

    @Test
    void constructorWithMessageAndCauseShouldSetBothCorrectly() {
        String message = "Interrupted";
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message, cause);
        assertEquals(message, exception.getMessage(), "constructorWithMessageAndCauseShouldSetBothCorrectly: L31");
        assertEquals(cause, exception.getCause(), "constructorWithMessageAndCauseShouldSetBothCorrectly: L32");
    }

    @Test
    void constructorWithCauseShouldSetCauseAndDeriveMessage() {
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(cause);
        assertEquals(cause.toString(), exception.getMessage(), "constructorWithCauseShouldSetCauseAndDeriveMessage: L39");
        assertEquals(cause, exception.getCause(), "constructorWithCauseShouldSetCauseAndDeriveMessage: L40");
    }
}
