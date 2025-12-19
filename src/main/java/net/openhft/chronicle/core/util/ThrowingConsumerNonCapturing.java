/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

/**
 * Consumer that can throw a checked exception and avoids lambda capture.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 * @param <U> buffer/target type for the write
 */
public interface ThrowingConsumerNonCapturing<I, T extends Throwable, U> {

    /**
     * Performs this operation on the given argument.
     *
     * @param in the input argument
     * @param sb destination to append to
     * @param toBytes helper used to write bytes
     * @throws T if the consumer fails
     */
    void accept(I in, CharSequence sb, U toBytes) throws T;
}
