/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

/**
 * Three argument function that is permitted to throw a checked exception.
 * <p>
 * Useful when adapting APIs that expect pure functions but where implementations need to
 * propagate failures without eagerly wrapping them.
 *
 * @param <I> first argument type
 * @param <J> second argument type
 * @param <A> third argument type
 * @param <R> result type
 * @param <T> checked exception type thrown
 */
@FunctionalInterface
public interface ThrowingTriFunction<I, J, A, R, T extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @param i3 the third function argument
     * @return the function result
     * @throws T on an error.
     */
    @NotNull
    R apply(I in, J i2, A i3) throws T;
}
