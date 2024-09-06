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

package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * Represents a handler for events within an event loop.
 * <p>
 * This interface should be implemented by classes that handle specific events within an event loop.
 * An {@code EventHandler} can be registered to an {@link EventLoop} for processing events.
 * </p>
 */
@FunctionalInterface
public interface EventHandler extends VanillaEventHandler {

    /**
     * Called once when this handler is added to an {@link EventLoop}.
     * <p>
     * This method might be invoked before the {@link EventLoop} has started and could be called from any thread.
     * </p>
     *
     * @param eventLoop The event loop to which this handler has been added.
     */
    default void eventLoop(EventLoop eventLoop) {
        // Default implementation does nothing.
    }

    /**
     * Called when the handler has begun being executed on the event loop.
     * <p>
     * This method will always be called on the {@link EventLoop} thread and is invoked after {@link #eventLoop(EventLoop)}.
     * It will be followed by zero or more invocations of {@link #action()} and then an invocation of {@link #loopFinished()}.
     * </p>
     * <p>
     * If this method throws an exception, it will be logged and the handler will be removed from the event loop.
     * </p>
     */
    default void loopStarted() {
        // Default implementation does nothing.
    }

    /**
     * Notifies the handler that its action method will not be called again.
     * <p>
     * This method will be called if and only if {@link #loopStarted()} was called, and it will always be called on the event loop thread.
     * It is invoked when the event loop is terminating or if this {@code EventHandler} is being removed from the event loop.
     * </p>
     * <p>
     * If this handler implements {@link Closeable}, the event loop will call {@link Closeable#close()} (once only) after {@code loopFinished()} has been called.
     * </p>
     * <p>
     * The {@code loopFinished()} method is intended for any cleanup that must be performed on the event loop thread or cleanup that only needs to occur if {@link #loopStarted()} was called.
     * All other cleanup should be done in {@link Closeable#close()}.
     * </p>
     * <p>
     * Exceptions thrown by {@code loopFinished()} or {@code close()} are caught, logged at the warning level, and cleanup continues.
     * </p>
     */
    default void loopFinished() {
        // Default implementation does nothing.
    }

    /**
     * Returns the priority level of this event handler within the event loop.
     * <p>
     * The priority determines the order in which handlers are executed within the event loop.
     * </p>
     *
     * @return The priority level of this event handler. Default is {@link HandlerPriority#MEDIUM}.
     */
    @NotNull
    default HandlerPriority priority() {
        return HandlerPriority.MEDIUM;
    }
}
