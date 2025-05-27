/*
 * Copyright 2016-2025 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.InvalidMarshallableException;

/**
 * Represents an event handler that performs actions within an event loop.
 * <p>
 * This interface should be implemented by classes that handle specific events or tasks within an event loop.
 * The {@code action()} method is responsible for performing the necessary actions or tasks.
 * <p>
 * The event loop can service multiple event handlers, and the frequency at which any particular handler is serviced
 * is influenced by the handler's priority as well as the overall activity within the event loop.
 */
@FunctionalInterface
public interface VanillaEventHandler {

    /**
     * Performs a unit of work on the event loop thread.
     * This method should return quickly without blocking.
     *
     * @implSpec Returning {@code true} biases the scheduler to call this handler again
     *           without delay. Throwing {@link InvalidEventHandlerException#reusable()}
     *           removes the handler from the {@link EventLoop} and must have no side
     *           effects.
     *
     * @return {@code true} if more work is expected imminently; {@code false} otherwise
     * @throws InvalidEventHandlerException to remove this handler from the event loop
     * @throws InvalidMarshallableException if a DTO validation fails
     */
    boolean action() throws InvalidEventHandlerException, InvalidMarshallableException;
}
