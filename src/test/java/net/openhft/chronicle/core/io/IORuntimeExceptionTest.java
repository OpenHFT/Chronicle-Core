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

        assertEquals(message, exception.getMessage(),
                "message constructor should preserve message: " + exception.getMessage());
    }

    @Test
    void testConstructorWithThrowable() {
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(cause);

        assertEquals(cause, exception.getCause(),
                "cause constructor should preserve cause: " + exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndThrowable() {
        String message = "Error message";
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(message, cause);

        assertEquals(message, exception.getMessage(),
                "message and cause constructor should preserve message: " + exception.getMessage());
        assertEquals(cause, exception.getCause(),
                "message and cause constructor should preserve cause: " + exception.getCause());
    }

    @Test
    void testNewIORuntimeException() {
        Exception closedException = new IOException("Connection reset by peer");
        Exception otherException = new IOException("Some other IO error");

        IORuntimeException runtimeClosedException = IORuntimeException.newIORuntimeException(closedException);
        IORuntimeException runtimeOtherException = IORuntimeException.newIORuntimeException(otherException);

        assertInstanceOf(ClosedIORuntimeException.class, runtimeClosedException,
                "closed exception should map to ClosedIORuntimeException");
        assertEquals(closedException, runtimeClosedException.getCause(),
                "closed IO exception should be preserved as cause");

        assertFalse(runtimeOtherException instanceof ClosedIORuntimeException, "object should not be of specified type");
        assertEquals(otherException, runtimeOtherException.getCause(),
                "non-closed IO exception should be preserved as cause");
    }
}
