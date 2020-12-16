package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Thrown if the receiver of a method event throws an exception on invocation
 */
public class InvocationTargetRuntimeException extends RuntimeException {
    public InvocationTargetRuntimeException(@NotNull Throwable cause) {
        super(cause instanceof InvocationTargetException ? cause.getCause() : cause);
    }
}
