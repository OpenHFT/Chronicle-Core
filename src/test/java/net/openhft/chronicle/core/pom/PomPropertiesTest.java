package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.Test;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class PomPropertiesTest {

    @Test
    void testCreateWithValidArguments() {
        assertEquals("{}",
                PomProperties.create("net.openhft", "chronicle-queue").toString());
    }

    @Test
    void testCreateWithNullGroupId() {
        assertEquals("{}",
                PomProperties.create(null, "chronicle-queue").toString());
    }

    @Test
    void testCreateWithNullArtifactId() {
        assertEquals("{}",
                PomProperties.create("net.openhft", null).toString());
    }
}
