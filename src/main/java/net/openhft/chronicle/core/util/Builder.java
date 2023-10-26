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

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A Builder of type T is a configurable object that can provide
 * another non-null T instance.
 * <p>
 * Extends {@link Supplier} to make this useable as a {@link Supplier}.
 *
 * @param <T> of object provided
 */
@FunctionalInterface
public interface Builder<T> extends Supplier<T> {

    /**
     * Builds and returns a non-null T instance.
     * <p>
     * The builder should always creates a new instance if the
     * instance is mutable. If the instance is immutable,
     * the builder may create a new instance, or it may return
     * a previously existing instance at its own discretion.
     * <p>
     * As opposed to a factory, a Builder is often only able to be invoked at most one time.
     *
     * @return a non-null instance of type T
     * @throws IllegalStateException if the builder is can only be invoked once
     *                               and this method is invoked more than once.
     */
    @NotNull
    T build();

    @Override
    default T get() {
        return build();
    }
}