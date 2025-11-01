/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 */

public interface ThrowingConsumerNonCapturing<I, T extends Throwable, U> {

    /**
     * Performs this operation on the given argument.
     *
     * @param in the input argument
     */
    void accept(I in, CharSequence sb, U toBytes) throws T;
}
