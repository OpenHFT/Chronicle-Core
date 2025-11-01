/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * A runtime exception used to wrap a {@link ClassNotFoundException}.
 * <p>
 * This exception is typically used to convert a checked exception (ClassNotFoundException)
 * into an unchecked exception, allowing it to propagate up the call stack without the need
 * for explicit exception handling.
 *
 * @see RuntimeException
 * @see ClassNotFoundException
 */
public class ClassNotFoundRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new runtime exception with the specified cause.
     *
     * @param cause The {@code ClassNotFoundException} to be wrapped by this runtime exception.
     */
    public ClassNotFoundRuntimeException(ClassNotFoundException cause) {
        super(cause);
    }

    /**
     * Returns the {@code ClassNotFoundException} that is the cause of this runtime exception.
     *
     * @return The {@code ClassNotFoundException} that is the cause of this runtime exception.
     */
    @Override
    public synchronized ClassNotFoundException getCause() {
        return (ClassNotFoundException) super.getCause();
    }
}
