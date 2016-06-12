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

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 * <p/>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingFunction<I, R, T extends Throwable> {
    static <I, R, T extends Throwable> Function<I, R> asFunction(ThrowingFunction<I, R, T> function) {
        return in -> {
            try {
                return function.apply(in);

            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Applies this function to the given argument.
     *
     * @param in the function argument
     * @return the function result
     */
    R apply(I in) throws T;
}
