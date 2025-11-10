//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.io.IORuntimeException;

/**
 * Represents an operation that accepts two input arguments and returns no
 * result. Unlike most other functional interfaces, {@code ThrowingBiConsumer} is expected
 * to operate via side-effects.
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object)}.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowingBiConsumer<I, J, T extends Throwable> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param in the first input argument
     * @param i2 the second input argument
     */
    void accept(I in, J i2) throws T, IORuntimeException;
}
