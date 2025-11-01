/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * A custom exception indicating an illegal access or operation due to threading constraints.
 * <p>
 * This exception is typically thrown when a component, which is not designed to be thread-safe,
 * is accessed or modified by multiple threads concurrently. It provides additional context
 * by encapsulating the offending sequence of operations, usually through a stack trace.
 */
public class ThreadingIllegalStateException extends IllegalStateException {
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new {@code ThreadingIllegalStateException} with the specified detail
     * message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link Throwable#getMessage()} method).
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method). A {@code null} value is
     *                permitted and indicates that the cause is nonexistent or unknown.
     */
    public ThreadingIllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
