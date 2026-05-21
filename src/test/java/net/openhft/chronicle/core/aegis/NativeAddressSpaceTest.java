/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NativeAddressSpace policy helper behavior tests")
class NativeAddressSpaceTest {

    @Test
    @DisplayName("passes x86-64 aliases through unchanged (falls through to the 56-bit default)")
    void normalizesCommonX8664Aliases() {
        assertEquals("amd64", NativeAddressSpace.normalizeArch("amd64"));
        assertEquals("x64", NativeAddressSpace.normalizeArch("x64"));
        assertEquals("x86_64", NativeAddressSpace.normalizeArch("x86_64"));
    }

    @Test
    @DisplayName("normalizes common 32-bit aliases")
    void normalizesCommon32BitAliases() {
        assertEquals("x86_32", NativeAddressSpace.normalizeArch("i686"));
        assertEquals("x86_32", NativeAddressSpace.normalizeArch("x86"));
        assertEquals("arm_32", NativeAddressSpace.normalizeArch("armv7l"));
        assertEquals("arm_32", NativeAddressSpace.normalizeArch("arm32"));
    }

    @Test
    @DisplayName("handles a null architecture string")
    void normalizeArchHandlesNull() {
        assertEquals("", NativeAddressSpace.normalizeArch(null));
    }

    @Test
    @DisplayName("trims and lowercases architecture strings")
    void normalizeArchTrimsAndLowercases() {
        // x86-64 aliases have no dedicated bucket, so "  AMD64  " comes back
        // lower-cased and trimmed; only the 32-bit buckets do explicit mapping.
        assertEquals("amd64", NativeAddressSpace.normalizeArch("  AMD64  "));
        assertEquals("arm_32", NativeAddressSpace.normalizeArch("ARMv7l"));
    }

    @Test
    @DisplayName("leaves unknown architecture values normalized but otherwise intact")
    void normalizeArchLeavesUnknownValuesUntouched() {
        assertEquals("aarch64", NativeAddressSpace.normalizeArch("AArch64"));
        assertEquals("riscv64", NativeAddressSpace.normalizeArch("riscv64"));
    }

    @Test
    @DisplayName("keeps the 56-bit default for x86-64 aliases")
    void keeps56BitDefaultForX8664() {
        assertEquals(56, NativeAddressSpace.addressBitsFor("amd64"));
        assertEquals(56, NativeAddressSpace.addressBitsFor("x86_64"));
    }

    @Test
    @DisplayName("uses 32 bits for obvious 32-bit architectures")
    void uses32BitsForObvious32BitArchitectures() {
        assertEquals(32, NativeAddressSpace.addressBitsFor("i686"));
        assertEquals(32, NativeAddressSpace.addressBitsFor("armv7l"));
    }

    @Test
    @DisplayName("keeps the 56-bit default for arm64 and unknown architectures")
    void keeps56BitDefaultForArm64AndUnknownArchitectures() {
        assertEquals(56, NativeAddressSpace.addressBitsFor("aarch64"));
        assertEquals(56, NativeAddressSpace.addressBitsFor("riscv64"));
    }

    @Test
    @DisplayName("defaults null architecture input to the 56-bit fallback")
    void addressBitsForHandlesNullArch() {
        assertEquals(56, NativeAddressSpace.addressBitsFor(null));
    }

    @Test
    @DisplayName("normalizes the empty architecture string to the empty bucket")
    void normalizeArchHandlesEmpty() {
        assertEquals("", NativeAddressSpace.normalizeArch(""));
        assertEquals("", NativeAddressSpace.normalizeArch("   "));
    }

    @Test
    @DisplayName("defaults the empty architecture string to the 56-bit fallback")
    void addressBitsForHandlesEmpty() {
        assertEquals(56, NativeAddressSpace.addressBitsFor(""));
    }

    @Test
    @DisplayName("reserves the first 64 KiB of address space")
    void minAddressReservesFirst64KiB() {
        assertEquals(64L << 10, NativeAddressSpace.minAddress());
    }

    @Test
    @DisplayName("defines the inclusive upper bound as one below the exclusive upper bound")
    void maxAddressInclusiveIsOneBelowExclusive() {
        assertEquals(NativeAddressSpace.maxAddressExclusive() - 1,
                NativeAddressSpace.maxAddressInclusive());
    }

    @Test
    @DisplayName("computes the exclusive upper bound from the configured address bits")
    void maxAddressExclusiveMatchesBoundsHolderBits() {
        final long expected = 1L << NativeAddressSpace.BoundsHolder.ADDRESS_BITS;
        assertEquals(expected, NativeAddressSpace.maxAddressExclusive());
    }

    @Test
    @DisplayName("keeps the reserved lower bound below the inclusive upper bound")
    void minAddressIsBelowMaxAddressInclusive() {
        assertTrue(NativeAddressSpace.minAddress() < NativeAddressSpace.maxAddressInclusive());
    }

    @Test
    @DisplayName("reports overflow when a range extends past the ceiling")
    void wouldOverflowAtCeiling() {
        assertTrue(NativeAddressSpace.wouldOverflow(NativeAddressSpace.maxAddressInclusive(), 2));
    }

    @Test
    @DisplayName("does not report overflow for a range that fits below the ceiling")
    void wouldOverflowFalseForFittingRange() {
        assertFalse(NativeAddressSpace.wouldOverflow(NativeAddressSpace.minAddress(), 8));
    }

    @Test
    @DisplayName("does not report overflow for a single-byte range ending exactly at the ceiling")
    void wouldOverflowFalseForRangeEndingExactlyAtCeiling() {
        assertFalse(NativeAddressSpace.wouldOverflow(NativeAddressSpace.maxAddressInclusive(), 1));
    }

    @Test
    @DisplayName("reports overflow at the exclusive upper bound when length is positive")
    void wouldOverflowTrueAtExclusiveUpperBoundWithPositiveLength() {
        assertTrue(NativeAddressSpace.wouldOverflow(NativeAddressSpace.maxAddressExclusive(), 1));
    }

    @Test
    @DisplayName("does not report overflow for zero-length access at the exclusive upper bound")
    void wouldOverflowFalseForZeroLengthAtCeiling() {
        assertFalse(NativeAddressSpace.wouldOverflow(NativeAddressSpace.maxAddressExclusive(), 0));
    }
}
