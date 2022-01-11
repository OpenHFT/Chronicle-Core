package net.openhft.chronicle.core.threads;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvalidEventHandlerExceptionTest {

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