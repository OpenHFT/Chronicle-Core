/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OSPageAlignmentTest {

    @Test
    void pageAlignAlignsToCurrentPageSize() {
        int pageSize = OS.pageSize();
        long base = 123;
        long aligned = OS.pageAlign(base);
        assertTrue(aligned >= base, "aligned value " + aligned + " should be >= base " + base);
        assertEquals(0L, aligned % pageSize, "memory should be aligned to page boundary");

        long large = (long) pageSize * 123456 + 7;
        long expected = ((large + pageSize - 1) / pageSize) * pageSize;
        assertEquals(expected, OS.pageAlign(large), "pageAlign should round up large values to next page boundary");
    }

    @Test
    void defaultOsPageSizeFallsBackToSafeSizeOnWindows() {
        int defaultSize = OS.defaultOsPageSize();
        if (OS.isWindows()) {
            assertEquals(OS.SAFE_PAGE_SIZE, defaultSize, "default OS page size should use SAFE_PAGE_SIZE on Windows");
        } else {
            assertEquals(OS.pageSize(), defaultSize, "defaultOsPageSize should equal actual page size on non-Windows platforms");
        }
    }
}
