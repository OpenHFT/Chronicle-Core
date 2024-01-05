package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterruptedRuntimeExceptionTest {

    @Test
    void defaultConstructorShouldCreateExceptionWithNoMessageOrCause() {
        InterruptedRuntimeException exception = new InterruptedRuntimeException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageShouldSetCorrectMessage() {
        String message = "Interrupted";
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndCauseShouldSetBothCorrectly() {
        String message = "Interrupted";
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructorWithCauseShouldSetCauseAndDeriveMessage() {
        Throwable cause = new RuntimeException("Cause");
        InterruptedRuntimeException exception = new InterruptedRuntimeException(cause);
        assertEquals(cause.toString(), exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
