package net.openhft.chronicle.core.internal.pom;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class InternalPomProperties {

    // Suppresses default constructor, ensuring non-instantiability.
    private InternalPomProperties() {
    }

    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<>();

    @NotNull
    public static Properties create(@NotNull final String groupId, @NotNull final String artifactId) {
        final Properties properties = new Properties();
        try {
            final String resourceName = resourceName(groupId, artifactId);
            try (InputStream inputStream = InternalPomProperties.class.getResourceAsStream(resourceName)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
            // Returns an empty set of properties if we fail.
        }
        return properties;
    }

    public static String version(@NotNull final String groupId, @NotNull final String artifactId) {
        return VERSION_CACHE.computeIfAbsent(groupId + ":" + artifactId, unused -> InternalPomProperties.extractVersionOrUnknown(groupId, artifactId));
    }

    private static String resourceName(@NotNull final String groupId, @NotNull final String artifactId) {
         return "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
    }

    private static String extractVersionOrUnknown(@NotNull final String groupId, @NotNull final String artifactId) {
        return create(groupId, artifactId).getProperty("version", "unknown");
    }
}