/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StandardMapsKeysTest {

    @Test
    void standardUserPropertiesContainExpectedKeys() {
        Map<String, String> m = AnalyticsFacade.standardUserProperties();
        assertFalse(m.isEmpty(), "standardUserPropertiesContainExpectedKeys: L18");
        assertTrue(m.containsKey("java_runtime_name"), "standardUserPropertiesContainExpectedKeys: L19");
        assertTrue(m.containsKey("os_name"), "standardUserPropertiesContainExpectedKeys: L20");
        assertTrue(m.containsKey("java_major_version"), "standardUserPropertiesContainExpectedKeys: L21");
        assertTrue(m.containsKey("available_processors"), "standardUserPropertiesContainExpectedKeys: L22");
        // values are non-empty strings
        for (String v : m.values()) assertNotNull(v, "standardUserPropertiesContainExpectedKeys: L24");
    }

    @Test
    void additionalPropertiesMayBeEmptyButAreNonNull() {
        Map<String, String> m = AnalyticsFacade.standardAdditionalProperties();
        assertNotNull(m, "additionalPropertiesMayBeEmptyButAreNonNull: L30");
    }
}
