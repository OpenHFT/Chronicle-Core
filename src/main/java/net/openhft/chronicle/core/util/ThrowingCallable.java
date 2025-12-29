/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

/**
 * {@link java.util.concurrent.Callable} variant whose {@link #call()} method can throw a checked exception.
 * <p>
 * Enables APIs to accept lambdas that propagate checked exceptions without wrapping them immediately.
 *
 * @param <R> result type
 * @param <T> checked exception type thrown
 */
@FunctionalInterface
public interface ThrowingCallable<R, T extends Throwable> {
    /**
     * Executes the callable action and returns the computed result value.
     *
     * @return result of the computation
     * @throws T if execution fails
     */
    @NotNull
    R call() throws T;
}
