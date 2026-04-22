/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Memory;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.Address;
import net.openhft.chronicle.core.annotation.NonNegative;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Demonstrates the {@code assert SKIP_ASSERTIONS || MemoryAegis.assertXxx(...)}
 * idiom against real {@link OS#memory()} operations.
 *
 * <p>The fixtures below deliberately use the same public memory primitives that
 * downstream code already has today: primitive read at a native address,
 * primitive write into an on-heap byte array, native-to-native copy, and
 * object-or-address dispatch. Each test runs with assertions enabled (the
 * default for {@code mvn test}) so the guarded branches execute; in the normal
 * assertion-disabled production run the JIT elides the whole right-hand side
 * and the helpers become the equivalent of their bodies minus the guard.</p>
 *
 * <p>These samples document the intended integration pattern for future
 * migration work. They are not evidence that every existing low-level caller
 * has already been wired to {@link MemoryAegis}.</p>
 */
@DisplayName("MemoryAegis canonical `assert SKIP_ASSERTIONS || ...` usage")
class MemoryAegisUsageTest {

    private static final Memory MEMORY = OS.memory();

    // Deliberately shadows {@link net.openhft.chronicle.assertions.AssertUtil#SKIP_ASSERTIONS}
    // with a {@code false} local so the aegis branches actually execute in
    // this test. Chronicle-Core's test classpath ships the assertions-disabled
    // twin (where the shared constant is {@code true}); importing it would
    // constant-fold every guard to a no-op, and the hostile-input fixtures
    // below would then reach {@link Memory#readLong(long)} with bogus
    // addresses and crash the JVM instead of tripping an AssertionError.
    private static final boolean SKIP_ASSERTIONS = false;

    @Test
    @DisplayName("primitive read at a native address uses assertAddressRange with TYPE.BYTES")
    void demonstratesAddressRangePrimitiveRead() {
        final long address = MEMORY.allocate(Long.BYTES);
        try {
            MEMORY.writeLong(address, 0x0102_0304_0506_0708L);
            final long sample = sampleUnsafeGetLong(address);
            assertEquals(0x0102_0304_0506_0708L, sample);
        } finally {
            MEMORY.freeMemory(address, Long.BYTES);
        }
    }

    @Test
    @DisplayName("on-heap byte-array slice uses assertByteArrayRange with TYPE.BYTES")
    void demonstratesByteArrayRangePrimitiveWrite() {
        final byte[] bytes = new byte[16];
        final long baseOffset = MEMORY.arrayBaseOffset(byte[].class);

        samplePutInt(bytes, 4, 0x01020304);

        assertEquals(0, bytes[0]);
        assertEquals(0, bytes[3]);
        assertEquals(0x01020304, MEMORY.readInt(bytes, baseOffset + 4));
        assertEquals(0, bytes[8]);
        assertEquals(0, bytes[15]);
    }

    @Test
    @DisplayName("native-to-native copy guards source and destination independently")
    void demonstratesTwoAssertAddressRangeCalls() {
        final long from = MEMORY.allocate(Long.BYTES);
        final long to = MEMORY.allocate(Long.BYTES);
        try {
            MEMORY.writeLong(from, 0x1122_3344_5566_7788L);

            // Exercising both guards in one call verifies that `to` does not
            // silently piggy-back on the `from` check when they differ.
            sampleCopyMemory(from, to, Long.BYTES);

            assertEquals(0x1122_3344_5566_7788L, MEMORY.readLong(to));
        } finally {
            MEMORY.freeMemory(to, Long.BYTES);
            MEMORY.freeMemory(from, Long.BYTES);
        }
    }

    @Test
    @DisplayName("Unsafe-style access inlines the on-heap / off-heap dispatch at the call site")
    void demonstratesObjectOrAddressRangeDispatch() {
        final ByteFieldHolder onHeap = new ByteFieldHolder();
        final long fieldOffset = UnsafeMemory.unsafeObjectFieldOffset(
                Jvm.getField(ByteFieldHolder.class, "value"));
        sampleUnsafePutByte(onHeap, fieldOffset);
        assertEquals((byte) 1, MEMORY.readByte(onHeap, fieldOffset));

        final long nativeAddress = MEMORY.allocate(Byte.BYTES);
        try {
            sampleUnsafePutByte(null, nativeAddress);
            assertEquals((byte) 1, MEMORY.readByte(nativeAddress));
        } finally {
            MEMORY.freeMemory(nativeAddress, Byte.BYTES);
        }
    }

    @Test
    @DisplayName("assertion guards still fire under hostile inputs when assertions are enabled")
    void sampleHelpersStillRejectBadInputs() {
        // Passing an address below the reserved lower bound must still trip
        // the assertion when the idiom is active.
        assertThrows(AssertionError.class,
                () -> sampleUnsafeGetLong(NativeAddressSpace.minAddress() - 1));
        assertThrows(AssertionError.class,
                () -> samplePutInt(new byte[4], 2, 0));
    }

    /* ------------------------------------------------------------------ *
     * Hand-rolled fixtures that mirror real UnsafeMemory call sites.     *
     * ------------------------------------------------------------------ */

    /**
     * Fixture showing the intended native-read pattern. The address range is
     * checked for {@link Long#BYTES} bytes, matching the width of the
     * subsequent read.
     */
    private static long sampleUnsafeGetLong(@Address final long address) {
        assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(address, Long.BYTES);
        return MEMORY.readLong(address);
    }

    /**
     * Fixture showing the intended on-heap byte-array pattern. The byte-array
     * range is checked for {@link Integer#BYTES} bytes at the caller-supplied
     * offset.
     */
    private static void samplePutInt(final byte[] bytes,
                                     @NonNegative final int offset,
                                     final int value) {
        assert SKIP_ASSERTIONS || MemoryAegis.assertByteArrayRange(bytes, offset, Integer.BYTES);
        MEMORY.writeInt(bytes, MEMORY.arrayBaseOffset(byte[].class) + offset, value);
    }

    /**
     * Fixture showing the intended native-to-native copy pattern. The caller
     * uses two separate assertions because {@code from} and {@code to} are
     * independent native-address ranges.
     */
    private static void sampleCopyMemory(@Address final long from,
                                         @Address final long to,
                                         @NonNegative final int length) {
        assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(from, length);
        assert SKIP_ASSERTIONS || MemoryAegis.assertAddressRange(to, length);
        MEMORY.copyMemory(from, to, (long) length);
    }

    /**
     * Fixture showing the intended object-or-address dispatch pattern. The
     * call site inlines the null-check: when {@code obj == null} the offset is
     * an absolute native address and goes through
     * {@link MemoryAegis#assertAddressRange(long, long)}; otherwise it is an
     * object-relative offset and goes through
     * {@link MemoryAegis#assertObjectRange(Object, long, long)}. Keeping the
     * ternary at the call site makes the two contracts visible to both the
     * reader and static analysis.
     */
    private static void sampleUnsafePutByte(final Object obj,
                                               @NonNegative final long offset) {
        assert SKIP_ASSERTIONS || (obj == null
                ? MemoryAegis.assertAddressRange(offset, Byte.BYTES)
                : MemoryAegis.assertObjectRange(obj, offset, Byte.BYTES));
        if (obj == null) {
            MEMORY.writeByte(offset, (byte) 1);
        } else {
            MEMORY.writeByte(obj, offset, (byte) 1);
        }
    }

    private static final class ByteFieldHolder {
        volatile byte value;
    }
}
