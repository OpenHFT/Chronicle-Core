package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.Analytics;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum AnalyticsUtil {;

    private static final boolean DISABLE_ANALYTICS = Optional.ofNullable(System.getProperty("chronicle.analytics.disable")).filter("true"::equals).isPresent();
    // Protect against too many entries in the cache.
    private static final int MAX_LIBRARIES = 1_000;

    private static final Map<String, Map<String, Analytics>> INSTANCES = new ConcurrentHashMap<>();

    @NotNull
    public static Analytics acquire(@NotNull final String libraryName, @NotNull final String libraryVersion) {
        if (libraryVersion.endsWith("SNAPSHOT") || DISABLE_ANALYTICS || INSTANCES.size() >= MAX_LIBRARIES) {
            return MuteAnalytics.INSTANCE;
        }
        return INSTANCES
                .computeIfAbsent(libraryName, unused -> new ConcurrentHashMap<>())
                .computeIfAbsent(libraryVersion, v -> new VanillaAnalytics(libraryName, v));

    }

}