/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
    default void eventLoop(EventLoop eventLoop) {
    }

    /**
     * Notify handler that the event the handler's action method
     * will not be called again. Should be called once only. Should be called from the event
     * loop's execution thread.
     * <p>This is called either when the event loop is terminating, or if this EventHandler s being
     * removed from the event loop.
     * <p>If this implements {@link Closeable} then the event loop will close this after
     * loopFinished has been called. close should be called once only.
     * <p>Exceptions thrown by loopFinished or close are caught and logged (at debug level)
     * and cleanup continues
     */
    default void loopFinished() {
    }

    @NotNull
    default HandlerPriority priority() {
        return HandlerPriority.MEDIUM;
    }
}

