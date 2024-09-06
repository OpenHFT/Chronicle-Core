/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;

/**
 * The {@code DelegatingEventLoop} class provides an implementation of the {@link EventLoop} interface
 * that delegates all method calls to an underlying {@link EventLoop} instance.
 * <p>
 * This class can be used as a base class for creating custom event loops that enhance or override
 * the behavior of an existing {@link EventLoop} instance without modifying the original implementation.
 * </p>
 */
public class DelegatingEventLoop implements EventLoop {
    @NotNull
    public final EventLoop inner;

    /**
     * Constructs a new {@code DelegatingEventLoop} instance that delegates to the specified
     * {@link EventLoop}.
     *
     * @param eventLoop the underlying {@link EventLoop} to which this object delegates all method calls.
     */
    public DelegatingEventLoop(@NotNull EventLoop eventLoop) {
        this.inner = eventLoop;
    }

    /**
     * Returns the name of the underlying {@link EventLoop}.
     *
     * @return the name of the underlying {@link EventLoop}.
     */
    @Override
    public String name() {
        return inner.name();
    }

    /**
     * Starts the underlying {@link EventLoop}.
     */
    @Override
    public void start() {
        inner.start();
    }

    /**
     * Unpauses the underlying {@link EventLoop}.
     */
    @Override
    public void unpause() {
        inner.unpause();
    }

    /**
     * Stops the underlying {@link EventLoop}.
     */
    @Override
    public void stop() {
        inner.stop();
    }

    /**
     * Checks if the underlying {@link EventLoop} is closed.
     *
     * @return {@code true} if the underlying {@link EventLoop} is closed, otherwise {@code false}.
     */
    @Override
    public boolean isClosed() {
        return inner.isClosed();
    }

    /**
     * Checks if the underlying {@link EventLoop} is stopped.
     *
     * @return {@code true} if the underlying {@link EventLoop} is stopped, otherwise {@code false}.
     */
    @Override
    public boolean isStopped() {
        return inner.isStopped();
    }

    /**
     * Checks if the underlying {@link EventLoop} is in the process of closing.
     *
     * @return {@code true} if the underlying {@link EventLoop} is closing, otherwise {@code false}.
     */
    @Override
    public boolean isClosing() {
        return inner.isClosing();
    }

    /**
     * Checks if the underlying {@link EventLoop} is alive.
     *
     * @return {@code true} if the underlying {@link EventLoop} is alive, otherwise {@code false}.
     */
    @Override
    public boolean isAlive() {
        return inner.isAlive();
    }

    /**
     * Closes the underlying {@link EventLoop}.
     */
    @Override
    public void close() {
        inner.close();
    }

    /**
     * Adds an {@link EventHandler} to the underlying {@link EventLoop}.
     *
     * @param handler the {@link EventHandler} to be added.
     */
    @Override
    public void addHandler(EventHandler handler) {
        inner.addHandler(handler);
    }

    /**
     * Checks if the current thread runs inside the core loop of the underlying {@link EventLoop}.
     *
     * @return {@code true} if the current thread runs inside the core loop, otherwise {@code false}.
     */
    @Override
    public boolean runsInsideCoreLoop() {
        return inner.runsInsideCoreLoop();
    }
}
