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

@FunctionalInterface
public interface EventHandler extends VanillaEventHandler {

    /**
     * This method is called once when it is added to an eventLoop, which might be before the EventLoop has started
     * This could be called in any thread.
     *
     * @param eventLoop the handler has been added to.
     */
    default void eventLoop(EventLoop eventLoop) {
    }

    /**
     * This handler has begun being executed on the event loop. This will be called after the call to {@link #eventLoop(EventLoop)}
     * and will always be called on the EventLoop thread.
     * <p>
     * This call will be followed by zero or more invocations of {@link #action()} and then an invocation of {@link #loopFinished()}
     * <p>
     * Exceptions thrown by loopStarted will be logged and the handler will be removed from the event loop.
     */
    default void loopStarted() {
    }

    /**
     * Notify handler that the event handler's action method will not be called again. It will be called
     * if and only if {@link #loopStarted()} was called, and will always be called on the event loop thread.
     * <p>
     * This is called either when the event loop is terminating, or if this EventHandler is being
     * removed from the event loop.
     * <p>
     * If this handler implements {@link Closeable} then the event loop will call close (once only) on this after
     * loopFinished has been called.
     * <p>
     * loopFinished is the place to put any clean-up that MUST be performed on the event loop thread, or
     * cleanup that only needs to occur if {@link #loopStarted()} was called. All other clean up can be put into
     * {@link Closeable#close()}.
     * <p>
     * Exceptions thrown by loopFinished or close are caught and logged (at warning level) and cleanup continues.
     */
    default void loopFinished() {
    }

    @NotNull
    default HandlerPriority priority() {
        return HandlerPriority.MEDIUM;
    }
}

