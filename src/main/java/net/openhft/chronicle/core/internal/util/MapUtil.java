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
                .map(ObjectUtils::requireNonNull)
                .collect(collectingAndThen(toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
    }

}
