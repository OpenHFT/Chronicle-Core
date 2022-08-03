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

@FunctionalInterface
public interface VanillaEventHandler {

    /**
     * Performs an action corresponding to some amount of work and returns if it is expected that a
     * subsequent call to this method would result in additional work being carried out.
     * <p>
     * This method is called from an event loop's execution thread. Each event loop can services multiple event handlers.
     * The aggressiveness with which any one handler is serviced is influenced by the handler's priority as well as
     * other activity on the event loop as a whole.
     * <p>
     * If an event handler returns {@code true } from action(), it biases the event loop to service the same
     * handler again "soon". How soon depends on a variety of factors and the other work the event loop has
     * to do across the other handlers.
     * <p>
     * Returning {@code true } when there is no actual work to do may waste cycles servicing a handler which has nothing
     * to do, at the expense of stealing cycles away from other handlers.
     * Conversely, returning <code>false</code> when there is work to do will effectively increase latency as the event loop
     * will take the "false" as a hint that several other handlers can be serviced ahead of this one.
     * <p>
     * As a rule of thumb, an action handler should do a certain amount of work then yield/return
     * If it knows for sure that there is remaining work to be done at the point of yielding then return {@code true}.
     * Otherwise return <code>false</code> and the event loop will revisit based on the handler's priority and other work load.
     * <p>
     * As with a lot of scheduling approaches there's no single answer and some experimentation under typical loads
     * would always be recommended. But the above rule of thumb is a good starting point.
     * <p>
     * When the event handler is not required anymore and should be removed from the event loop, the
     * {@link InvalidEventHandlerException#reusable()} method returns a reusable pre-created
     * InvalidEventHandlerException which can be thrown to remove the EventHandler from the EventLoop.
     *
     * @return true if you expect more work very soon.
     * @throws InvalidEventHandlerException when the event handler is not required anymore and should be removed from
     *         the event loop.
     *         It is recommended to throw this exception if the event handler is closed.
     *         The InvalidEventHandlerException.reusable() method returns a reusable, pre-created,
     *         InvalidEventHandlerException that is unmodifiable and contains no stack trace.
     *         See {@link InvalidEventHandlerException#reusable()}.
     */
    boolean action() throws InvalidEventHandlerException;
}
