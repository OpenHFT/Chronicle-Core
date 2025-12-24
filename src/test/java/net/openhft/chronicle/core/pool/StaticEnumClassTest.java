/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
class StaticEnumClassTest extends CoreTestCommon {

    @Test
    void testInitialSize() throws IllegalArgumentException {
        EnumCache<Ecn> ecnEnumCache = EnumCache.of(Ecn.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1), "EnumCache size should round up to next power of 2 equal to 32");
    }
}
