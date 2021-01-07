package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A Builder is a configurable Supplier which always creates a new object.
 *
 * @param <T> Of object created
 */
public interface Builder<T> extends Supplier<T> {
    @NotNull
    T build();

    @Override
    @NotNull
    default T get() {
        return build();
    }
}
