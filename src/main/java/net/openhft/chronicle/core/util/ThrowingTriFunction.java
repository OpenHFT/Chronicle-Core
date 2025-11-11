/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingTriFunction<I, J, A, R, T extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @return the function result
     * @throws T on an error.
     */
    @NotNull
    R apply(I in, J i2, A i3) throws T;
}
