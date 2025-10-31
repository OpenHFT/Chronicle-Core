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

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

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

