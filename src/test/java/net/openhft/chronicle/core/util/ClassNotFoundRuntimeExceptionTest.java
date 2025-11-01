/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassNotFoundRuntimeExceptionTest {

    @Test
    public void testConstructor() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        assertNotNull(exception);
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testGetCause() {
        ClassNotFoundException cause = new ClassNotFoundException("Test class not found");
        ClassNotFoundRuntimeException exception = new ClassNotFoundRuntimeException(cause);

        Throwable throwableCause = exception.getCause();

        assertTrue(throwableCause instanceof ClassNotFoundException);
        assertEquals(cause, throwableCause);
    }
}
