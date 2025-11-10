//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        assertFalse(m.isEmpty());
        assertTrue(m.containsKey("java_runtime_name"));
        assertTrue(m.containsKey("os_name"));
        assertTrue(m.containsKey("java_major_version"));
        assertTrue(m.containsKey("available_processors"));
        // values are non-empty strings
        for (String v : m.values()) assertNotNull(v);
    }

    @Test
    void additionalPropertiesMayBeEmptyButAreNonNull() {
        Map<String, String> m = AnalyticsFacade.standardAdditionalProperties();
        assertNotNull(m);
    }
}
