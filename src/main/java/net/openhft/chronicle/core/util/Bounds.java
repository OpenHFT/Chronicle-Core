/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.aegis.NativeAddressSpace;

/**
 * Meta-type-aware bounds checks for {@code long} and {@code int} values. Each
 * helper carries the constraint set appropriate to its meta-type and requires
 * a {@code message} naming the value being checked.
 * <p>
 * Coordinate helpers ({@link #requireOffset(long, String)},
 * {@link #requirePosition(long, String)}, {@link #requireCapacity(long, String)},
 * {@link #requireSize(long, String)}) enforce
 * {@code 0 <= value < NativeAddressSpace.maxAddressExclusive()}. The single
 * combined check {@code (value & maxAddressInclusive()) != value} catches
 * both negative values and values that overflow the configured address space
 * in one operation, because {@code maxAddressInclusive()} is
 * {@code (2^N) - 1} and therefore doubles as the low-bit mask.
 * <p>
 * {@link #requireAddress(long, String)} additionally enforces
 * {@code value >= NativeAddressSpace.minAddress()}, matching the
 * native-address contract used by
 * {@link net.openhft.chronicle.core.aegis.MemoryAegis#assertAddressRange(long, long)}.
 * <p>
 * {@link #requireNonNegative(long, String)} is the generic fallback for
 * {@code @NonNegative} fields without a more specific meta-type and applies
 * the simple {@code value >= 0} check only.
 * <p>
 * Use a meta-type-specific helper rather than the generic
 * {@link Longs#requireNonNegative(long)} so the call site documents intent
 * and downstream tooling (aegis) can verify the helper matches the receiver's
 * meta-type.
 */
public final class Bounds {

    private Bounds() {
    }

    /**
     * Returns {@code value} after checking it is a valid native address:
     * {@code value >= minAddress()} and {@code value < maxAddressExclusive()}.
     *
     * @param value   the address to check
     * @param message names the value being checked, used in the error message
     * @return the provided {@code value}
     * @throws IllegalArgumentException if {@code value} is not in
     *                                  {@code [minAddress(), maxAddressInclusive()]}
     */
    public static long requireAddress(final long value, final String message) {
        if (value < NativeAddressSpace.minAddress()
                || (value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (address) must be in ["
                    + NativeAddressSpace.minAddress() + ", "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid file offset:
     * {@code 0 <= value < maxAddressExclusive()}.
     */
    public static long requireOffset(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (offset) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid position:
     * {@code 0 <= value < maxAddressExclusive()}.
     */
    public static long requirePosition(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (position) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid capacity:
     * {@code 0 <= value < maxAddressExclusive()}.
     */
    public static long requireCapacity(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (capacity) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid size:
     * {@code 0 <= value < maxAddressExclusive()}.
     */
    public static long requireSize(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (size) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid range start
     * (a position-like coordinate): {@code 0 <= value < maxAddressExclusive()}.
     */
    public static long requireStart(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (start) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is a valid exclusive end
     * (a position-like coordinate): {@code 0 <= value < maxAddressExclusive()}.
     * Used for `readLimit`-style end-exclusive boundaries.
     */
    public static long requireLimit(final long value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (limit) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking it is non-negative. Prefer a
     * meta-type-specific helper when the meta-type is known; use this only
     * as the generic fallback for {@code @NonNegative}.
     */
    public static long requireNonNegative(final long value, final String message) {
        if (value < 0)
            throw new IllegalArgumentException(message + " must be non-negative: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireAddress(long, String)}.
     */
    public static int requireAddress(final int value, final String message) {
        if (value < NativeAddressSpace.minAddress()
                || (value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (address) must be in ["
                    + NativeAddressSpace.minAddress() + ", "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireOffset(long, String)}.
     */
    public static int requireOffset(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (offset) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requirePosition(long, String)}.
     */
    public static int requirePosition(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (position) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireCapacity(long, String)}.
     */
    public static int requireCapacity(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (capacity) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireSize(long, String)}.
     */
    public static int requireSize(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (size) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireStart(long, String)}.
     */
    public static int requireStart(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (start) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireLimit(long, String)}.
     */
    public static int requireLimit(final int value, final String message) {
        if ((value & NativeAddressSpace.maxAddressInclusive()) != value)
            throw new IllegalArgumentException(message + " (limit) must be in [0, "
                    + NativeAddressSpace.maxAddressInclusive() + "]: " + value);
        return value;
    }

    /**
     * {@code int} overload of {@link #requireNonNegative(long, String)}.
     */
    public static int requireNonNegative(final int value, final String message) {
        if (value < 0)
            throw new IllegalArgumentException(message + " must be non-negative: " + value);
        return value;
    }
}
