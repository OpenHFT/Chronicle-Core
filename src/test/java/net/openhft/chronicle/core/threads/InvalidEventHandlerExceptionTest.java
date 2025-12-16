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

@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate") // JUnit4 annotations require public class
public class InvalidEventHandlerExceptionTest extends CoreTestCommon {

    private InvalidEventHandlerException e;

    @Test
    public void testStandardConstructors() {
        String message = "Error occurred";
        Throwable cause = new RuntimeException("Cause of error");

        InvalidEventHandlerException exceptionWithMessage = new InvalidEventHandlerException(message);
        assertEquals(message, exceptionWithMessage.getMessage(), "testStandardConstructors: L31");

        InvalidEventHandlerException exceptionWithCause = new InvalidEventHandlerException(cause);
        assertSame(cause, exceptionWithCause.getCause(), "testStandardConstructors: L34");

        InvalidEventHandlerException defaultException = new InvalidEventHandlerException();
        assertNull(defaultException.getMessage(), "testStandardConstructors: L37");
    }

    @Test
    public void testReusableInstance() {
        InvalidEventHandlerException reusableInstance = InvalidEventHandlerException.reusable();
        assertNotNull(reusableInstance, "testReusableInstance: L43");
        assertEquals(0, reusableInstance.getStackTrace().length, "testReusableInstance: L44");

        // Test immutability
        Throwable newCause = new RuntimeException("New cause");
        assertSame(reusableInstance, reusableInstance.initCause(newCause), "testReusableInstance: L48");
        assertNull(reusableInstance.getCause(), "testReusableInstance: L49");

        // Attempt to set a new stack trace
        reusableInstance.setStackTrace(new StackTraceElement[]{});
        assertEquals(0, reusableInstance.getStackTrace().length, "testReusableInstance: L53");
    }

    @BeforeEach
    public void setup() {
        e = InvalidEventHandlerException.reusable();
    }

    @Test
    public void stacktrace() {
        assertEquals(0, e.getStackTrace().length, "stacktrace: L63");

        StackTraceElement[] newStackTrace = Stream.of(new StackTraceElement("A", "foo", "A.java", 42))
                .toArray(StackTraceElement[]::new);

        e.setStackTrace(newStackTrace);
        assertEquals(0, e.getStackTrace().length, "stacktrace: L69");
    }

    @Test
    public void printStackTrace() throws IOException {
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
        assertTrue(stackTrace.contains("Reusable"), "printStackTrace: L86");
        assertTrue(stackTrace.contains("no stack trace"), "printStackTrace: L87");
    }

    @Test
    public void toStringTest() {
        assertTrue(e.toString().contains("Reusable"), "toStringTest: L92");
        assertTrue(e.toString().contains("no stack trace"), "toStringTest: L93");
    }
}
