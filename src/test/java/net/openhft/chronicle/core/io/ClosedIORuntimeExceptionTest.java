package net.openhft.chronicle.core.io;

import static org.junit.Assert.*;
import org.junit.Test;

public class ClosedIORuntimeExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage);

        assertEquals("The message should match the one provided to the constructor",
                testMessage, exception.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String testMessage = "Test message";
        Throwable testCause = new Throwable("Test cause");
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, testCause);

        assertEquals("The message should match the one provided to the constructor",
                testMessage, exception.getMessage());
        assertEquals("The cause should match the one provided to the constructor",
                testCause, exception.getCause());
    }

    @Test
    public void testConstructorWithNullCause() {
        String testMessage = "Test message";
        ClosedIORuntimeException exception = new ClosedIORuntimeException(testMessage, null);

        assertEquals("The message should match the one provided to the constructor",
                testMessage, exception.getMessage());
        assertNull("The cause should be null", exception.getCause());
    }
}
