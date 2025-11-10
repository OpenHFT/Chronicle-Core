//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * <p>
 * Represents a supplier of results which might throw an Exception
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <V> the type of results supplied by this supplier
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<V, T extends Throwable> {

    static <V, T extends Throwable> Supplier<V> asSupplier(@NotNull ThrowingSupplier<V, T> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();

            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Gets a result.
     *
     * @throws InvalidMarshallableException if the object created is not valid
     * @return a result
     */
    @NotNull
    V get() throws T, InvalidMarshallableException;
}
