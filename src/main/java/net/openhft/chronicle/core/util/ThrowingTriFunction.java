/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

@FunctionalInterface
public interface ThrowingTriFunction<I, J, A, R, T extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param in the first function argument
     * @param i2 the second function argument
     * @return the function result
     * @throws T on an error.
     */
    @NotNull
    R apply(I in, J i2, A i3) throws T;
}
