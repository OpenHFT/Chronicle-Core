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
 * This class is an implementation of the {@link EventLoop} interface
 * that delegates all calls to an underlying {@link EventLoop} instance.
 * <p>
 * This can be used as a base class for implementations that need to override
 * or add behavior to an existing {@link EventLoop} instance without modifying
 * the original class.
 * 
 */
public class DelegatingEventLoop implements EventLoop {
    @NotNull
    private final EventLoop inner;

    /**
     * Constructs a new {@code DelegatingEventLoop} instance that delegates to the specified
     * {@link EventLoop}.
     *
     * @param eventLoop the underlying {@link EventLoop} to which this object delegates all calls.
     */
    public DelegatingEventLoop(@NotNull EventLoop eventLoop) {
        this.inner = eventLoop;
    }

    @Override
    public String name() {
        return inner.name();
    }

    @Override
    public void start() {
        inner.start();
    }

    @Override
    public void unpause() {
        inner.unpause();
    }

    @Override
    public void stop() {
        inner.stop();
    }

    @Override
    public boolean isClosed() {
        return inner.isClosed();
    }

    @Override
    public boolean isStopped() {
        return inner.isStopped();
    }

    @Override
    public boolean isClosing() {
        return inner.isClosing();
    }

    @Override
    public boolean isAlive() {
        return inner.isAlive();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void awaitTermination() {
        inner.awaitTermination();
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public void addHandler(EventHandler handler) {
        inner.addHandler(handler);
    }

    @Override
    public boolean runsInsideCoreLoop() {
        return inner.runsInsideCoreLoop();
    }
}
