/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeakIdentityHashMapTest extends CoreTestCommon {
    @Test
    void twoKeys() {
        String a1 = Character.toString('a');
        String a2 = Character.toString('a');
        WeakIdentityHashMap<String, Integer> map = new WeakIdentityHashMap<>();
        map.put(a1, 1);
        map.put(a2, 2);
        assertEquals(2, map.size());
        map.clear();
        assertTrue(map.isEmpty());
    }
}
