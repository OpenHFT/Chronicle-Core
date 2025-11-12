/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;

/**
 * The Mocker class provides utility methods for creating mocked instances of interfaces.
 * This class was left here for backward compatibility.
 *
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
