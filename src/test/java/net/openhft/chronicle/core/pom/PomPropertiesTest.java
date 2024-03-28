package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PomPropertiesTest {

    @Test
    void testCreateWithValidArguments() {
        Properties properties = PomProperties.create("net.openhft", "chronicle-queue");
        assertNotNull(properties);
    }

    @Test
    void testCreateWithNullGroupId() {
        assertThrows(NullPointerException.class, () -> PomProperties.create(null, "chronicle-queue"));
    }

    @Test
    void testCreateWithNullArtifactId() {
        assertThrows(NullPointerException.class, () -> PomProperties.create("net.openhft", null));
    }
}
