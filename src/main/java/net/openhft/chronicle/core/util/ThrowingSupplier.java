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
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a supplier of results that can throw a checked exception. This is a specialized
 * version of a {@link Supplier} that allows for checked exceptions, enabling lambda expressions
 * and method references to throw exceptions when used in contexts that expect a supplier.
 * <p>
 * There is no requirement that a new or distinct result be returned each time the supplier is invoked.
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/FunctionalInterface.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <V> the type of results supplied by this supplier
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<V, T extends Throwable> {

    /**
     * Converts a {@link ThrowingSupplier} into a standard {@link Supplier} that throws
     * unchecked exceptions. This is useful for integrating with APIs that expect a
     * {@link Supplier} and do not handle checked exceptions.
     *
     * @param throwingSupplier The {@link ThrowingSupplier} to convert.
     * @param <V>              The type of results supplied by the supplier.
     * @param <T>              The type of exception thrown by the supplier.
     * @return A {@link Supplier} that wraps the {@link ThrowingSupplier} and rethrows any
     * exceptions as unchecked.
     */
    static <V, T extends Throwable> Supplier<V> asSupplier(@NotNull ThrowingSupplier<V, T> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();

            } catch (Throwable t) {
                // Rethrow the caught throwable as an unchecked exception
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Gets a result, potentially throwing a checked exception.
     *
     * @return A result.
     * @throws T If an error occurs during the supply operation.
     * @throws InvalidMarshallableException If the object created is not valid.
     */
    @NotNull
    V get() throws T, InvalidMarshallableException;
}
