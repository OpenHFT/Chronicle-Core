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
        assertTrue(first > 0);
        Field ps = OS.class.getDeclaredField("pageSize");
        ps.setAccessible(true);
        ps.setInt(null, 0);
        int second = OS.pageSize();
        assertTrue(second > 0);

        long align1 = OS.mapAlignment();
        assertTrue(align1 > 0);
        Field ma = OS.class.getDeclaredField("mapAlignment");
        ma.setAccessible(true);
        ma.setInt(null, 0);
        long align2 = OS.mapAlignment();
        assertTrue(align2 > 0);
        // Values should be stable and positive across recomputation
        assertEquals(first, second);
        assertEquals(align1, align2);
    }
}
