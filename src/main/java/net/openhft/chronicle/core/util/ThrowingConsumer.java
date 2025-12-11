/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * <p>
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowingConsumer<I, T extends Throwable> {

    /**
     * Performs this operation on the given argument.
     *
     * @param in the input argument
     * @throws T                   if the operation fails
     * @throws IllegalStateException if called in an invalid state
     */
    void accept(I in) throws T, IllegalStateException;
}
