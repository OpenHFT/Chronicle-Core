/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
class PomPropertiesTest {

    @Test
    @DisplayName("Create with valid arguments pom properties")
    void testCreateWithValidArguments() {
        assertEquals("{}",
                PomProperties.create("net.openhft", "chronicle-queue").toString(), "PomProperties should return empty map string representation when created with valid groupId and artifactId");
    }

    @Test
    @DisplayName("Create with null group id pom")
    void testCreateWithNullGroupId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create(null, "chronicle-queue");
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException("groupId is null");
                    }
                },
                "create should throw when groupId is null");
    }

    @Test
    @DisplayName("Create with null artifact id pom")
    void testCreateWithNullArtifactId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create("net.openhft", null);
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException("artifactId is null");
                    }
                },
                "create should throw when artifactId is null");
    }
}
