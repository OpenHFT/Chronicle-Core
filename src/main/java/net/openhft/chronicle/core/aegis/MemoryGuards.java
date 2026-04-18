/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.annotation.NonNegative;

import java.util.Objects;

/**
 * Runtime proof helpers for off-heap and on-heap range validation. Chronicle's
 * security tooling ({@code CSCallerCheckedBounds}) recognises calls to the
 * methods here as valid discharge of a caller-owned slice obligation.
 *
 * <p>For on-heap sites with a known array length, prefer
 * {@link Objects#checkFromIndexSize(int, int, int)}; this class covers the
 * off-heap cases where the source / destination size is known separately
 * from the starting address.</p>
 */
public final class MemoryGuards {

    private MemoryGuards() {
    }

    /**
     * Validates that the copy region fits within both the source and
     * destination regions.
     *
     * @param srcAddress the source region start address
     * @param srcSize    the source region size (in bytes)
     * @param from       the offset into the source region
     * @param dstAddress the destination region start address
     * @param dstSize    the destination region size (in bytes)
     * @param to         the offset into the destination region
     * @param length     the number of bytes to copy
     * @throws IndexOutOfBoundsException if either projection exceeds its
     *                                   region, any argument is negative,
     *                                   or the arithmetic overflows
     */
    public static void requireCopyRange(final long srcAddress,
                                        @NonNegative final long srcSize,
                                        @NonNegative final long from,
                                        final long dstAddress,
                                        @NonNegative final long dstSize,
                                        @NonNegative final long to,
                                        @NonNegative final long length) {
        requireRange(srcAddress, srcSize, from, length);
        requireRange(dstAddress, dstSize, to, length);
    }

    /**
     * Validates that {@code [address + offset, address + offset + length)}
     * fits inside {@code [address, address + size)}.
     *
     * @param address the region start address
     * @param size    the region size (in bytes)
     * @param offset  the offset into the region
     * @param length  the number of bytes to cover
     * @throws IndexOutOfBoundsException if the projection exceeds the
     *                                   region, any argument is negative,
     *                                   the base address is zero, or the
     *                                   arithmetic overflows
     */
    public static void requireRange(final long address,
                                    @NonNegative final long size,
                                    @NonNegative final long offset,
                                    @NonNegative final long length) {
        if (!isValidRange(address, size, offset, length)) {
            throw new IndexOutOfBoundsException(
                    "address=" + address + " size=" + size
                            + " offset=" + offset + " length=" + length);
        }
    }

    /**
     * Non-throwing variant of {@link #requireCopyRange(long, long, long, long, long, long, long)}
     * suitable for use as an {@code if}-guard. Returns {@code true} when the
     * copy fits in both regions, {@code false} otherwise.
     *
     * @param srcAddress the source region start address
     * @param srcSize    the source region size (in bytes)
     * @param from       the offset into the source region
     * @param dstAddress the destination region start address
     * @param dstSize    the destination region size (in bytes)
     * @param to         the offset into the destination region
     * @param length     the number of bytes to copy
     * @return {@code true} iff the copy fits in both regions
     */
    public static boolean isValidCopyRange(final long srcAddress,
                                           final long srcSize,
                                           final long from,
                                           final long dstAddress,
                                           final long dstSize,
                                           final long to,
                                           final long length) {
        return isValidRange(srcAddress, srcSize, from, length)
                && isValidRange(dstAddress, dstSize, to, length);
    }

    /**
     * Non-throwing variant of {@link #requireRange(long, long, long, long)}
     * suitable for use as an {@code if}-guard.
     *
     * @param address the region start address
     * @param size    the region size (in bytes)
     * @param offset  the offset into the region
     * @param length  the number of bytes to cover
     * @return {@code true} iff the projection fits in the region without
     * overflowing the addressed native range
     */
    public static boolean isValidRange(final long address,
                                       final long size,
                                       final long offset,
                                       final long length) {
        if (address <= 0 || size < 0 || offset < 0 || length < 0)
            return false;
        if (address > Long.MAX_VALUE - size)
            return false;
        return offset <= size - length;
    }
}
