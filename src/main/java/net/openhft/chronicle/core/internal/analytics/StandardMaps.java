/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Utility class for creating standard parameter maps used in analytics and logging events.
 * This class provides methods to generate standard event parameters, user properties,
 * and additional event parameters based on the current runtime environment and stack trace.
 *
 * <p>The class collects system properties, memory usage details, and package information,
 * and ensures that only relevant data is included in analytics events based on predefined
 * white and blacklists of package names.</p>
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
     * Creates a map of standard event parameters for analytics events.
     *
     * @param appVersion The version of the application being tracked. Must not be null.
     * @return A map containing standard event parameters, such as the application version.
     * @throws NullPointerException If {@code appVersion} is null.
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
     * Generates a map of additional event parameters based on the current thread's stack trace.
     *
     * @return A map containing package names extracted from the stack trace as additional event parameters.
     */
    public static Map<String, String> standardAdditionalEventParameters() {
        return standardAdditionalEventParameters(Thread.currentThread().getStackTrace());
    }

    /**
     * Generates a map of standard user properties for analytics based on system properties
     * and runtime information, such as the Java version, operating system, and available memory.
     *
     * @return A map containing user properties for analytics, such as the runtime environment and system specs.
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
     * Helper method to create a map of additional event parameters based on a provided stack trace.
     *
     * @param stackTraceElements The stack trace to extract package names from. Must not be null.
     * @return A map containing package names from the stack trace as event parameters.
     * @throws NullPointerException If {@code stackTraceElements} is null.
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
     * Ensures that package names are distinct up to the third level, to reduce redundancy
     * in event parameter package names.
     *
     * @param name The package name to check.
     * @param set  A set of package names already included in the parameters.
     * @return True if the package name is distinct, false otherwise.
     */
    static boolean distinctUpToMaxLevel3(final String name, final Set<String> set) {
        final String packageNameUpToMaxLevel3 = packageNameUpToMaxLevel3(name);
        return set.add(packageNameUpToMaxLevel3);
    }

    /**
     * Extracts the package name from a fully qualified class name.
     *
     * @param className The fully qualified class name.
     * @return The package name.
     */
    static String packageName(final String className) {
        final int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0)
            return className.substring(0, lastDotIndex);
        else
            return className;
    }

    /**
     * Extracts the package name up to the third level (e.g., "com.example.foo").
     *
     * @param className The fully qualified class name.
     * @return The package name up to the third level.
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

    /**
     * Determines if a package name should be included in event parameters based on white
     * and blacklists.
     *
     * @param packageName The package name to check.
     * @return True if the package name should be included, false otherwise.
     */
    static boolean shouldBeSent(final String packageName) {
        return WHITE_LIST_CONTAINS.stream().anyMatch(packageName::contains)
                || (!BLACK_LIST_EXACT.contains(packageName)
                && BLACK_LIST_STARTS_WITH.stream().noneMatch(packageName::startsWith));
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
        return new AbstractMap.SimpleImmutableEntry<>(replaceDotsWithUnderscore(systemProperty), Jvm.getProperty(systemProperty));
    }

    private static Map.Entry<String, String> entry(@NotNull final String key, @Nullable final String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static String replaceDotsWithUnderscore(@NotNull final String s) {
        return s.replace('.', '_');
    }
}
