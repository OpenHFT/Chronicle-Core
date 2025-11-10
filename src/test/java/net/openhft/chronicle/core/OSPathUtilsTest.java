//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        assertTrue(def >= ps);
    }

    @Test
    void findFileReturnsLastElement() {
        File f = OS.findFile("this-path-does-not-exist", "file.txt");
        assertTrue(f.getPath().endsWith("file.txt"));
    }
}

