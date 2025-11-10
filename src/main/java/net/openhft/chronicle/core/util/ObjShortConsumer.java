//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts a an object-valued and {@code short}-valued argument, and returns no result.  This is the
 * {@code (reference, long)} specialization of {@link java.util.function.BiConsumer} for {@code short}.  Unlike most other functional
 * interfaces, {@code ObjShortConsumer} is expected to operate via side-effects.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, short)}.
 *
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ObjShortConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t     the first input argument
     * @param value the second input argument
     */
    void accept(T t, short value);
}
