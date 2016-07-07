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
import net.openhft.chronicle.core.io.IORuntimeException;

import java.util.function.BiConsumer;

/**
 * Represents an operation that accepts two input arguments and returns no
 * result. Unlike most other functional interfaces, {@code ThrowingBiConsumer} is expected
 * to operate via side-effects.
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object)}.
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowingBiConsumer<I, J, T extends Throwable> {
    static <I, J, T extends Throwable> BiConsumer<I, J> asConsumer(ThrowingBiConsumer<I, J, T> function) {
        return (in, i2) -> {
            try {
                function.accept(in, i2);
            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Performs this operation on the given arguments.
     *
     * @param in the first input argument
     * @param i2 the second input argument
     */
    void accept(I in, J i2) throws T, IORuntimeException;
}
