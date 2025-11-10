//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * Represents a function that accepts a single {@code char} value argument and produces a
 * {@code boolean} result. This is the {@code char}-consuming primitive type specialization of
 * {@code Function}.
 *
 * <p>This is a functional interface whose functional method is {@link #applyAsChar(char)}.
 *
 * @see java.util.function.Function
 */
@FunctionalInterface
public interface CharToBooleanFunction {

    /**
     * Applies this function to the given {@code char} value.
     *
     * @param value the {@code char} value to be processed by the function.
     * @return the {@code boolean} result of the function.
     */
    boolean applyAsChar(char value);
}
