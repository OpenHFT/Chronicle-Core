package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadingIllegalStateExceptionTest {

    @Test
    public void testConstructorWithMessageAndCause() {
        String expectedMessage = "Custom threading error message";
        Throwable expectedCause = new RuntimeException("Cause of error");

        ThreadingIllegalStateException exception = new ThreadingIllegalStateException(expectedMessage, expectedCause);

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(expectedCause, exception.getCause());
    }
}
