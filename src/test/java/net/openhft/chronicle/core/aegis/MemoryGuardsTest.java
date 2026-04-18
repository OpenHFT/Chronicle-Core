/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.aegis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MemoryGuardsTest {

    @Test
    public void validRangeAcceptsInBoundsProjection() {
        assertTrue(MemoryGuards.isValidRange(64, 32, 4, 8));
        MemoryGuards.requireRange(64, 32, 4, 8);
    }

    @Test
    public void validRangeRejectsZeroAddress() {
        assertFalse(MemoryGuards.isValidRange(0, 32, 4, 8));
        assertThrows(IndexOutOfBoundsException.class,
                () -> MemoryGuards.requireRange(0, 32, 4, 8));
    }

    @Test
    public void validRangeRejectsWrappedRegion() {
        assertFalse(MemoryGuards.isValidRange(Long.MAX_VALUE - 3, 8, 0, 4));
        assertThrows(IndexOutOfBoundsException.class,
                () -> MemoryGuards.requireRange(Long.MAX_VALUE - 3, 8, 0, 4));
    }

    @Test
    public void validCopyRangeRejectsWrappedDestination() {
        assertFalse(MemoryGuards.isValidCopyRange(64, 32, 4,
                Long.MAX_VALUE - 3, 8, 0, 4));
        assertThrows(IndexOutOfBoundsException.class,
                () -> MemoryGuards.requireCopyRange(64, 32, 4,
                        Long.MAX_VALUE - 3, 8, 0, 4));
    }
}
