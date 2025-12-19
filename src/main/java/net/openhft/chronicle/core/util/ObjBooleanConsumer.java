/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * Represents an operation that accepts an object-valued and {@code Boolean}-valued argument, and returns no result.  This is the
 * {@code (reference, long)} specialization of {@link java.util.function.BiConsumer} for {@code Boolean}.  Unlike most other functional
 * interfaces, {@code ObjBooleanConsumer} is expected to operate via side effects.
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, Boolean)}.
 *
 * @param <T> argument type of the reference parameter
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface ObjBooleanConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t     the first input argument
     * @param value the second input argument
     */
    void accept(T t, Boolean value);
}
