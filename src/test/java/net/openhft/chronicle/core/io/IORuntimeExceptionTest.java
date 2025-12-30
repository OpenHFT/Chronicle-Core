/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class IORuntimeExceptionTest {

    @DisplayName("Constructor with message io runtime exception")
    @Test
    void testConstructorWithMessage() {
        String message = "Error message";
        IORuntimeException exception = new IORuntimeException(message);

        assertEquals(message, exception.getMessage(),
                "exception message should match the provided message for string constructor");
    }

    @DisplayName("Constructor with throwable io runtime exception")
    @Test
    void testConstructorWithThrowable() {
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(cause);

        assertEquals(cause, exception.getCause(),
                "exception cause should match the provided throwable for cause constructor");
    }

    @DisplayName("Constructor with message and throwable io")
    @Test
    void testConstructorWithMessageAndThrowable() {
        String message = "Error message";
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(message, cause);

        assertEquals(message, exception.getMessage(),
                "exception message should match the provided message for message-and-cause constructor");
        assertEquals(cause, exception.getCause(),
                "exception cause should match the provided throwable for message-and-cause constructor");
    }

    @DisplayName("newIORuntimeException classifies closed and other errors")
    @Test
    void testNewIORuntimeException() {
        Exception closedException = new IOException("Connection reset by peer");
        Exception otherException = new IOException("Some other IO error");

        IORuntimeException runtimeClosedException = IORuntimeException.newIORuntimeException(closedException);
        IORuntimeException runtimeOtherException = IORuntimeException.newIORuntimeException(otherException);

        assertInstanceOf(ClosedIORuntimeException.class, runtimeClosedException,
                "newIORuntimeException should classify connection reset as ClosedIORuntimeException");
        assertEquals(closedException, runtimeClosedException.getCause(), "ClosedIORuntimeException cause should match the original exception");

        assertFalse(runtimeOtherException instanceof ClosedIORuntimeException, "object should not be of specified type");
        assertEquals(otherException, runtimeOtherException.getCause(), "IORuntimeException cause should match the original exception");
    }
}
