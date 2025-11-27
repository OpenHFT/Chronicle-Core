/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Centralises interaction with {@code sun.nio.ch.DirectBuffer} to reduce compiler warnings.
 * <p>
 * Used to query direct buffer addresses and invoke the cleaner when available.
 */
public final class DirectBufferUtil {

    // Suppresses default constructor, ensuring non-instantiability.
    private DirectBufferUtil() {
    }

    /**
     * Returns the class of sun.nio.ch.DirectBuffer.
     *
     * @return the class of sun.nio.ch.DirectBuffer
     */
    public static Class<?> directBufferClass() {
        return sun.nio.ch.DirectBuffer.class;
    }

    /**
     * Cleans the provided {@code buffer} if and only if it is an
     * instance of sun.nio.ch.DirectBuffer
     *
     * @param buffer to clean
     * @throws NullPointerException if the provided {@code buffer } is {@code null}
     */
    @Deprecated(/* to be removed in 2027, only used in tests */)
    public static void cleanIfInstanceOfDirectBuffer(final ByteBuffer buffer) {
        requireNonNull(buffer);
        if (buffer instanceof DirectBuffer) {
            ((DirectBuffer) buffer).cleaner().clean();
        }
    }

    /**
     * Returns the address of the provided {@code buffer} if and only if it is an
     * instance of sun.nio.ch.DirectBuffer, otherwise throws an exception.
     *
     * @param buffer to clean
     * @throws NullPointerException if the provided {@code buffer } is {@code null}
     * @throws ClassCastException   if the provided {@code buffer } is not an instance of sun.nio.ch.DirectBuffer
     */
    public static long addressOrThrow(final ByteBuffer buffer) {
        requireNonNull(buffer);
        if (!(buffer instanceof DirectBuffer)) {
            throw new ClassCastException("Buffer is not a DirectBuffer: " + buffer.getClass().getName());
        }
        try {
            return ((DirectBuffer) buffer).address();
        } catch (IllegalAccessError e) {
            throw new ClassCastException(e.toString());
        }
    }
}
