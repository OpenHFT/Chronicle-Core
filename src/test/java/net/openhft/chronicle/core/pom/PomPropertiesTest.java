/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PomPropertiesTest {

    @Test
    void testCreateWithValidArguments() {
        assertEquals("{}",
                PomProperties.create("net.openhft", "chronicle-queue").toString());
    }

    @Test
    void testCreateWithNullGroupId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create(null, "chronicle-queue").toString();
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException();
                    }
                });
    }

    @Test
    void testCreateWithNullArtifactId() {
        assertThrows(NullPointerException.class,
                () -> {
                    try {
                        PomProperties.create("net.openhft", null).toString();
                    } catch (IllegalArgumentException iae) {
                        throw new NullPointerException();
                    }
                });
    }
}
