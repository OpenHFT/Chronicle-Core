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
 * Represents an operation that accepts a single input argument and two additional context arguments,
 * and can throw a checked exception. This is a specialized version of a consumer that is capable of
 * throwing exceptions, allowing for lambda expressions and method references that might throw checked exceptions.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 * @param <U> the type of the third argument (context) used in the function
 */

public interface ThrowingConsumerNonCapturing<I, T extends Throwable, U> {

    /**
     * Performs this operation on the given argument and additional context arguments.
     *
     * @param in     the input argument
     * @param sb     the {@link CharSequence} used in the function
     * @param toBytes the third argument providing additional context, such as a transformation or configuration object
     * @throws T if the operation throws a checked exception
     */
    void accept(I in, CharSequence sb, U toBytes) throws T;
}
