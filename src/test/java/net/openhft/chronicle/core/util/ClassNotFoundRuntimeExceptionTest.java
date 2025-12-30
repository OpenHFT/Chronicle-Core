/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassNotFoundRuntimeExceptionTest {

    @DisplayName("Constructor preserves ClassNotFoundException cause for runtime exception")
    @Test
    void testConstructor() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        assertNotNull(exception, "exception should be captured");
        assertEquals(cause, exception.getCause(), "exception should preserve the original ClassNotFoundException as its cause");
    }

    @DisplayName("getCause returns original ClassNotFoundException instance for runtime")
    @Test
    void testGetCause() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        Throwable throwableCause = exception.getCause();

        assertInstanceOf(ClassNotFoundException.class, throwableCause,
                "getCause should return a ClassNotFoundException instance");
        assertEquals(cause, throwableCause, "getCause should return the original ClassNotFoundException");
    }
}
