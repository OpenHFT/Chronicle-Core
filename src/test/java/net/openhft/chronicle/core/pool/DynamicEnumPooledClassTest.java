/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicEnumPooledClassTest extends CoreTestCommon {
    @Test
    public void additionalEnum() {
        EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
        assertEquals(YesNo.Yes, yesNoEnumCache.valueOf("Yes"), "additionalEnum: L18");
        assertEquals(YesNo.No, yesNoEnumCache.valueOf("No"), "additionalEnum: L19");
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()), "additionalEnum: L20");

        YesNo maybe = yesNoEnumCache.valueOf("Maybe");
        assertEquals("Maybe", maybe.name(), "additionalEnum: L23");
        assertEquals(2, maybe.ordinal(), "additionalEnum: L24");
        assertEquals("[Yes, No, Maybe]", Arrays.toString(yesNoEnumCache.asArray()), "additionalEnum: L25");

        YesNo unknown = yesNoEnumCache.valueOf("Unknown");
        assertEquals("Unknown", unknown.name(), "additionalEnum: L28");
        assertEquals(3, unknown.ordinal(), "additionalEnum: L29");
        assertEquals("[Yes, No, Maybe, Unknown]", Arrays.toString(yesNoEnumCache.asArray()), "additionalEnum: L30");

        // check that asArray returns YesNo instances
        for (YesNo yesNo : yesNoEnumCache.asArray())
            assertEquals(yesNo.name(), yesNo.toString(), "additionalEnum: L34");

        DynamicEnumClass<YesNo> dynamicEnumClass = (DynamicEnumClass<YesNo>) yesNoEnumCache;
        dynamicEnumClass.reset();
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()), "additionalEnum: L38");
    }

    @Test
    public void testInitialSize() throws IllegalArgumentException {
        EnumCache<EcnDynamic> ecnEnumCache = EnumCache.of(EcnDynamic.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1), "testInitialSize: L44");
    }
}
