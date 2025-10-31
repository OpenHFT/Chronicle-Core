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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmUtilitiesTest extends CoreTestCommon {

    private static final String SIZE_PROPERTY = "chronicle.core.test.size";
    private static final String FLAG_PROPERTY = "chronicle.core.test.flag";

    @AfterEach
    void clearTestProperties() {
        System.clearProperty(SIZE_PROPERTY);
        System.clearProperty(FLAG_PROPERTY);
    }

    @Test
    void majorVersionMatchesSpecification() {
        assertEquals(detectMajorVersionFromSpecification(), Jvm.majorVersion());
    }

    @Test
    void getBooleanUnderstandsEmptyAndFalseValues() {
        System.clearProperty(FLAG_PROPERTY);
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "true");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "false");
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY, true));
    }

    // parseSize and getSize are covered by JvmParseSizeTest

    @Test
    void getPropertyFallsBackToDefault() {
        System.clearProperty("chronicle.test.prop");
        assertEquals("fallback", Jvm.getProperty("chronicle.test.prop", "fallback"));

        System.setProperty("chronicle.test.prop", "value");
        assertEquals("value", Jvm.getProperty("chronicle.test.prop", "fallback"));
    }

    @Test
    void getLongReturnsParsedValueOrDefault() {
        System.clearProperty("chronicle.test.long");
        assertEquals(Long.valueOf(42L), Jvm.getLong("chronicle.test.long", 42L));

        System.setProperty("chronicle.test.long", "1234");
        assertEquals(Long.valueOf(1234L), Jvm.getLong("chronicle.test.long", 0L));
    }

    @Test
    void resourceTracingToggles() {
        boolean original = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(!original);
            assertEquals(!original, Jvm.isResourceTracing());
        } finally {
            Jvm.setResourceTracing(original);
        }
    }

    private int detectMajorVersionFromSpecification() {
        String specVersion = System.getProperty("java.specification.version", "8");
        int dotIndex = specVersion.indexOf('.');
        if (dotIndex >= 0) {
            return Integer.parseInt(specVersion.substring(dotIndex + 1));
        }
        return Integer.parseInt(specVersion);
    }
}
