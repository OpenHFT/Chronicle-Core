/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.annotation.Address;
import net.openhft.chronicle.core.annotation.NonNegative;

/**
 * Central assertion helpers for low-level memory access.
 *
 * <p>These helpers are intended for use with Chronicle's
 * {@code assert SKIP_ASSERTIONS || ...} idiom so hot-path checks still
 * compile out in the normal assertions-disabled build while debug builds
 * keep precise range validation and failure messages.</p>
 */
public final class MemoryAegis {

    private MemoryAegis() {
    }

    /**
     * Asserts that an on-heap byte-array slice is valid.
     *
     * @param bytes  the source array
     * @param offset the slice start
     * @param length the slice length
     * @return {@code true}
     * @throws AssertionError if the slice is invalid
     */
    public static boolean assertByteArrayRange(final byte[] bytes,
                                               final @NonNegative long offset,
                                               final @NonNegative long length) {
        if (bytes == null)
            throw new AssertionError("bytes must not be null");
        return assertSizedRange("bytes.length", bytes.length, offset, length);
    }

    /**
     * Asserts that an on-heap char-array slice is valid.
     *
     * @param chars  the source array
     * @param offset the slice start
     * @param length the slice length
     * @return {@code true}
     * @throws AssertionError if the slice is invalid
     */
    public static boolean assertCharArrayRange(final char[] chars,
                                               final @NonNegative long offset,
                                               final @NonNegative long length) {
        if (chars == null)
            throw new AssertionError("chars must not be null");
        return assertSizedRange("chars.length", chars.length, offset, length);
    }

    /**
     * Asserts that a string slice is valid.
     *
     * @param text   the source string
     * @param start  the slice start
     * @param length the slice length
     * @return {@code true}
     * @throws AssertionError if the slice is invalid
     */
    public static boolean assertStringRange(final String text,
                                            final @NonNegative long start,
                                            final @NonNegative long length) {
        if (text == null)
            throw new AssertionError("text must not be null");
        return assertSizedRange("text.length()", text.length(), start, length);
    }

    /**
     * Asserts that a native address range is non-zero, non-negative, and
     * does not wrap around.
     *
     * @param address the native start address
     * @param length  the number of bytes covered
     * @return {@code true}
     * @throws AssertionError if the range is invalid
     */
    public static boolean assertAddressRange(final @Address long address,
                                             final @NonNegative long length) {
        if (length < 0)
            throw new AssertionError("length must be >= 0: " + length);
        if (address < NativeAddressSpace.minAddress())
            throw new AssertionError("address must be >= " + NativeAddressSpace.minAddress()
                    + ": " + address);
        if (address >= NativeAddressSpace.maxAddressExclusive())
            throw new AssertionError("address must be <= " + NativeAddressSpace.maxAddressInclusive()
                    + ": " + address);
        if (length > NativeAddressSpace.maxAddressExclusive() - address)
            throw new AssertionError("native range exceeds configured address space: address=" + address
                    + ", length=" + length
                    + ", maxAddressExclusive=" + NativeAddressSpace.maxAddressExclusive());
        return true;
    }

    /**
     * Asserts that an object-relative byte range is sane for low-level
     * field or array-base access.
     *
     * @param object the target object
     * @param offset the starting offset
     * @param length the number of bytes covered
     * @return {@code true}
     * @throws AssertionError if the range is invalid
     */
    public static boolean assertObjectRange(final Object object,
                                            final @NonNegative long offset,
                                            final @NonNegative long length) {
        if (object == null)
            throw new AssertionError("object must not be null");
        if (offset <= 0)
            throw new AssertionError("offset must be > 0: " + offset);
        if (length < 0)
            throw new AssertionError("length must be >= 0: " + length);
        if (offset >= NativeAddressSpace.maxAddressExclusive())
            throw new AssertionError("offset must be <= " + NativeAddressSpace.maxAddressInclusive()
                    + ": " + offset);
        if (length > NativeAddressSpace.maxAddressExclusive() - offset)
            throw new AssertionError("object range exceeds configured address space: offset=" + offset);
        return true;
    }

    /**
     * Asserts that a single-register Unsafe range is valid.
     * When {@code object} is non-null the offset is treated as an
     * object-relative range; when {@code object} is null it is treated
     * as an absolute native address.
     *
     * @param object the target object, or {@code null} for absolute access
     * @param offset the starting offset or address
     * @param length the number of bytes covered
     * @return {@code true}
     * @throws AssertionError if the range is invalid
     */
    public static boolean assertObjectOrAddressRange(final Object object,
                                                     final @NonNegative long offset,
                                                     final @NonNegative long length) {
        if (object == null) {
            return assertAddressRange(offset, length);
        }
        return assertObjectRange(object, offset, length);
    }

    private static boolean assertSizedRange(final String sizeName,
                                            final long size,
                                            final long offset,
                                            final long length) {
        if (offset < 0)
            throw new AssertionError("offset must be >= 0: " + offset);
        if (length < 0)
            throw new AssertionError("length must be >= 0: " + length);
        if (offset > size - length) {
            throw new AssertionError("slice exceeds " + sizeName
                    + ": offset=" + offset
                    + ", length=" + length
                    + ", " + sizeName + "=" + size);
        }
        return true;
    }
}
