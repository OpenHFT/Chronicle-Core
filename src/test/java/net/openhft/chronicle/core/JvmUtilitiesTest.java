/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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

    @DisplayName("majorVersionMatchesSpecification behaviour under expected input and output conditions")
    @Test
    void majorVersionMatchesSpecification() {
        assertEquals(detectMajorVersionFromSpecification(), Jvm.majorVersion(), "JVM major version should match specification version");
    }

    @DisplayName("getBooleanUnderstandsEmptyAndFalseValues behaviour under expected input and output conditions")
    @Test
    void getBooleanUnderstandsEmptyAndFalseValues() {
        System.clearProperty(FLAG_PROPERTY);
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY), "getBoolean should return false when property is not set");

        System.setProperty(FLAG_PROPERTY, "");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY), "getBoolean should return true when property is set to empty string");

        System.setProperty(FLAG_PROPERTY, "true");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY), "getBoolean should return true when property is set to 'true'");

        System.setProperty(FLAG_PROPERTY, "false");
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY, true), "getBoolean should return false when property is set to 'false' despite default being true");
    }

    // parseSize and getSize are covered by JvmParseSizeTest

    @DisplayName("getPropertyFallsBackToDefault behaviour under expected input and output conditions")
    @Test
    void getPropertyFallsBackToDefault() {
        System.clearProperty("chronicle.test.prop");
        assertEquals("fallback", Jvm.getProperty("chronicle.test.prop", "fallback"), "getProperty should return default value when property is not set");

        System.setProperty("chronicle.test.prop", "value");
        assertEquals("value", Jvm.getProperty("chronicle.test.prop", "fallback"), "getProperty should return actual value when property is set");
    }

    @DisplayName("getLongReturnsParsedValueOrDefault behaviour under expected input and output conditions")
    @Test
    void getLongReturnsParsedValueOrDefault() {
        System.clearProperty("chronicle.test.long");
        assertEquals(42L, Jvm.getLong("chronicle.test.long", 42L), "getLong should return default value when property is not set");

        System.setProperty("chronicle.test.long", "1234");
        assertEquals(1234L, Jvm.getLong("chronicle.test.long", 0L), "getLong should return parsed value when property is set");
    }

    @DisplayName("resourceTracingToggles behaviour under expected input and output conditions")
    @Test
    void resourceTracingToggles() {
        boolean original = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(!original);
            assertEquals(!original, Jvm.isResourceTracing(), "resource tracing state should match the set value");
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
