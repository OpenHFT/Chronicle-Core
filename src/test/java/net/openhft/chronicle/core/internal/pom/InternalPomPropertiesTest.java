package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalPomPropertiesTest {

    @Test
    void versionShouldReturnUnknownForMissingArtifact() {
        String version = InternalPomProperties.version("non.existent", "artifact");
        assertEquals("unknown", version);
    }
}
