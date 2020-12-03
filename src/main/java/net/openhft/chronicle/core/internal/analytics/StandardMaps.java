package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public enum StandardMaps {
    ;

    private static final long GIB = 1024L * 1024L;

    // Exact match of package names of length 1 to 3. O(1) lookup performance.
    private static final Set<String> KNOWN_PACKAGE_NAMES_OF_MAX_DEPTH_3 = Stream.of(
            "java.lang",
            "sun.reflect",
            "java.lang.reflect",
            "net.openhft.chronicle",
            "org.apache.maven"
    ).collect(toSet());

    // This is more expensive because we are using startsWith() with
    // these package names.
    private static final Set<String> OTHER_KNOWN_PACKAGE_NAMES = Stream.of(
            "software.chronicle", // Enterprise packages
            "org.junit",
            "net.openhft",
            "java",
            "jdk",
            "sun"
            // Add third party libs
    ).collect(toSet());


    public static Map<String, String> standardEventParameters(@NotNull final String appVersion) {
        return Stream.of(
                entry("app_version", appVersion)
        )
                .filter(e -> e.getValue() != null)
                .collect(toOrderedMap());
    }

    public static Map<String, String> standardAdditionalEventParameters() {
        return standardAdditionalEventParameters(Thread.currentThread().getStackTrace());
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
                .collect(toOrderedMap());
    }

    static Map<String, String> standardAdditionalEventParameters(final StackTraceElement[] stackTraceElements) {
        final AtomicInteger cnt = new AtomicInteger();
        return Stream.of(stackTraceElements)
                .map(StackTraceElement::getClassName)
                .map(StandardMaps::packageNameUpToMaxLevel3)
                .filter(StandardMaps::isUnknownPackageName)
                .distinct()
                .limit(3)
                .collect(toOrderedMap(s -> "package_name_" + cnt.getAndIncrement(), Function.identity()));
    }

    static String packageNameUpToMaxLevel3(final String className) {
        int noDots = 0;
        int i = 0;
        int lastDotIndex = 0;
        for (; i < className.length(); i++) {
            final char c = className.charAt(i);
            if ('.' == c) {
                lastDotIndex = i;
                if (++noDots >= 3)
                    break;
            }
        }
        if (lastDotIndex == 0) {
            // No package found
            return "";
        }
        return className.substring(0, lastDotIndex);
    }

    static boolean isUnknownPackageName(final String packageName) {
        return !KNOWN_PACKAGE_NAMES_OF_MAX_DEPTH_3.contains(packageName)
                && OTHER_KNOWN_PACKAGE_NAMES.stream().noneMatch(packageName::startsWith);
    }

    @NotNull
    private static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toOrderedMap() {
        return toOrderedMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @NotNull
    private static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toOrderedMap(@NotNull final Function<? super T, ? extends K> keyMapper, @NotNull final Function<? super T, ? extends V> valueMapper) {
        return toMap(keyMapper, valueMapper, (a, b) -> b, LinkedHashMap::new);
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