/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.announcer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnouncerTest {

    @Test
    @DisplayName("Announce accepts valid group and artefact values")
    void testAnnounceWithValidArguments() {
        // This is a simple test to ensure no exceptions are thrown with valid arguments
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue"),
                "announce should not throw for valid group and artefact values");
    }

    @Test
    @DisplayName("Announce with null group id announcer")
    void testAnnounceWithNullGroupId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce(null, "chronicle-queue");
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException("groupId is null");
            }
        }, "announce should throw when groupId is null");
    }

    @Test
    @DisplayName("Announce with null artifact id announcer")
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
