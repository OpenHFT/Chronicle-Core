/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvalidEventHandlerExceptionTest extends CoreTestCommon {

    @Test
    public void testStandardConstructors() {
        String message = "Error occurred";
        Throwable cause = new RuntimeException("Cause of error");

        InvalidEventHandlerException exceptionWithMessage = new InvalidEventHandlerException(message);
        assertEquals(message, exceptionWithMessage.getMessage());

        InvalidEventHandlerException exceptionWithCause = new InvalidEventHandlerException(cause);
        assertSame(cause, exceptionWithCause.getCause());

        InvalidEventHandlerException defaultException = new InvalidEventHandlerException();
        assertNull(defaultException.getMessage());
    }

    @Test
    public void testReusableInstance() {
        InvalidEventHandlerException reusableInstance = InvalidEventHandlerException.reusable();
        assertNotNull(reusableInstance);
        assertEquals(0, reusableInstance.getStackTrace().length);

        // Test immutability
        Throwable newCause = new RuntimeException("New cause");
        assertSame(reusableInstance, reusableInstance.initCause(newCause));
        assertNull(reusableInstance.getCause());

        // Attempt to set a new stack trace
        reusableInstance.setStackTrace(new StackTraceElement[]{});
        assertEquals(0, reusableInstance.getStackTrace().length);
    }
    InvalidEventHandlerException e;

    @Before
    public void setup() {
        e = InvalidEventHandlerException.reusable();
    }

    @Test
    public void stacktrace() {
        assertEquals(0, e.getStackTrace().length);

        StackTraceElement[] newStackTrace = Stream.of(new StackTraceElement("A", "foo", "A.java", 42))
                .toArray(StackTraceElement[]::new);

        e.setStackTrace(newStackTrace);
        assertEquals(0, e.getStackTrace().length);
    }

    @Test
    public void printStackTrace() throws IOException {
        final StringBuilder sb = new StringBuilder();

        try (OutputStream os = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                sb.append((char) b);
            }
        };
             PrintStream ps = new PrintStream(os)) {
            e.printStackTrace(ps);
        }
        final String stackTrace = sb.toString();
        assertTrue(stackTrace.contains("Reusable"));
        assertTrue(stackTrace.contains("no stack trace"));
    }

    @Test
    public void toStringTest() {
        assertTrue(e.toString().contains("Reusable"));
        assertTrue(e.toString().contains("no stack trace"));
    }
}