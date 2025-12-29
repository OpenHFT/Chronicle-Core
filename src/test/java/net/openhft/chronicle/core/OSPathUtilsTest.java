/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OSPathUtilsTest {

    @DisplayName("default page size is not less than actual")
    @Test
    void defaultPageSizeNotLessThanActual() {
        int def = OS.defaultOsPageSize();
        int ps = OS.pageSize();
        assertTrue(def >= ps, "default page size should be >= page size: def=" + def + ", ps=" + ps);
    }

    @DisplayName("findFile returns the last path element")
    @Test
    void findFileReturnsLastElement() {
        File f = OS.findFile("this-path-does-not-exist", "file.txt");
        assertTrue(f.getPath().endsWith("file.txt"),
                "file path should end with \"file.txt\": " + f.getPath());
    }
}
