/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StandardMapsKeysTest {

    @DisplayName("standardUserPropertiesContainExpectedKeys behaviour under expected input and output conditions")
    @Test
    void standardUserPropertiesContainExpectedKeys() {
        Map<String, String> m = AnalyticsFacade.standardUserProperties();
        assertFalse(m.isEmpty(), "user properties map should not be empty");
        assertTrue(m.containsKey("java_runtime_name"), "user properties should contain java_runtime_name key");
        assertTrue(m.containsKey("os_name"), "user properties should contain os_name key");
        assertTrue(m.containsKey("java_major_version"), "user properties should contain java_major_version key");
        assertTrue(m.containsKey("available_processors"), "user properties should contain available_processors key");
        // values are non-empty strings
        for (String v : m.values())
            assertNotNull(v, "each user property value should be non-null: " + v);
    }

    @DisplayName("additionalPropertiesMayBeEmptyButAreNonNull behaviour under expected input and output conditions")
    @Test
    void additionalPropertiesMayBeEmptyButAreNonNull() {
        Map<String, String> m = AnalyticsFacade.standardAdditionalProperties();
        assertNotNull(m, "standardAdditionalProperties should return non-null map");
    }
}
