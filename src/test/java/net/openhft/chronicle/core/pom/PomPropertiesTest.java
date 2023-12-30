package net.openhft.chronicle.core.pom;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class PomPropertiesTest {

    @Test
    void testCreateWithValidArguments() {
        Properties properties = PomProperties.create("net.openhft", "chronicle-queue");
        assertNotNull(properties);
    }

    @Test
    void testCreateWithNullGroupId() {
        PomProperties.create(null, "chronicle-queue");
    }

    @Test
    void testCreateWithNullArtifactId() {
        PomProperties.create("net.openhft", null);
    }
}
