/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class InternalPomPropertiesMatrixTest {

    @Test
    void createReturnsAllStandardFields() {
        Properties properties = InternalPomProperties.create("test.group", "test-artifact");

        assertEquals("1.2.3", properties.getProperty("version"));
        assertEquals("test.group", properties.getProperty("groupId"));
        assertEquals("test-artifact", properties.getProperty("artifactId"));
    }

    @Test
    void createRejectsNullCoordinates() {
        assertThrows(NullPointerException.class, () -> InternalPomProperties.create(null, "artifact"));
        assertThrows(NullPointerException.class, () -> InternalPomProperties.create("group", null));
    }
}
