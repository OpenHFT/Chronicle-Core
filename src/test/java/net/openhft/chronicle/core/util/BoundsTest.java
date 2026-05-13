/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.aegis.NativeAddressSpace;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BoundsTest {

    @Test
    public void requireOffsetAcceptsZeroSentinel() {
        assertEquals(0L, Bounds.requireOffset(0L, "off"));
    }

    @Test
    public void requireOffsetAcceptsInclusiveMaximum() {
        assertEquals(NativeAddressSpace.maxAddressInclusive(),
                Bounds.requireOffset(NativeAddressSpace.maxAddressInclusive(), "off"));
    }

    @Test
    public void requireOffsetRejectsNegativeValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireOffset(-1L, "off"));
        assertContains(ex.getMessage(), "off (offset)");
        assertContains(ex.getMessage(), "-1");
    }

    @Test
    public void requireOffsetRejectsValueAtMaxAddressExclusive() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireOffset(NativeAddressSpace.maxAddressExclusive(), "off"));
    }

    @Test
    public void requireOffsetRejectsLongMaxValue() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireOffset(Long.MAX_VALUE, "off"));
    }

    @Test
    public void requirePositionRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requirePosition(-1L, "pos"));
    }

    @Test
    public void requireCapacityRejectsOversize() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireCapacity(NativeAddressSpace.maxAddressExclusive(), "cap"));
    }

    @Test
    public void requireSizeAcceptsValidValues() {
        assertEquals(0L, Bounds.requireSize(0L, "size"));
        assertEquals(1024L, Bounds.requireSize(1024L, "size"));
    }

    @Test
    public void requireSizeRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireSize(-1L, "size"));
    }

    @Test
    public void requireAddressAcceptsConfiguredMinAddress() {
        assertEquals(NativeAddressSpace.minAddress(),
                Bounds.requireAddress(NativeAddressSpace.minAddress(), "addr"));
    }

    @Test
    public void requireAddressRejectsReservedLowAddressSpace() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireAddress(NativeAddressSpace.minAddress() - 1, "addr"));
        assertContains(ex.getMessage(), "addr (address)");
    }

    @Test
    public void requireAddressRejectsZero() {
        // Zero is below minAddress() (64 KiB) so it must be rejected.
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireAddress(0L, "addr"));
    }

    @Test
    public void requireAddressRejectsOversize() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireAddress(NativeAddressSpace.maxAddressExclusive(), "addr"));
    }

    @Test
    public void requireNonNegativeAcceptsZeroAndPositive() {
        assertEquals(0L, Bounds.requireNonNegative(0L, "n"));
        assertEquals(Long.MAX_VALUE, Bounds.requireNonNegative(Long.MAX_VALUE, "n"));
    }

    @Test
    public void requireNonNegativeRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> Bounds.requireNonNegative(-1L, "n"));
    }

    @Test
    public void intOverloadsApplySameRules() {
        assertEquals(0, Bounds.requireOffset(0, "off"));
        assertEquals(1024, Bounds.requirePosition(1024, "pos"));
        assertThrows(IllegalArgumentException.class, () -> Bounds.requireOffset(-1, "off"));
        assertThrows(IllegalArgumentException.class, () -> Bounds.requireCapacity(-1, "cap"));
        assertThrows(IllegalArgumentException.class, () -> Bounds.requireSize(-1, "size"));
        assertThrows(IllegalArgumentException.class, () -> Bounds.requireAddress(-1, "addr"));
        assertThrows(IllegalArgumentException.class, () -> Bounds.requireNonNegative(-1, "n"));
    }

    private static void assertContains(final String haystack, final String needle) {
        if (haystack == null || !haystack.contains(needle))
            throw new AssertionError("expected message to contain '" + needle + "' but got: " + haystack);
    }
}
