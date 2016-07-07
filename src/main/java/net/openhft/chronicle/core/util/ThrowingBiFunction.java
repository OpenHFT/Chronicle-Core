/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link ThrowingFunction}.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <I> the type of the first argument to the function
 * @param <J> the type of the second argument to the function
 * @param <T> the type of Throwable thrown
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingBiFunction<I, J, R, T extends Throwable> {
    static <I, J, T extends Throwable, R> BiFunction<I, J, R> asBiFunction(ThrowingBiFunction<I, J, R, T> function) {
        return (in, i2) -> {
            try {
                return function.apply(in, i2);
            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @return the function result
     * @throws T on an error.
     */
    R apply(I in, J i2) throws T;
}
