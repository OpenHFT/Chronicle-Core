package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.io.IORuntimeException;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class IORuntimeExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "Error message";
        IORuntimeException exception = new IORuntimeException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() {
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() {
        String message = "Error message";
        Throwable cause = new IOException("Cause");
        IORuntimeException exception = new IORuntimeException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testNewIORuntimeException() {
        Exception closedException = new IOException("Connection reset by peer");
        Exception otherException = new IOException("Some other IO error");

        IORuntimeException runtimeClosedException = IORuntimeException.newIORuntimeException(closedException);
        IORuntimeException runtimeOtherException = IORuntimeException.newIORuntimeException(otherException);

        assertTrue(runtimeClosedException instanceof ClosedIORuntimeException);
        assertEquals(closedException, runtimeClosedException.getCause());

        assertFalse(runtimeOtherException instanceof ClosedIORuntimeException);
        assertEquals(otherException, runtimeOtherException.getCause());
    }
}
