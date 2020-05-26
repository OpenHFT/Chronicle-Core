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

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * <p>
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 * </p><p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 * </p>
 *
 * @param <I> the type of the input to the function
 * @param <T> the type of Throwable thrown
 */
@FunctionalInterface
public interface ThrowingConsumer<I, T extends Throwable> {
    static <I, T extends Throwable> Consumer<I> asConsumer(@NotNull ThrowingConsumer<I, T> function) {
        return in -> {
            try {
                function.accept(in);
            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param in the input argument
     */
    void accept(I in) throws T;
}
