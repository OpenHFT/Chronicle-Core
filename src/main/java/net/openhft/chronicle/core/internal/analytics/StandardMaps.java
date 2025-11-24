/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
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
import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Helpers for building standard analytics maps (event parameters and user properties).
 * <p>
 * Encapsulates whitelisting and blacklisting of package names and normalises JVM and OS
 * properties into ordered {@link LinkedHashMap} instances.
 */
public final class StandardMaps {

    // Suppresses default constructor, ensuring non-instantiability.
    private StandardMaps() {
    }

    private static final long GIB = 1L << 30;

    // Any package containing these sub-strings will be used
    private static final Set<String> WHITE_LIST_CONTAINS = Stream.of(
            ".demo",
            "run.chronicle"
    ).collect(toSet());

    // Exact match of package names. O(1) lookup performance.
    private static final Set<String> BLACK_LIST_EXACT = Stream.of(
            "java.lang",
            "sun.reflect",
            "java.lang.reflect",
            "org.apache.maven"
    ).collect(toSet());

    // This is more expensive because we are using startsWith() with
    // these package names.
    private static final Set<String> BLACK_LIST_STARTS_WITH = Stream.concat(
            BLACK_LIST_EXACT.stream(),
            Stream.of(
                    "software.chronicle", // Enterprise packages
                    "org.junit",
                    "net.openhft",
                    "java",
                    "jdk",
                    "sun"
                    // Add third party libs
            )).collect(toSet());

    /**
     * Build a minimal event parameter map containing the application version.
     */
    public static Map<String, String> standardEventParameters(@NotNull final String appVersion) {
        requireNonNull(appVersion);
        return Stream.of(
                        entry("app_version", appVersion)
                )
                .filter(e -> e.getValue() != null)
                .collect(toOrderedMap());
    }

    /**
     * Convenience overload that inspects the current thread stack for additional parameters.
     */
    public static Map<String, String> standardAdditionalEventParameters() {
        return standardAdditionalEventParameters(Thread.currentThread().getStackTrace());
    }

    /**
     * Collect JVM and OS details for use as user properties.
     */
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

    /**
     * Extract up to three distinct package names from the supplied stack to help identify call sites.
     */
    static Map<String, String> standardAdditionalEventParameters(@NotNull final StackTraceElement[] stackTraceElements) {
        requireNonNull(stackTraceElements);
        final AtomicInteger cnt = new AtomicInteger();
        final Set<String> distinctKeys = new HashSet<>();
        return Stream.of(stackTraceElements)
                .map(StackTraceElement::getClassName)
                .map(StandardMaps::packageName)
                .filter(StandardMaps::shouldBeSent)
                .filter(pn -> StandardMaps.distinctUpToMaxLevel3(pn, distinctKeys))
                .limit(3)
                .collect(toOrderedMap(s -> "package_name_" + cnt.getAndIncrement(), Function.identity()));
    }

    /**
     * Track whether a package name (trimmed to at most three levels) has been seen before.
     */
    static boolean distinctUpToMaxLevel3(final String name, final Set<String> set) {
        final String packageNameUpToMaxLevel3 = packageNameUpToMaxLevel3(name);
        return set.add(packageNameUpToMaxLevel3);
    }

    /**
     * Return the package portion of a fully qualified class name, or the name itself if none.
     */
    static String packageName(final String className) {
        final int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex>0)
            return className.substring(0, lastDotIndex);
        else
            return className;
    }

    /**
     * Trim a fully qualified class name to a package containing at most three levels.
     */
    static String packageNameUpToMaxLevel3(final String className) {
        if (className.isEmpty())
            return className;
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
            return className;
        }
        return className.substring(0, lastDotIndex);
    }

    static boolean shouldBeSent(final String packageName) {
        return WHITE_LIST_CONTAINS.stream().anyMatch(packageName::contains)
                || (!BLACK_LIST_EXACT.contains(packageName)
                && BLACK_LIST_STARTS_WITH.stream().noneMatch(packageName::startsWith));
    }

    /**
     * Collector that preserves encounter order when constructing a map from entries.
     */
    @NotNull
    private static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toOrderedMap() {
        return toOrderedMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * Collector that preserves encounter order using the provided key/value mappers.
     */
    @NotNull
    private static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toOrderedMap(@NotNull final Function<? super T, ? extends K> keyMapper, @NotNull final Function<? super T, ? extends V> valueMapper) {
        return toMap(keyMapper, valueMapper, (a, b) -> b, LinkedHashMap::new);
    }

    private static Map.Entry<String, String> entryFor(@NotNull final String systemProperty) {
        return new AbstractMap.SimpleImmutableEntry<>(replaceDotsWithUnderscore(systemProperty), Jvm.getProperty(systemProperty));
    }

    /**
     * Simple key/value entry factory.
     */
    private static Map.Entry<String, String> entry(@NotNull final String key, @Nullable final String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Utility that normalises dot-separated property names to underscore-separated analytics keys.
     */
    private static String replaceDotsWithUnderscore(@NotNull final String s) {
        return s.replace('.', '_');
    }
}
