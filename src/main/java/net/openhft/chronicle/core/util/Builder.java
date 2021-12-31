package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

/**
 * A Builder of type T is a configurable object that can provide
 * another non-null T instance.
 *
 * @param <T> of object provided
 */
@FunctionalInterface
public interface Builder<T> {

    /**
     * Builds and returns a non-null T instance.
     * <p>
     * The builder always creates a new instance if the
     * instance is mutable. If the instance is immutable,
     * the builder may create a new instance or it may return
     * a previously existing instance at its own discretion.
     * <p>
     * As opposed to a factory, a Builder is often unipotent
     * meaning that this method can be invoked at most one time.
     *
     * @return a non-null instance of type T
     *
     * @throws IllegalStateException if the builder is unipotent
     * and this method is invoked more than once.
     */
    @NotNull
    T build();

}