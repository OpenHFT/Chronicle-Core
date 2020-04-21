package net.openhft.chronicle.core.threads;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

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
    public void printStackTrace() {
        e.printStackTrace();
    }

    @Test
    public void toStringTest() {
        assertTrue(e.toString().contains("no stack trace"));
    }

}