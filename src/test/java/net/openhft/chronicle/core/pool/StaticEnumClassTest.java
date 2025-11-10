//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class StaticEnumClassTest extends CoreTestCommon {

    @Test
    public void testInitialSize() throws IllegalArgumentException {
        EnumCache<Ecn> ecnEnumCache = EnumCache.of(Ecn.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1));
    }
}
