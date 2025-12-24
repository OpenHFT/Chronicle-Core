/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers OS helper logic for map modes, page alignment, and large offset handling.
 */
class OSLogicTest extends CoreTestCommon {

    @Test
    void imodeForMapsEachMapMode() {
        assertEquals(0, OS.imodeFor(FileChannel.MapMode.READ_ONLY), "READ_ONLY should map to imode 0");
        assertEquals(1, OS.imodeFor(FileChannel.MapMode.READ_WRITE), "READ_WRITE should map to imode 1");
        assertEquals(2, OS.imodeFor(FileChannel.MapMode.PRIVATE), "PRIVATE should map to imode 2");
    }

    @Test
    void pageAlignCustomSizeRoundsUp() {
        int page = 1024;
        long size = 1025;
        assertEquals(2048, OS.pageAlign(size, page), "pageAlign should round up to 2048");
    }

    @Test
    void mapAlignHandlesLargeOffsets() {
        long alignment = 8;
        long offset = Long.MAX_VALUE - 7; // already aligned to 8
        assertEquals(offset, OS.mapAlign(offset, (int) alignment), "aligned offset should be unchanged");

        long offset2 = Long.MAX_VALUE - 10; // not aligned, next multiple fits
        long expected = offset2 + alignment - (offset2 % alignment);
        assertEquals(expected, OS.mapAlign(offset2, (int) alignment), "unaligned offset should round up");
    }
}
