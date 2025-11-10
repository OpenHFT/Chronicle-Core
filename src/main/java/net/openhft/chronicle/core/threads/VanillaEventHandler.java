//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
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
     * <p> Returning {@code true} biases the scheduler to call this handler again
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
