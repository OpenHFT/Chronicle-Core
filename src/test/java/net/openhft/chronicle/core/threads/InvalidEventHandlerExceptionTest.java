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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the behavior of the {@link InvalidEventHandlerException} class.
 */
public class InvalidEventHandlerExceptionTest extends CoreTestCommon {

    private InvalidEventHandlerException e;

    /**
     * Initializes an instance of {@link InvalidEventHandlerException} to be reused across tests.
     */
    @Before
    public void setup() {
        e = InvalidEventHandlerException.reusable();
    }

    /**
     * Tests if the stack trace of the reusable InvalidEventHandlerException is empty
     * and cannot be modified.
     */
    @Test
    public void shouldHaveEmptyAndUnmodifiableStackTrace() {
        assertEquals("Expected an empty stack trace.", 0, e.getStackTrace().length);

        StackTraceElement[] newStackTrace = {
                new StackTraceElement("A", "foo", "A.java", 42)
        };

        e.setStackTrace(newStackTrace);
        assertEquals("StackTrace should remain empty after an attempt to set it.", 0, e.getStackTrace().length);
    }

    /**
     * Tests if printStackTrace prints the expected content indicating that the exception is reusable
     * and contains no stack trace.
     */
    @Test
    public void shouldIndicateReusableAndNoStackTraceOnPrint() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(os)) {
            e.printStackTrace(ps);
            final String stackTrace = os.toString();
            assertTrue("Expected 'Reusable' in printed stack trace.", stackTrace.contains("Reusable"));
            assertTrue("Expected 'no stack trace' in printed stack trace.", stackTrace.contains("no stack trace"));
        }
    }

    /**
     * Tests if the toString method of the reusable InvalidEventHandlerException
     * includes indications that it is reusable and has no stack trace.
     */
    @Test
    public void shouldIndicateReusableAndNoStackTraceInToString() {
        String toStringResult = e.toString();
        assertTrue("Expected 'Reusable' in toString result.", toStringResult.contains("Reusable"));
        assertTrue("Expected 'no stack trace' in toString result.", toStringResult.contains("no stack trace"));
    }
}
