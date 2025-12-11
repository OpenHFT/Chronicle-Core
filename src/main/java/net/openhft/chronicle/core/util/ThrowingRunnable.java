/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * {@link Runnable} variant whose {@link #run()} method can throw a checked exception.
 *
 * @param <T> checked exception type thrown
 */
@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {
    /**
     * Executes the runnable.
     *
     * @throws T if execution fails
     */
    void run() throws T;
}
