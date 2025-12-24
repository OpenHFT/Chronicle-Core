/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.announcer;

import net.openhft.chronicle.core.announcer.Announcer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InternalAnnouncerTest {

    @BeforeAll
    static void disableOutput() {
        // Ensure the class initialises with announcement disabled
        System.setProperty("chronicle.announcer.disable", "true");
    }

    @Test
    void announceEmptyPropertiesAnnouncesOncePerArtifact() {
        assertDoesNotThrow(() -> {
            Announcer.announce("net.openhft", "chronicle-core");
            Announcer.announce("net.openhft", "chronicle-core");
        }, "announce should not throw for empty properties");
    }

    @Test
    void announceWithLogoOnly() {
        Map<String, String> props = new HashMap<>();
        props.put(Announcer.LOGO, "ASCII-LOGO");
        assertDoesNotThrow(() -> {
            Announcer.announce("net.openhft", "chronicle-map", props);
            Announcer.announce("net.openhft", "chronicle-map", props);
        }, "announce should not throw with logo only");
    }

    @Test
    void announceWithAdditionalProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(Announcer.LOGO, "ASCII-LOGO");
        props.put("build", "test");
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue", props),
                "announce should not throw with extra properties");
    }
}
