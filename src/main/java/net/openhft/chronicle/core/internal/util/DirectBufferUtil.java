/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.internal.util;

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * This utility class provides centralized access to the
 * internal class sun.nio.ch.DirectBuffer in order to reduce
 * compile time warnings.
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
     * @throws NullPointerException If the provided {@code buffer } is {@code null}
     */
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
     * @throws NullPointerException If the provided {@code buffer } is {@code null}
     * @throws ClassCastException   If the provided {@code buffer } is not an instance of sun.nio.ch.DirectBuffer
     */
    public static long addressOrThrow(final ByteBuffer buffer) {
        requireNonNull(buffer);
        return ((DirectBuffer) buffer).address();
    }

}
