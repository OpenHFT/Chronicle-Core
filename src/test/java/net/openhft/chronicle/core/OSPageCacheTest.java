/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OSPageCacheTest {

    @Test
    void pageSizeAndMapAlignmentCache() throws Exception {
        int first = OS.pageSize();
        assertTrue(first > 0, "page size " + first + " should be positive on first call");
        Field ps = OS.class.getDeclaredField("pageSize");
        ps.setAccessible(true);
        ps.setInt(null, 0);
        int second = OS.pageSize();
        assertTrue(second > 0, "page size " + second + " should be positive after cache reset");

        long align1 = OS.mapAlignment();
        assertTrue(align1 > 0, "map alignment " + align1 + " should be positive on first call");
        Field ma = OS.class.getDeclaredField("mapAlignment");
        ma.setAccessible(true);
        ma.setInt(null, 0);
        long align2 = OS.mapAlignment();
        assertTrue(align2 > 0, "map alignment " + align2 + " should be positive after cache reset");
        // Values should be stable and positive across recomputation
        assertEquals(first, second, "page size should be stable across recomputation");
        assertEquals(align1, align2, "map alignment should be stable across recomputation");
    }
}
