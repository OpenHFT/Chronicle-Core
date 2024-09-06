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

import java.util.function.Supplier;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * A wrapper for an {@link EventLoop} that is created on demand when any of its methods are called.
 * <p>
 * This class allows for the lazy instantiation of an EventLoop, creating it only when it is actually needed.
 * </p>
 */
public class OnDemandEventLoop implements EventLoop {
    // Supplier for creating the actual EventLoop when required
    private final Supplier<EventLoop> eventLoopSupplier;
    // The actual EventLoop instance, initialized on demand
    private volatile EventLoop eventLoop;

    /**
     * Constructs an OnDemandEventLoop with the given supplier for creating the actual EventLoop.
     *
     * @param eventLoopSupplier The supplier to be used for creating the actual EventLoop on demand.
     * @throws NullPointerException if the eventLoopSupplier is null.
     */
    public OnDemandEventLoop(@NotNull final Supplier<EventLoop> eventLoopSupplier) {
        this.eventLoopSupplier = requireNonNull(eventLoopSupplier);
    }

    /**
     * Gets the EventLoop instance, creating it if it has not been initialized yet.
     *
     * @return The EventLoop instance.
     */
    EventLoop eventLoop() {
        EventLoop el = this.eventLoop;
        // Check if the event loop has already been created
        if (el != null)
            return el;
        synchronized (this) {
            el = this.eventLoop;
            // Double-check in case it was initialized while waiting for the lock
            if (el != null)
                return el;
            // Initialize the EventLoop
            eventLoop = eventLoopSupplier.get();
            return eventLoop;
        }
    }

    /**
     * Checks if the EventLoop has been created.
     *
     * @return true if the EventLoop has been created, false otherwise.
     */
    public boolean hasEventLoop() {
        return eventLoop != null;
    }

    @Override
    public String name() {
        // Delegate to the actual EventLoop instance
        return eventLoop().name();
    }

    @Override
    public void addHandler(EventHandler handler) {
        // Delegate to the actual EventLoop instance
        eventLoop().addHandler(handler);
    }

    @Override
    public void start() {
        // Delegate to the actual EventLoop instance
        eventLoop().start();
    }

    @Override
    public void unpause() {
        // Unpause only if the EventLoop has been created
        if (hasEventLoop())
            eventLoop().unpause();
    }

    @Override
    public void stop() {
        // Stop only if the EventLoop has been created
        if (hasEventLoop())
            eventLoop().stop();
    }

    @Override
    public boolean isClosed() {
        // Check if the EventLoop is closed if it has been created
        return !hasEventLoop() && eventLoop().isClosed();
    }

    @Override
    public boolean isAlive() {
        // Check if the EventLoop is alive if it has been created
        return hasEventLoop() && eventLoop().isAlive();
    }

    @Override
    public boolean isStopped() {
        final EventLoop el = this.eventLoop;
        // Check if the EventLoop is stopped if it has been created
        return el != null && el.isStopped();
    }

    /**
     * Closes the EventLoop if it has been created.
     */
    @Override
    public void close() {
        // Close only if the EventLoop has been created
        if (hasEventLoop())
            eventLoop().close();
    }
}
