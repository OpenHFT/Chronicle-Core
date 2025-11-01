/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link ThrowingFunction}.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <I> the type of the first argument to the function
 * @param <J> the type of the second argument to the function
 * @param <T> the type of Throwable thrown
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingBiFunction<I, J, R, T extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @return the function result
     * @throws T on an error.
     */
    @NotNull
    R apply(I in, J i2) throws T;
}
