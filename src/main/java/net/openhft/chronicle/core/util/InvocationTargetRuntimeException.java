/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Runtime wrapper for exceptions thrown by reflective invocation targets.
 * <p>
 * Unwraps {@link InvocationTargetException} to surface the underlying cause, enabling callers to
 * rethrow unchecked without losing the original failure.
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
