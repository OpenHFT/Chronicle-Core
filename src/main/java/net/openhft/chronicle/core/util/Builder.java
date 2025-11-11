/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A Builder of type T is a configurable object that can provide
 * another non-null T instance.
 * <p>
 * Extends {@link Supplier} to make this useable as a {@link Supplier}.
 *
 * @param <T> of object provided
 */
@FunctionalInterface
public interface Builder<T> extends Supplier<T> {

    /**
     * Builds and returns a non-null T instance.
     * <p>
     * The builder should always creates a new instance if the
     * instance is mutable. If the instance is immutable,
     * the builder may create a new instance, or it may return
     * a previously existing instance at its own discretion.
     * <p>
     * As opposed to a factory, a Builder is often only able to be invoked at most one time.
     *
     * @return a non-null instance of type T
     * @throws IllegalStateException if the builder is can only be invoked once
     *                               and this method is invoked more than once.
     */
    @NotNull
    T build();

    @Override
    default T get() {
        return build();
    }
}
