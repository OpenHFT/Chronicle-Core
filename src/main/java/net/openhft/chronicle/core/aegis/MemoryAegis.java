/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.annotation.Address;
import net.openhft.chronicle.core.annotation.NonNegative;
import org.jetbrains.annotations.NotNull;

/**
 * Central assertion helpers for low-level memory access.
 *
 * <p>These helpers are intended for use with Chronicle's
 * {@code assert SKIP_ASSERTIONS || ...} idiom so hot-path checks still
 * compile out in the normal assertions-disabled build while debug builds
 * keep precise range validation and failure messages.</p>
 *
 * <p>Public surface:</p>
 * <ul>
 *   <li>{@link #assertByteArrayRange(byte[], int, int)},
 *       {@link #assertCharArrayRange(char[], int, int)},
 *       {@link #assertStringRange(String, int, int)} - on-heap slice bounds
 *       against the array/string length. Offset and length are {@code int}
 *       because the backing {@code length} / {@link String#length()} is also
 *       {@code int}.</li>
 *   <li>{@link #assertAddressRange(long, long)} - bounded native-address
 *       range against {@link NativeAddressSpace}.</li>
 *   <li>{@link #assertObjectRange(Object, long, long)} - object-relative
 *       byte range bounded by {@link NativeAddressSpace#maxAddressExclusive()}.</li>
 * </ul>
 *
 * <p><b>Unsafe-style on-heap-or-off-heap access:</b> there is no combined
 * helper because the same {@code offset} argument means two different things
 * depending on whether the caller is passing an object reference. Inline the
 * dispatch at the call site so the contract is visible to the reader; see the
 * "Usage patterns" examples below.</p>
 *
 * <p><b>Address-space assumption:</b> these guards intentionally model only
 * ordinary non-negative user-space ranges. They do not attempt to validate
 * negative or sign-extended special mappings such as the legacy x86-64
 * {@code [vsyscall]} mapping visible in {@code /proc/self/maps}. Call sites
 * that genuinely need to touch those regions should not route through
 * {@code MemoryAegis} until Chronicle has a separate policy for them.</p>
 *
 * <h2>Usage patterns</h2>
 *
 * <p>The assertions return {@code true} so they chain cleanly into the
 * {@code assert SKIP_ASSERTIONS || ...} idiom; if SKIP_ASSERTIONS is true
 * the whole line is compiled away and doesn't add to bytecode. if assertions are disabled
 * (the normal production posture) the JIT elides the whole right-hand side.
 * The following examples show the intended adoption pattern for low-level
 * callers such as {@code UnsafeMemory} and helpers built on {@code OS.memory()}.
 * They document how to wire the checks cleanly without claiming that every
 * existing caller has already been migrated.</p>
 *
 * <p><b>Fixed-width primitive read at a native address.</b> The width is the
 * {@code TYPE.BYTES} constant so the assertion covers exactly the range that
 * {@code Unsafe.getXxx} will touch:</p>
 * <pre>{@code
 * public static long unsafeGetLong(@Address long address) {
 *     assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(address, Long.BYTES);
 *     return UNSAFE.getLong(address);
 * }
 * }</pre>
 *
 * <p><b>Fixed-width primitive write into an on-heap byte array.</b> Use the
 * array variant so the check runs against the array length rather than the
 * native-address ceiling:</p>
 * <pre>{@code
 * public static void putInt(byte[] bytes, @NonNegative int offset, int value) {
 *     assert SKIP_ASSERTIONS || MemoryAegis.assertByteArrayRange(bytes, offset, Integer.BYTES);
 *     UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
 * }
 * }</pre>
 *
 * <p><b>Native-to-native copy.</b> Each operand has its own assertion because
 * {@code from} and {@code to} are independent address ranges:</p>
 * <pre>{@code
 * public static void copyMemory(@Address long from, @Address long to, @NonNegative int length) {
 *     assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(from, length);
 *     assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(to, length);
 *     MEMORY.copyMemory(from, to, (long) length);
 * }
 * }</pre>
 *
 * <p><b>Unsafe-style access that may be on-heap or off-heap.</b> When the
 * {@code object} reference is {@code null} the offset is an absolute native
 * address; otherwise it is an object-relative offset (field offset, array
 * base offset + index, etc.). Inline the dispatch at the call site: the
 * ternary keeps both contracts visible to the reader and to any static
 * analyser, which a combined {@code assertObjectOrAddressRange} would
 * obscure behind one ambiguous entry point:</p>
 * <pre>{@code
 * public static void unsafePutBoolean(Object obj, @NonNegative long offset, boolean value) {
 *     assert SKIP_ASSERTIONS || (obj == null
 *             ? MemoryAegis.assertAddressRange(offset, Byte.BYTES)
 *             : MemoryAegis.assertObjectRange(obj, offset, Byte.BYTES));
 *     UNSAFE.putBoolean(obj, offset, value);
 * }
 * }</pre>
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
    public static boolean assertByteArrayRange(final byte @NotNull [] bytes,
                                               final @NonNegative int offset,
                                               final @NonNegative int length) {
        //noinspection ConstantValue
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
    public static boolean assertCharArrayRange(final char @NotNull [] chars,
                                               final @NonNegative int offset,
                                               final @NonNegative int length) {
        //noinspection ConstantValue
        if (chars == null)
            throw new AssertionError("chars must not be null");
        return assertSizedRange("chars.length", chars.length, offset, length);
    }

    /**
     * Asserts that a string slice is valid.
     *
     * <p><b>Unit:</b> {@code start} and {@code length} are counted in
     * {@link String#length()} units (UTF-16 code units), not bytes. Callers
     * reading bytes from a compact-string backing array (Java 9+) must use a
     * byte-unit guard such as {@link #assertObjectRange(Object, long, long)}
     * against the backing array instead.</p>
     *
     * @param text   the source string
     * @param start  the slice start, in {@link String#length()} units
     * @param length the slice length, in {@link String#length()} units
     * @return {@code true}
     * @throws AssertionError if the slice is invalid
     */
    public static boolean assertStringRange(final @NotNull String text,
                                            final @NonNegative int start,
                                            final @NonNegative int length) {
        //noinspection ConstantValue
        if (text == null)
            throw new AssertionError("text must not be null");
        return assertSizedRange("text.length()", text.length(), start, length);
    }

    /**
     * Asserts that the byte range {@code [address, address + length)} lies
     * inside the configured native-address space
     * {@code [NativeAddressSpace.minAddress(), NativeAddressSpace.maxAddressExclusive())}.
     *
     * <p>Rejects: {@code length < 0}; {@code address} in the reserved low
     * guard area (below {@link NativeAddressSpace#minAddress()}); {@code address}
     * at or above {@link NativeAddressSpace#maxAddressExclusive()}; and ranges
     * that would wrap past the ceiling. This deliberately excludes negative
     * special mappings such as the legacy x86-64 {@code [vsyscall]} mapping;
     * {@code MemoryAegis} models ordinary user-space addresses only.</p>
     *
     * @param address the native start address
     * @param length  the number of bytes covered
     * @return {@code true}
     * @throws AssertionError if the range is invalid
     */
    public static boolean assertAddressRange(final @Address long address,
                                             final @NonNegative long length) {
        if (length < 0)
            throw new AssertionError("length must be >= 0: length=" + length);
        if (address < NativeAddressSpace.minAddress())
            throw new AssertionError("address must be >= " + NativeAddressSpace.minAddress()
                    + " (inclusive lower bound): address=" + address);
        if (address >= NativeAddressSpace.maxAddressExclusive())
            throw new AssertionError("address must be <= " + NativeAddressSpace.maxAddressInclusive()
                    + " (inclusive upper bound): address=" + address);
        if (NativeAddressSpace.wouldOverflow(address, length))
            throw new AssertionError("native range exceeds configured address space"
                    + ": address=" + address
                    + ", length=" + length
                    + ", maxAddressExclusive=" + NativeAddressSpace.maxAddressExclusive());
        return true;
    }

    /**
     * Asserts that the byte range {@code [offset, offset + length)} is a
     * valid object-relative Unsafe access on {@code object}.
     *
     * <p>Rejects: {@code object == null}; {@code offset < 0}; {@code length < 0};
     * {@code offset} at or above {@link NativeAddressSpace#maxAddressExclusive()}
     * (the same ceiling native addresses obey, which keeps object offsets and
     * native addresses on one consistent bound); and ranges that would wrap
     * past that ceiling.</p>
     *
     * <p><b>On the ceiling's tightness:</b> real object offsets (field offsets,
     * array base + index) are many orders of magnitude below
     * {@link NativeAddressSpace#maxAddressExclusive()}. The ceiling here is a
     * symmetry choice with {@link #assertAddressRange(long, long)}, not a tight
     * backstop against runaway offsets. The checks that catch real bugs in this
     * method are the non-null, non-negative, and non-overflow guards; do not
     * rely on the ceiling to flag out-of-bounds object accesses.</p>
     *
     * @param object the target object
     * @param offset the starting offset
     * @param length the number of bytes covered
     * @return {@code true}
     * @throws AssertionError if the range is invalid
     */
    public static boolean assertObjectRange(final @NotNull Object object,
                                            final @NonNegative long offset,
                                            final @NonNegative long length) {
        //noinspection ConstantValue
        if (object == null)
            throw new AssertionError("object must not be null");
        if (offset < 0)
            throw new AssertionError("offset must be >= 0: offset=" + offset);
        if (length < 0)
            throw new AssertionError("length must be >= 0: length=" + length);
        if (offset >= NativeAddressSpace.maxAddressExclusive())
            throw new AssertionError("offset must be <= " + NativeAddressSpace.maxAddressInclusive()
                    + " (inclusive upper bound): offset=" + offset);
        if (length >= NativeAddressSpace.maxAddressExclusive())
            throw new AssertionError("length must be <= " + NativeAddressSpace.maxAddressInclusive()
                    + " (inclusive upper bound): length=" + length);
        if (NativeAddressSpace.wouldOverflow(offset, length))
            throw new AssertionError("object range exceeds configured address space"
                    + ": offset=" + offset
                    + ", length=" + length
                    + ", maxAddressExclusive=" + NativeAddressSpace.maxAddressExclusive());
        return true;
    }

    // All three parameters are `int`: `size` is `bytes.length` / `chars.length`
    // / `text.length()`, and the public wrappers now also take `int` offsets /
    // lengths because slices into on-heap collections are naturally int-bounded.
    // `length > size` and `offset > size - length` stay numerically correct
    // given that the earlier negative-checks guarantee both are >= 0 and size
    // is >= 0 by construction.
    private static boolean assertSizedRange(final String sizeName,
                                            final int size,
                                            final int offset,
                                            final int length) {
        if (offset < 0)
            throw new AssertionError("offset must be >= 0: offset=" + offset);
        if (length < 0)
            throw new AssertionError("length must be >= 0: length=" + length);
        // Check length against size before the combined slice check so the
        // failure message names the oversized parameter directly rather than
        // reporting "slice exceeds" for any overflow.
        if (length > size) {
            throw new AssertionError("length exceeds " + sizeName
                    + ": length=" + length
                    + ", " + sizeName + "=" + size);
        }
        if (offset > size - length) {
            throw new AssertionError("slice exceeds " + sizeName
                    + ": offset=" + offset
                    + ", length=" + length
                    + ", " + sizeName + "=" + size
                    + ", end=" + (offset + length));
        }
        return true;
    }
}
