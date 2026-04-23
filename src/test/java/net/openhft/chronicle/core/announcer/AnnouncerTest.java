/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.announcer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncerTest {

    @Test
    void testAnnounceWithValidArguments() {
        // This is a simple test to ensure no exceptions are thrown with valid arguments
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue"));
    }

    @Test
    void testAnnounceWithNullGroupId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce(null, "chronicle-queue");
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException();
            }
        });
    }

    @Test
    void testAnnounceWithNullArtifactId() {
        assertThrows(NullPointerException.class, () -> {
            try {
                Announcer.announce("net.openhft", null);
            } catch (IllegalArgumentException iae) {
                throw new NullPointerException();
            }
        });
    }
}
