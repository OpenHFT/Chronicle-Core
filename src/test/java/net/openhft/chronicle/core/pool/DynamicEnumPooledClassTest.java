/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Maths;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DynamicEnumPooledClassTest extends CoreTestCommon {
    @Test
    public void additionalEnum() {
        EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
        assertEquals(YesNo.Yes, yesNoEnumCache.valueOf("Yes"));
        assertEquals(YesNo.No, yesNoEnumCache.valueOf("No"));
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()));

        YesNo maybe = yesNoEnumCache.valueOf("Maybe");
        assertEquals("Maybe", maybe.name());
        assertEquals(2, maybe.ordinal());
        assertEquals("[Yes, No, Maybe]", Arrays.toString(yesNoEnumCache.asArray()));

        YesNo unknown = yesNoEnumCache.valueOf("Unknown");
        assertEquals("Unknown", unknown.name());
        assertEquals(3, unknown.ordinal());
        assertEquals("[Yes, No, Maybe, Unknown]", Arrays.toString(yesNoEnumCache.asArray()));

        // check that asArray returns YesNo instances
        for (YesNo yesNo : yesNoEnumCache.asArray())
            assertEquals(yesNo.name(), yesNo.toString());

        DynamicEnumClass<YesNo> dynamicEnumClass = (DynamicEnumClass<YesNo>) yesNoEnumCache;
        dynamicEnumClass.reset();
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()));
    }

    @Test
    public void testInitialSize() throws IllegalArgumentException {
        EnumCache<EcnDynamic> ecnEnumCache = EnumCache.of(EcnDynamic.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1));
    }
}
