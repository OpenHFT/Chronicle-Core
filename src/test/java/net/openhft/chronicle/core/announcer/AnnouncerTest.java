/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.announcer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnouncerTest {

    @DisplayName("testAnnounceWithValidArguments behaviour under expected input and output conditions")
    @Test
    void testAnnounceWithValidArguments() {
        // This is a simple test to ensure no exceptions are thrown with valid arguments
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue"),
                "announce should not throw for valid group and artefact values");
    }

    @DisplayName("testAnnounceWithNullGroupId behaviour under expected input and output conditions")
    @Test
    void testAnnounceWithNullGroupId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce(null, "chronicle-queue");
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException("groupId is null");
            }
        }, "announce should throw when groupId is null");
    }

    @DisplayName("testAnnounceWithNullArtifactId behaviour under expected input and output conditions")
    @Test
    void testAnnounceWithNullArtifactId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce("net.openhft", null);
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException("artifactId is null");
            }
        }, "announce should throw when artifactId is null");
    }
}
