package net.openhft.chronicle.core.util;

import java.util.function.Supplier;

/**
 * A Builder is a configurable Supplier which always creates a new object.
 *
 * @param <T> Of object created
 */
public interface Builder<T> extends Supplier<T> {
    public T build();

    @Override
    default T get() {
        return build();
    }
}
