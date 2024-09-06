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

package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * Utility class for creating and manipulating {@link Map} entries and unmodifiable maps.
 * <p>
 * This class provides utility methods for creating immutable map entries and building unmodifiable maps from entries.
 * It uses Java's functional stream API to collect and convert the entries into an immutable map.
 * </p>
 * <p>
 * This class is not intended to be instantiated and provides static utility methods.
 * </p>
 */
public final class MapUtil {

    // Private constructor to prevent instantiation
    private MapUtil() {
    }

    /**
     * Creates a {@link Map.Entry} with the specified key and value.
     * <p>
     * The returned entry is immutable, meaning its key and value cannot be changed once created.
     * Both the key and value must be non-null, and this is enforced with {@link ObjectUtils#requireNonNull(Object)}.
     * </p>
     *
     * @param key   The key for the entry, must be non-null.
     * @param value The value for the entry, must be non-null.
     * @param <K>   The type of keys maintained by the map.
     * @param <V>   The type of mapped values.
     * @return A {@link Map.Entry} representing the key-value pair.
     * @throws NullPointerException If the key or value is null.
     */
    public static <K, V> Map.Entry<K, V> entry(@NotNull final K key, @NotNull final V value) {
        requireNonNull(key);
        requireNonNull(value);
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Creates an unmodifiable map from the provided entries.
     * <p>
     * The entries must be non-null, and their keys and values are validated to be non-null as well. The map created
     * is immutable, meaning that attempts to modify it will result in an {@link UnsupportedOperationException}.
     * </p>
     * <p>
     * This method uses varargs to accept an arbitrary number of {@link Map.Entry} objects.
     * </p>
     *
     * @param entries The entries to be added to the map, must be non-null.
     * @param <K>     The type of keys maintained by the map.
     * @param <V>     The type of mapped values.
     * @return An unmodifiable map containing the specified entries.
     * @throws NullPointerException If any entry, key, or value is null.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K, V> Map<K, V> ofUnmodifiable(final Map.Entry<K, V>... entries) {
        requireNonNull(entries);
        return Stream.of(entries)
                .map(ObjectUtils::requireNonNull)
                .collect(collectingAndThen(toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
    }

}
