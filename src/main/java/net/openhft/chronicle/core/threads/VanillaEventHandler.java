/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
     * Performs an action corresponding to some amount of work and returns a boolean indicating whether
     * it is expected that a subsequent call to this method would result in additional work being carried out.
     * <p>
     * This method is executed on the event loop's thread. A return value of {@code true} suggests to the event loop
     * that this handler should be serviced again shortly. However, the exact timing depends on various factors,
     * including the handler's priority and other activity on the event loop.
     * <p>
     * Returning {@code true} unnecessarily may lead to wastage of CPU cycles on a handler with no actual work,
     * potentially affecting the performance of other handlers. Conversely, returning {@code false} while there is work
     * pending may increase latency as the event loop might delay servicing this handler.
     * <p>
     * Generally, an action handler should perform a fixed amount of work and then return. If it is certain that
     * there is more work to be done immediately, it should return {@code true}. Otherwise, it should return {@code false},
     * and the event loop will schedule the next execution based on the handler's priority and workload.
     * <p>
     * It is recommended to experiment with different strategies under typical workloads to find the optimal approach.
     * <p>
     * To remove this event handler from the event loop, the {@link InvalidEventHandlerException} can be thrown.
     * The {@link InvalidEventHandlerException#reusable()} method returns a reusable, pre-created instance of this
     * exception, which is unmodifiable and contains no stack trace.
     *
     * @return {@code true} if it is expected that there is more work to be done imminently; {@code false} otherwise.
     * @throws InvalidEventHandlerException   if the event handler is no longer needed and should be removed from
     *                                        the event loop. This exception should be thrown if the event handler
     *                                        is closed. Use {@link InvalidEventHandlerException#reusable()} for
     *                                        a reusable instance of this exception.
     * @throws InvalidMarshallableException   if there is a failure in the validation of a DTO being read or written.
     */
    boolean action() throws InvalidEventHandlerException, InvalidMarshallableException;
}
