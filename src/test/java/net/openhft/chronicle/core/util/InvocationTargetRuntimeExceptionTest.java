/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class InvocationTargetRuntimeExceptionTest {

    @Test
    void testConstructorWithInvocationTargetException() {
        Exception targetException = new Exception("Target exception");
        InvocationTargetException invocationCause = new InvocationTargetException(targetException);

        InvocationTargetRuntimeException exception = new InvocationTargetRuntimeException(invocationCause);

        assertEquals(targetException, exception.getCause(),
                "The cause should be the target exception of the InvocationTargetException");
    }

    @Test
    void testConstructorWithNonInvocationTargetException() {
        Exception nonInvocationCause = new Exception("Non-invocation exception");

        InvocationTargetRuntimeException exception = new InvocationTargetRuntimeException(nonInvocationCause);

        assertEquals(nonInvocationCause, exception.getCause(),
                "The cause should be the non-invocation exception provided to the constructor");
    }
}
