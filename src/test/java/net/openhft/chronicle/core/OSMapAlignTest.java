/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OSMapAlignTest {

    @Test
    @DisplayName("Map align basic and edge cases")
    void mapAlignBasicAndEdgeCases() {
        int page = OS.defaultOsPageSize();
        assertEquals(page, OS.mapAlign(1, page), "mapAlign should round up size less than page to one page");
        assertEquals(page, OS.mapAlign(page, page), "mapAlign should return page size when size equals page");
        assertEquals(2L * page, OS.mapAlign(page + 1, page), "mapAlign should round up size beyond page to two pages");
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1, page),
                "mapAlign should reject negative size values");
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(1, 0),
                "mapAlign should reject non-positive page size");
    }
}
