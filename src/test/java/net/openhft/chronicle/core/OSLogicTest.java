/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;

/**
 * Covers small logic helpers inside OS.
 */
public class OSLogicTest extends CoreTestCommon {

    @Test
    public void imodeForMapsEachMapMode() {
        assertEquals(0, OS.imodeFor(FileChannel.MapMode.READ_ONLY));
        assertEquals(1, OS.imodeFor(FileChannel.MapMode.READ_WRITE));
        assertEquals(2, OS.imodeFor(FileChannel.MapMode.PRIVATE));
    }

    @Test
    public void pageAlignCustomSizeRoundsUp() {
        int page = 1024;
        long size = 1025;
        assertEquals(2048, OS.pageAlign(size, page));
    }

    @Test
    public void mapAlignHandlesLargeOffsets() {
        long alignment = 8;
        long offset = Long.MAX_VALUE - 7; // already aligned to 8
        assertEquals(offset, OS.mapAlign(offset, (int) alignment));

        long offset2 = Long.MAX_VALUE - 10; // not aligned, next multiple fits
        long expected = offset2 + alignment - (offset2 % alignment);
        assertEquals(expected, OS.mapAlign(offset2, (int) alignment));
    }
}
