/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternalPomPropertiesCorruptTest {

    @DisplayName("Version unknown when version missing in resource")
    @Test
    void versionUnknownWhenVersionMissingInResource() {
        String v = InternalPomProperties.version("test.group", "corrupt-artifact");
        assertEquals("unknown", v, "version string should match");
    }
}

