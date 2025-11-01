/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts a single {@code Boolean}-valued argument and returns no result.  This is the
 * primitive type specialization of {@link java.util.function.Consumer} for {@code Boolean}.  Unlike most other functional
 * interfaces, {@code BooleanConsumer} is expected to operate via side-effects.
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Boolean)}.
 *
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface BooleanConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(Boolean value);

}
