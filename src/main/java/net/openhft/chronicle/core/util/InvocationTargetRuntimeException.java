//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * The {@code InvocationTargetRuntimeException} class is a custom runtime exception
 * that is thrown when a method's invocation on the target object throws an exception.
 * This class helps in wrapping the exception thrown by the target method
 * into an unchecked exception.
 * <p>
 * For example, if a reflective method invocation through {@code java.lang.reflect.Method}
 * throws an exception, this class can be used to wrap and rethrow it as a
 * runtime exception.
 * <p>
 * This is useful in scenarios where the client code invoking the method
 * is not expecting or is not capable of handling checked exceptions thrown
 * by the method.
 *
 * @see InvocationTargetException
 */
public class InvocationTargetRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new {@code InvocationTargetRuntimeException} with the
     * specified cause. If the cause is an instance of
     * {@link InvocationTargetException}, the actual exception thrown by
     * the target method is extracted and set as the cause for this exception.
     * Otherwise, the supplied {@code cause} is set as the cause.
     *
     * @param cause The cause of this exception. This is usually the exception
     *              thrown by the method that was invoked.
     */
    public InvocationTargetRuntimeException(@NotNull Throwable cause) {
        super(cause instanceof InvocationTargetException ? cause.getCause() : cause);
    }
}
