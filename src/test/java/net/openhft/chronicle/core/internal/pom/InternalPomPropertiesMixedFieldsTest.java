/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternalPomPropertiesMixedFieldsTest {

    @DisplayName("versionResolvesWhenOnlyVersionIsPresent behaviour under expected input and output conditions")
    @Test
    void versionResolvesWhenOnlyVersionIsPresent() {
        String v = InternalPomProperties.version("test.group", "only-version");
        assertEquals("9.9.9", v, "version string should match");
    }
}

