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

/**
 * Represents a function that accepts a single {@code char} value argument and produces a
 * {@code boolean} result. This is the {@code char}-consuming primitive type specialization of
 * {@code Function}.
 *
 * <p>This is a functional interface whose functional method is {@link #applyAsChar(char)}.
 *
 * @see java.util.function.Function
 */
@FunctionalInterface
public interface CharToBooleanFunction {

    /**
     * Applies this function to the given {@code char} value.
     *
     * @param value the {@code char} value to be processed by the function.
     * @return the {@code boolean} result of the function.
     */
    boolean applyAsChar(char value);
}
