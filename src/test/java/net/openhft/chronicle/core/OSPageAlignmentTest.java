/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OSPageAlignmentTest {

    @Test
    public void pageAlignAlignsToCurrentPageSize() {
        int pageSize = OS.pageSize();
        long base = 123;
        long aligned = OS.pageAlign(base);
        assertTrue("Aligned value should be >= base", aligned >= base);
        assertEquals(0L, aligned % pageSize);

        long large = (long) pageSize * 123456 + 7;
        long expected = ((large + pageSize - 1) / pageSize) * pageSize;
        assertEquals(expected, OS.pageAlign(large));
    }

    @Test
    public void defaultOsPageSizeFallsBackToSafeSizeOnWindows() {
        int defaultSize = OS.defaultOsPageSize();
        if (OS.isWindows()) {
            assertEquals(OS.SAFE_PAGE_SIZE, defaultSize);
        } else {
            assertEquals(OS.pageSize(), defaultSize);
        }
    }
}
