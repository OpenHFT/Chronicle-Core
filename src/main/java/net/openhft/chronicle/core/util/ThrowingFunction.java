/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * <p>
 * Represents a function that accepts one argument and produces a result.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingFunction<I, R, T extends Throwable> {
    /**
     * Wraps a throwing function as a standard {@link Function}, converting checked exceptions to unchecked.
     *
     * @param function throwing function to wrap
     * @param <I>      input type
     * @param <R>      result type
     * @param <T>      checked exception type
     * @return function that rethrows failures unchecked
     */
    static <I, R, T extends Throwable> Function<I, R> asFunction(@NotNull ThrowingFunction<I, R, T> function) {
        return in -> {
            try {
                return function.apply(in);

            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Applies this function to the given argument.
     *
     * @param in the function argument
     * @return the function result
     * @throws T if the function fails
     */
    @NotNull
    R apply(I in) throws T;
}
