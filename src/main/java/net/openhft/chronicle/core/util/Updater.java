package net.openhft.chronicle.core.util;

import java.util.function.Consumer;

/**
 * This is similar to a Consumer but it is expected to alter the object whereas a Consumer is not expected to.
 */
@FunctionalInterface
public interface Updater<T> extends Consumer<T> {
    @Override
    default void accept(T t) {
        update(t);
    }

    void update(T t);
}
