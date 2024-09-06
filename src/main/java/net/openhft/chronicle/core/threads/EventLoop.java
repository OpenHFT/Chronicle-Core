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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

/**
 * Represents an event-driven loop responsible for processing event handlers.
 * <p>
 * An {@code EventLoop} manages a set of {@link EventHandler} instances, executing them based on their priority.
 * The loop continues to execute these handlers until it is explicitly stopped.
 * </p>
 */
public interface EventLoop extends Closeable {

    // A debugging flag to track adding handlers.
    boolean DEBUG_ADDING_HANDLERS = Jvm.getBoolean("debug.adding.handlers");

    /**
     * Retrieves the name of the event loop.
     *
     * @return the name of the event loop.
     */
    String name();

    /**
     * Adds an {@link EventHandler} to the event loop to be executed.
     * <p>
     * Handlers are executed in order of their priority. Handlers with the same priority have no guarantee of execution order.
     * Handlers will not be executed until {@link #start()} has been called.
     * </p>
     *
     * @param handler The handler to be added to the event loop.
     */
    void addHandler(EventHandler handler);

    /**
     * Starts the event loop, initiating the execution of handlers.
     * <p>
     * Once the event loop is started, it continuously runs and processes handlers added to it.
     * </p>
     */
    void start();

    /**
     * Unpauses the event loop, if it is currently paused.
     * <p>
     * This is typically used in implementations where the event loop may use a pauser. Unpausing can be helpful
     * if the event loop was temporarily paused and needs to resume execution.
     * </p>
     */
    void unpause();

    /**
     * Stops the execution of handlers and blocks until all handlers have completed.
     * <p>
     * Once an event loop is stopped, it is not expected to be restarted.
     * </p>
     */
    void stop();

    /**
     * Checks if the main thread of the event loop is currently running.
     *
     * @return {@code true} if the main thread of the event loop is running, otherwise {@code false}.
     */
    boolean isAlive();

    /**
     * Checks if the event loop is in the stopped state.
     *
     * @return {@code true} if the event loop has stopped, otherwise {@code false}.
     */
    boolean isStopped();

    /**
     * Determines if the current thread is executing within an event loop.
     *
     * @return {@code true} if the current thread is executing inside an event loop, otherwise {@code false}.
     */
    static boolean inEventLoop() {
        return CleaningThread.inEventLoop(Thread.currentThread());
    }

    /**
     * Stops the event loop and closes any resources it holds.
     * <p>
     * This method blocks until all handlers have stopped and all resources have been released.
     * </p>
     */
    @Override
    void close();

    /**
     * Determines if the current thread is the core thread of this event loop or if this cannot be determined.
     * <p>
     * Returns {@code true} by default, unless it can be determined that the current thread is not the core event thread.
     * </p>
     *
     * @return {@code true} if the current thread is the core event thread, or if this information cannot be determined; otherwise {@code false}.
     */
    default boolean runsInsideCoreLoop() {
        return true;
    }
}
