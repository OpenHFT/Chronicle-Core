/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import org.jetbrains.annotations.NotNull;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

/**
 * Small helpers for building immutable maps with predictable iteration order.
 * <p>
 * Provides {@link #entry(Object, Object)} and {@link #ofUnmodifiable(Map.Entry[])} to create
 * unmodifiable maps in a concise and type safe way.
 */
public final class MapUtil {

    private MapUtil() {
    }

    public static <K, V> Map.Entry<K, V> entry(@NotNull final K key, @NotNull final V value) {
        requireNonNull(key);
        requireNonNull(value);
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K, V> Map<K, V> ofUnmodifiable(final Map.Entry<K, V>... entries) {
        requireNonNull(entries);
        return Stream.of(entries)
                .map(Objects::requireNonNull)
                .collect(collectingAndThen(toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
    }
}
