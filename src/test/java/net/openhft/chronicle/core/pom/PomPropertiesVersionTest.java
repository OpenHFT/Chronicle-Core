/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomPropertiesVersionTest {

    @DisplayName("versionUnknownWhenResourceMissing behaviour under expected input and output conditions")
    @Test
    void versionUnknownWhenResourceMissing() {
        String v = PomProperties.version("net.openhft", "non-existent-artifact");
        assertEquals("unknown", v, "version string should match");
    }
}

