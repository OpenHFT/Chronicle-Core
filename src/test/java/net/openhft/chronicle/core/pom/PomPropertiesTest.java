/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
class PomPropertiesTest {

    @Test
    void testCreateWithValidArguments() {
        assertEquals("{}",
                PomProperties.create("net.openhft", "chronicle-queue").toString(), "PomProperties should return empty map string representation when created with valid groupId and artifactId");
    }

    @Test
    void testCreateWithNullGroupId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create(null, "chronicle-queue");
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException("Null groupId should map to NPE");
                    }
                }, "create should reject null groupId");
    }

    @Test
    void testCreateWithNullArtifactId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create("net.openhft", null);
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException("Null artifactId should map to NPE");
                    }
                }, "create should reject null artifactId");
    }
}
