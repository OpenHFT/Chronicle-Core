/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class IORuntimeExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Error message";
        IORuntimeException exception = new IORuntimeException(message);

        assertEquals(message, exception.getMessage(), "testConstructorWithMessage: L17");
    }

    @Test
    void testConstructorWithThrowable() {
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(cause);

        assertEquals(cause, exception.getCause(), "testConstructorWithThrowable: L25");
    }

    @Test
    void testConstructorWithMessageAndThrowable() {
        String message = "Error message";
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(message, cause);

        assertEquals(message, exception.getMessage(), "testConstructorWithMessageAndThrowable: L34");
        assertEquals(cause, exception.getCause(), "testConstructorWithMessageAndThrowable: L35");
    }

    @Test
    void testNewIORuntimeException() {
        Exception closedException = new IOException("Connection reset by peer");
        Exception otherException = new IOException("Some other IO error");

        IORuntimeException runtimeClosedException = IORuntimeException.newIORuntimeException(closedException);
        IORuntimeException runtimeOtherException = IORuntimeException.newIORuntimeException(otherException);

        assertInstanceOf(ClosedIORuntimeException.class, runtimeClosedException);
        assertEquals(closedException, runtimeClosedException.getCause(), "testNewIORuntimeException: L47");

        assertFalse(runtimeOtherException instanceof ClosedIORuntimeException, "testNewIORuntimeException: L49");
        assertEquals(otherException, runtimeOtherException.getCause(), "testNewIORuntimeException: L50");
    }
}
