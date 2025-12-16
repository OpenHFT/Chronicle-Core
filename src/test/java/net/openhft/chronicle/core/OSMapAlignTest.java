/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OSMapAlignTest {

    @Test
    void mapAlignBasicAndEdgeCases() {
        int page = OS.defaultOsPageSize();
        assertEquals(page, OS.mapAlign(1, page), "mapAlignBasicAndEdgeCases: L15");
        assertEquals(page, OS.mapAlign(page, page), "mapAlignBasicAndEdgeCases: L16");
        assertEquals(2L * page, OS.mapAlign(page + 1, page), "mapAlignBasicAndEdgeCases: L17");
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1, page));
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(1, 0));
    }
}

