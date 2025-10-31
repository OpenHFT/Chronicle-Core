/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

