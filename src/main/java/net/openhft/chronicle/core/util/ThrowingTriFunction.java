/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface similar to {@link java.util.function.BiFunction}, but it accepts three arguments and
 * can throw a checked exception. This interface is useful for lambda expressions and method references
 * that operate on three input arguments and can throw exceptions.
 *
 * @param <I> the type of the first input argument
 * @param <J> the type of the second input argument
 * @param <A> the type of the third input argument
 * @param <R> the type of the result of the function
 * @param <T> the type of throwable that may be thrown by the function
 */
@FunctionalInterface
public interface ThrowingTriFunction<I, J, A, R, T extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @param i3 the third function argument
     * @return the function result
     * @throws T if an error occurs during function execution
     */
    @NotNull
    R apply(I in, J i2, A i3) throws T;
}
