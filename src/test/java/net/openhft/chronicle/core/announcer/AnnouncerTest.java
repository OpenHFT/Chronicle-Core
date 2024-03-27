package net.openhft.chronicle.core.announcer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnouncerTest {

    @Test
    void testAnnounceWithValidArguments() {
        // This is a simple test to ensure no exceptions are thrown with valid arguments
        assertDoesNotThrow(() -> Announcer.announce("net.openhft", "chronicle-queue"));
    }

    @Test
    void testAnnounceWithNullGroupId() {
        assertThrows(IllegalArgumentException.class, () -> Announcer.announce(null, "chronicle-queue"));
    }

    @Test
    void testAnnounceWithNullArtifactId() {
        assertThrows(IllegalArgumentException.class, () -> Announcer.announce("net.openhft", null));
    }
}
