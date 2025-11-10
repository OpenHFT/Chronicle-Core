//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts an object-valued and {@code char}-valued argument, and returns no result.  This is the
 * {@code (reference, long)} specialization of {@link java.util.function.BiConsumer} for {@code char}.  Unlike most other functional
 * interfaces, {@code ObjCharConsumer} is expected to operate via side effects.
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, char)}.
 *
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ObjCharConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t     the first input argument
     * @param value the second input argument
     */
    void accept(T t, char value);
}
