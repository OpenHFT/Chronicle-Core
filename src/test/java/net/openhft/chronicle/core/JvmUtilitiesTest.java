package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JvmUtilitiesTest {

    private static final String SIZE_PROPERTY = "chronicle.core.test.size";
    private static final String FLAG_PROPERTY = "chronicle.core.test.flag";

    @AfterEach
    void clearTestProperties() {
        System.clearProperty(SIZE_PROPERTY);
        System.clearProperty(FLAG_PROPERTY);
    }

    @Test
    void majorVersionMatchesSpecification() {
        assertEquals(detectMajorVersionFromSpecification(), Jvm.majorVersion());
    }

    @Test
    void getBooleanUnderstandsEmptyAndFalseValues() {
        System.clearProperty(FLAG_PROPERTY);
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "true");
        assertTrue(Jvm.getBoolean(FLAG_PROPERTY));

        System.setProperty(FLAG_PROPERTY, "false");
        assertFalse(Jvm.getBoolean(FLAG_PROPERTY, true));
    }

    @Test
    void parseSizeSupportsBinarySuffixes() {
        assertEquals(1024L, Jvm.parseSize("1k"));
        assertEquals(1536L, Jvm.parseSize("1.5K"));
        assertEquals(1_048_576L, Jvm.parseSize("1MiB"));
        assertEquals(1L, Jvm.parseSize("1"));
    }

    @Test
    void parseSizeRejectsUnknownSuffix() {
        assertThrows(IllegalArgumentException.class, () -> Jvm.parseSize("10XB"));
    }

    @Test
    void getSizeReturnsParsedValueOrDefault() {
        System.clearProperty(SIZE_PROPERTY);
        assertEquals(512L, Jvm.getSize(SIZE_PROPERTY, 512L));

        System.setProperty(SIZE_PROPERTY, "2M");
        assertEquals(2 * (1L << 20), Jvm.getSize(SIZE_PROPERTY, 0L));

        System.setProperty(SIZE_PROPERTY, "notanumber");
        assertEquals(256L, Jvm.getSize(SIZE_PROPERTY, 256L));
    }

    @Test
    void getPropertyFallsBackToDefault() {
        System.clearProperty("chronicle.test.prop");
        assertEquals("fallback", Jvm.getProperty("chronicle.test.prop", "fallback"));

        System.setProperty("chronicle.test.prop", "value");
        assertEquals("value", Jvm.getProperty("chronicle.test.prop", "fallback"));
    }

    @Test
    void getLongReturnsParsedValueOrDefault() {
        System.clearProperty("chronicle.test.long");
        assertEquals(Long.valueOf(42L), Jvm.getLong("chronicle.test.long", 42L));

        System.setProperty("chronicle.test.long", "1234");
        assertEquals(Long.valueOf(1234L), Jvm.getLong("chronicle.test.long", 0L));
    }

    @Test
    void resourceTracingToggles() {
        boolean original = Jvm.isResourceTracing();
        try {
            Jvm.setResourceTracing(!original);
            assertEquals(!original, Jvm.isResourceTracing());
        } finally {
            Jvm.setResourceTracing(original);
        }
    }

    private int detectMajorVersionFromSpecification() {
        String specVersion = System.getProperty("java.specification.version", "8");
        int dotIndex = specVersion.indexOf('.');
        if (dotIndex >= 0) {
            return Integer.parseInt(specVersion.substring(dotIndex + 1));
        }
        return Integer.parseInt(specVersion);
    }
}
