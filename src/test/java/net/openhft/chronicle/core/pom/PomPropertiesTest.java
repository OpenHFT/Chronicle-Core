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

    @DisplayName("testCreateWithValidArguments behaviour under expected input and output conditions")
    @Test
    void testCreateWithValidArguments() {
        assertEquals("{}",
                PomProperties.create("net.openhft", "chronicle-queue").toString(), "PomProperties should return empty map string representation when created with valid groupId and artifactId");
    }

    @DisplayName("testCreateWithNullGroupId behaviour under expected input and output conditions")
    @Test
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

    @DisplayName("testCreateWithNullArtifactId behaviour under expected input and output conditions")
    @Test
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
