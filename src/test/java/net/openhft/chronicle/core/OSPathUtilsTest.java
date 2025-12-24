/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OSPathUtilsTest {

    @Test
    void defaultPageSizeNotLessThanActual() {
        int def = OS.defaultOsPageSize();
        int ps = OS.pageSize();
        assertTrue(def >= ps, "default OS page size " + def + " should be >= actual page size " + ps);
    }

    @Test
    void findFileReturnsLastElement() {
        File f = OS.findFile("this-path-does-not-exist", "file.txt");
        String path = f.getPath();
        assertTrue(path.endsWith("file.txt"),
                path + " should end with file.txt when directory does not exist");
    }
}
