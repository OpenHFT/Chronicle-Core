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

package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The Mocker class provides utility methods for creating mocked instances of interfaces.
 * @see net.openhft.chronicle.core.util.Mocker
 */
public final class Mocker {
    // Suppresses default constructor, ensuring non-instantiability.
    private Mocker() {
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided PrintStream.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the PrintStream to log the method invocations
     * @return the mocked instance of the class
     */
    // Used in generated code in FIX
    @NotNull
    public static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull PrintStream out) {
        return net.openhft.chronicle.core.util.Mocker.logging(interfaceType, description, out);
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided PrintWriter.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the PrintWriter to log the method invocations
     * @return the mocked instance of the class
     */
    @Deprecated(/* to be removed in x.26 */)
    @NotNull
    static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull PrintWriter out) {
        return net.openhft.chronicle.core.util.Mocker.logging(interfaceType, description, out);
    }

    /**
     * Creates a mocked instance of the specified interface that logs method invocations to the provided StringWriter.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param out           the StringWriter to log the method invocations
     * @return the mocked instance of the class
     */
    // Used in generated code in FIX
    @NotNull
    public static <T> T logging(@NotNull Class<T> interfaceType, String description, @NotNull StringWriter out) {
        return net.openhft.chronicle.core.util.Mocker.logging(interfaceType, description, out);
    }

    /**
     * Creates a mocked instance of the specified interface that enqueues method invocations to the provided BlockingQueue.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param queue         the BlockingQueue to enqueue the method invocations
     * @return the mocked instance of the class
     */
    // Used in generated code in FIX
    @NotNull
    public static <T> T queuing(@NotNull Class<T> interfaceType, String description, @NotNull BlockingQueue<String> queue) {
        return net.openhft.chronicle.core.util.Mocker.queuing(interfaceType, description, queue);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations using the provided consumer.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param consumer      the consumer to intercept and handle the method invocations
     * @return the mocked instance of the class
     */
    @Deprecated(/* to be removed in x.26 */)
    @NotNull
    public static <T> T intercepting(@NotNull Class<T> interfaceType, String description, @NotNull Consumer<String> consumer) {
        return net.openhft.chronicle.core.util.Mocker.intercepting(interfaceType, description, consumer, null);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations
     * using the provided consumer,
     * and optionally delegates the intercepted invocations to the provided object.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param description   the description of the mocking behavior
     * @param consumer      the consumer to intercept and handle the method invocations
     * @param t             the object to delegate the intercepted invocations, or null if no delegation is needed
     * @return the mocked instance of the class
     */
    @Deprecated(/* to be removed in x.26 */)
    @NotNull
    static <T> T intercepting(@NotNull Class<T> interfaceType, @NotNull final String description, @NotNull Consumer<String> consumer, T t) {
        return net.openhft.chronicle.core.util.Mocker.intercepting(interfaceType, description, consumer, t);
    }

    /**
     * Creates a mocked instance of the specified interface that intercepts method invocations using the provided
     * bi-consumer, and optionally delegates the intercepted invocations to the provided object.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param consumer      the bi-consumer to intercept and handle the method invocations
     * @param t             the object to delegate the intercepted invocations, or null if no delegation is needed
     * @return the mocked instance of the class
     */
    @Deprecated(/* to be removed in x.26 */)
    @NotNull
    public static <T> T intercepting(@NotNull Class<T> interfaceType, @NotNull BiConsumer<String, Object[]> consumer, T t) {
        return net.openhft.chronicle.core.util.Mocker.intercepting(interfaceType, consumer, t);
    }

    /**
     * Creates a mocked instance of the specified interface that ignores all method invocations.
     *
     * @param <T>           the type of the class
     * @param interfaceType the class to be mocked
     * @param additional    additional classes to add to the mocked instance
     * @return the mocked instance of the class
     */
    // Used in generated code in FIX
    @NotNull
    public static <T> T ignored(@NotNull Class<T> interfaceType, Class<?>... additional) {
        return net.openhft.chronicle.core.util.Mocker.ignored(interfaceType, additional);
    }
}
