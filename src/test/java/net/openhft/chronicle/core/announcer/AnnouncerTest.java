/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.announcer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnouncerTest {

    @Test
    void testAnnounceWithValidArguments() {
        // This is a simple test to ensure no exceptions are thrown with valid arguments
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue"),
                "announce should not throw with valid arguments");
    }

    @Test
    void testAnnounceWithNullGroupId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce(null, "chronicle-queue");
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException("Null groupId should map to NPE");
            }
        }, "announce should reject null group id");
    }

    @Test
    void testAnnounceWithNullArtifactId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce("net.openhft", null);
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException("Null artifactId should map to NPE");
            }
        }, "announce should reject null artifact id");
    }
}
