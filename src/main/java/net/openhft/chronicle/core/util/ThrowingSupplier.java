/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * <p>
 * Represents a supplier of results which might throw an Exception
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 * 
 *
 * @param <V> the type of results supplied by this supplier
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<V, T extends Throwable> {

    static <V, T extends Throwable> Supplier<V> asSupplier(@NotNull ThrowingSupplier<V, T> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();

            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Gets a result.
     *
     * @throws InvalidMarshallableException if the object created is not valid
     * @return a result
     */
    @NotNull
    V get() throws T, InvalidMarshallableException;
}
