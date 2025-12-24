/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class InvalidEventHandlerExceptionTest extends CoreTestCommon {

    private InvalidEventHandlerException e;

    @Test
    void testStandardConstructors() {
        String message = "Error occurred";
        Throwable cause = new RuntimeException("Cause of error");

        InvalidEventHandlerException exceptionWithMessage = new InvalidEventHandlerException(message);
        assertEquals(message, exceptionWithMessage.getMessage(), "exception message should match constructor argument");

        InvalidEventHandlerException exceptionWithCause = new InvalidEventHandlerException(cause);
        assertSame(cause, exceptionWithCause.getCause(), "exception cause should match constructor argument");

        InvalidEventHandlerException defaultException = new InvalidEventHandlerException();
        assertNull(defaultException.getMessage(), "default constructor should create exception with null message");
    }

    @Test
    void testReusableInstance() {
        InvalidEventHandlerException reusableInstance = InvalidEventHandlerException.reusable();
        assertNotNull(reusableInstance, "reusable InvalidEventHandlerException singleton should be non-null");
        assertEquals(0, reusableInstance.getStackTrace().length, "reusable instance should have empty stack trace");

        // Test immutability
        Throwable newCause = new RuntimeException("New cause");
        assertSame(reusableInstance, reusableInstance.initCause(newCause), "initCause should return same instance for reusable exception");
        assertNull(reusableInstance.getCause(), "reusable instance should ignore initCause and remain without cause");

        // Attempt to set a new stack trace
        reusableInstance.setStackTrace(new StackTraceElement[]{});
        assertEquals(0, reusableInstance.getStackTrace().length, "reusable instance should ignore setStackTrace and remain empty");
    }

    @BeforeEach
    public void setup() {
        e = InvalidEventHandlerException.reusable();
    }

    @Test
    void stacktrace() {
        assertEquals(0, e.getStackTrace().length, "reusable exception should have empty stack trace initially");

        StackTraceElement[] newStackTrace = Stream.of(new StackTraceElement("A", "foo", "A.java", 42))
                .toArray(StackTraceElement[]::new);

        e.setStackTrace(newStackTrace);
        assertEquals(0, e.getStackTrace().length, "reusable exception should ignore setStackTrace calls");
    }

    @Test
    void printStackTrace() throws IOException {
        final StringBuilder sb = new StringBuilder();

        try (OutputStream os = new OutputStream() {
            @Override
            public void write(int b) {
                sb.append((char) b);
            }
        };
             PrintStream ps = new PrintStream(os, true, UTF_8.name())) {
            e.printStackTrace(ps);
        }
        final String stackTrace = sb.toString();
        assertTrue(stackTrace.contains("Reusable"), "stack trace output should indicate reusable exception: " + stackTrace);
        assertTrue(stackTrace.contains("no stack trace"), "stack trace output should indicate no stack trace available: " + stackTrace);
    }

    @Test
    void toStringTest() {
        String text = e.toString();
        assertTrue(text.contains("Reusable"), "toString output should indicate reusable exception: " + text);
        assertTrue(text.contains("no stack trace"), "toString output should indicate no stack trace available: " + text);
    }
}
