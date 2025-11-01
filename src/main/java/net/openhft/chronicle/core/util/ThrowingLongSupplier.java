/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */

package net.openhft.chronicle.core.util;

/**
 * <p>
 * Represents a supplier of results which might throw an Exception
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * This is a <a href="package-summary.html">functional longerface</a>
 * whose functional method is {@link #getAsLong()}.
 *
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingLongSupplier<T extends Throwable> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    long getAsLong() throws T;
}
