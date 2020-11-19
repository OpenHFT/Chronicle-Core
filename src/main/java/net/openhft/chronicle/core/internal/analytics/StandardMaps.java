package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum StandardMaps {;

    private static final long GIB = 1024L * 1024L;

    public static Map<String, String> standardEventParameters(@NotNull final String appVersion) {
        return Stream.of(
                entry("app_version", appVersion)
        )
                .filter(e -> e.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    public static Map<String, String> standardUserProperties() {
        return Stream.of(
                entryFor("java.runtime.name"),
                entryFor("java.runtime.version"),
                entryFor("os.name"),
                entryFor("os.arch"),
                entryFor("os.version"),
                entry(replaceDotsWithUnderscore("timezone.default"), TimeZone.getDefault().getID()),
                entry(replaceDotsWithUnderscore("available.processors"), Integer.toString(Runtime.getRuntime().availableProcessors())), // Must be strings...
                entry(replaceDotsWithUnderscore("max.memory.gib"), Long.toString(Runtime.getRuntime().maxMemory() / GIB)),
                entry(replaceDotsWithUnderscore("java.major.version"), Long.toString(Jvm.majorVersion())),
                entry(replaceDotsWithUnderscore("max.direct.memory.gib"), Long.toString(Jvm.maxDirectMemory() / GIB))
        )
                .filter(e -> e.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    private static Map.Entry<String, String> entryFor(@NotNull final String systemProperty) {
        return new AbstractMap.SimpleImmutableEntry<>(replaceDotsWithUnderscore(systemProperty), System.getProperty(systemProperty));
    }

    private static Map.Entry<String, String> entry(@NotNull final String key, @Nullable final String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static String replaceDotsWithUnderscore(@NotNull final String s) {
        return s.replace('.', '_');
    }

}