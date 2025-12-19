/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreadingIllegalStateExceptionTest {

    @Test
    void testConstructorWithMessageAndCause() {
        String expectedMessage = "Custom threading error message";
        Throwable expectedCause = new RuntimeException("Cause of error");

        ThreadingIllegalStateException exception = new ThreadingIllegalStateException(expectedMessage, expectedCause);

        assertEquals(expectedMessage, exception.getMessage(), "exception should preserve the provided message");
        assertEquals(expectedCause, exception.getCause(), "exception should preserve the provided cause");
    }
}
