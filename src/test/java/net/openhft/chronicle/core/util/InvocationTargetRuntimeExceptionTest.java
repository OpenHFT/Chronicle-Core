package net.openhft.chronicle.core.util;

import org.junit.Ignore;
import org.junit.Test;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class InvocationTargetRuntimeExceptionTest {

    @Test
    public void testConstructorWithInvocationTargetException() {
        Exception targetException = new Exception("Target exception");
        InvocationTargetException invocationCause = new InvocationTargetException(targetException);

        InvocationTargetRuntimeException exception = new InvocationTargetRuntimeException(invocationCause);

        assertEquals("The cause should be the target exception of the InvocationTargetException",
                targetException, exception.getCause());
    }

    @Test
    public void testConstructorWithNonInvocationTargetException() {
        Exception nonInvocationCause = new Exception("Non-invocation exception");

        InvocationTargetRuntimeException exception = new InvocationTargetRuntimeException(nonInvocationCause);

        assertEquals("The cause should be the non-invocation exception provided to the constructor",
                nonInvocationCause, exception.getCause());
    }

    @Test
    @Ignore
    public void testConstructorWithNullCause() {
        InvocationTargetRuntimeException exception = new InvocationTargetRuntimeException(null);

        assertNull("The cause should be null", exception.getCause());
    }
}
