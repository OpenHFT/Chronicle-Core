/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicEnumPooledClassTest extends CoreTestCommon {
    @DisplayName("Dynamic enum cache adds new constants on demand")
    @Test
    void additionalEnum() {
        EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
        assertEquals(YesNo.Yes, yesNoEnumCache.valueOf("Yes"), "valueOf should return Yes enum constant");
        assertEquals(YesNo.No, yesNoEnumCache.valueOf("No"), "valueOf should return No enum constant");
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()), "initial enum cache should contain only declared constants");

        YesNo maybe = yesNoEnumCache.valueOf("Maybe");
        assertEquals("Maybe", maybe.name(), "dynamically created enum should have correct name");
        assertEquals(2, maybe.ordinal(), "dynamically created enum should have next ordinal value");
        assertEquals("[Yes, No, Maybe]", Arrays.toString(yesNoEnumCache.asArray()), "enum cache should include dynamically created constant");

        YesNo unknown = yesNoEnumCache.valueOf("Unknown");
        assertEquals("Unknown", unknown.name(), "second dynamic enum should have correct name");
        assertEquals(3, unknown.ordinal(), "second dynamic enum should have incremented ordinal value");
        assertEquals("[Yes, No, Maybe, Unknown]", Arrays.toString(yesNoEnumCache.asArray()), "enum cache should include all dynamic constants");

        // check that asArray returns YesNo instances
        for (YesNo yesNo : yesNoEnumCache.asArray())
            assertEquals(yesNo.name(), yesNo.toString(), "enum toString should match its name: " + yesNo);

        DynamicEnumClass<YesNo> dynamicEnumClass = (DynamicEnumClass<YesNo>) yesNoEnumCache;
        dynamicEnumClass.reset();
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()), "reset should remove dynamically created constants");
    }

    @DisplayName("Dynamic enum cache size rounds to power of two")
    @Test
    void testInitialSize() throws IllegalArgumentException {
        EnumCache<EcnDynamic> ecnEnumCache = EnumCache.of(EcnDynamic.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1), "enum cache size should be rounded to power of 2");
    }
}
