package net.openhft.chronicle.core.internal.pom;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class InternalPomPropertiesUtil {

    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<>();

    @NotNull
    public static Properties create(@NotNull final String libraryName) {
        final Properties properties = new Properties();
        try {
            properties.load(InternalPomPropertiesUtil.class.getResourceAsStream("/" + libraryName + ".pom.properties"));
        } catch (Exception ignore) {
            // Returns an empty set of properties if we fail.
        }
        return properties;
    }

    public static String version(@NotNull final String libraryName) {
        return VERSION_CACHE.computeIfAbsent(libraryName, InternalPomPropertiesUtil::extractVersionOrUnknown);
    }

    private static String extractVersionOrUnknown(String libraryName) {
        return Optional.ofNullable(create(libraryName).getProperty("version"))
                .orElse("unknown");
    }

}