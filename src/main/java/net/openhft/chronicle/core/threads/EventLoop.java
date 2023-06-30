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
 * EventLoop represents an event-driven loop responsible for processing event handlers.
 * Handlers can be added to the event loop and are executed based on their priority.
 * The event loop continues to execute handlers until it is explicitly stopped.
 */
public interface EventLoop extends Closeable {

    boolean DEBUG_ADDING_HANDLERS = Jvm.getBoolean("debug.adding.handlers");

    /**
     * Retrieves the name of the event loop.
     *
     * @return the name of the event loop.
     */
    String name();

    /**
     * Adds a handler to the event loop to be executed. The event loop should execute
     * handlers in order of their priority. Handlers with the same priority have no
     * guarantee of execution order. Handlers will not be executed before {@link #start()}
     * has been called.
     *
     * @param handler The handler to be added to the event loop.
     */
    void addHandler(EventHandler handler);

    /**
     * Starts the event loop. Once the event loop is started, it begins executing handlers.
     */
    void start();

    /**
     * Typically, implementations will unpause the event loop's pauser, if used.
     * This can be helpful in cases where the event loop was temporarily paused for some reason.
     */
    void unpause();

    /**
     * Stops executing handlers and blocks until all handlers are complete.
     * It is not expected that event loops can then be restarted.
     */
    void stop();

    /**
     * Checks if the main thread of the event loop is running.
     *
     * @return {@code true} if the main thread is running, otherwise {@code false}.
     */
    boolean isAlive();

    /**
     * Checks if the event loop is in the stopped state.
     *
     * @return {@code true} if the event loop is in the stopped state, otherwise {@code false}.
     */
    boolean isStopped();

    /**
     * Waits until the event loop has terminated (after {@link #stop()} has been called).
     *
     * @deprecated {@link #stop()} and {@link #close()} both block until the event handlers have
     * finished running, there's no reason to call this method explicitly.
     */
    @Deprecated(/* for removal in x.25 */)
    void awaitTermination();

    /**
     * Checks if the current thread is executing inside an event loop.
     *
     * @return {@code true} if the current thread is executing inside an event loop, otherwise {@code false}.
     */
    static boolean inEventLoop() {
        return CleaningThread.inEventLoop(Thread.currentThread());
    }

    /**
     * Stops the event loop and then closes any resources being held.
     * Blocks until all the handlers are stopped and closed.
     */
    @Override
    void close();

    /**
     * Checks if the current thread is the core thread of this event loop, or if this information
     * cannot be determined. If the current thread is not the core event thread but any other,
     * this method returns {@code false}.
     *
     * @return {@code true} unless the current thread is not the core event thread.
     */
    default boolean runsInsideCoreLoop() {
        return true;
    }
}
