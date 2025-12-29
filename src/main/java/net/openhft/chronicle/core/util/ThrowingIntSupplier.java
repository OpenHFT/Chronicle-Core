/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * <p>
 * Represents a supplier of results which might throw an Exception
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #getAsInt()}.
 *
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingIntSupplier<T extends Throwable> {

    /**
     * Gets an int result, possibly throwing a checked exception.
     *
     * @return a result
     * @throws T if supplying fails
     */
    int getAsInt() throws T;
}
