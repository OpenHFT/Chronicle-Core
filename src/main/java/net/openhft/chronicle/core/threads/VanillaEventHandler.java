/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.io.InvalidMarshallableException;

/**
 * Marker interface identical to {@link EventHandler} kept for binary compatibility.
 * <p>
 * Implementations perform non-blocking units of work on an event loop thread. Returning
 * {@code true} hints that more work is immediately available; throwing
 * {@link InvalidEventHandlerException#reusable()} removes the handler from the loop.
 */
@FunctionalInterface
public interface VanillaEventHandler {

    /**
     * Perform a unit of work on the event loop thread.
     *
     * @return {@code true} if more work should be scheduled immediately
     * @throws InvalidEventHandlerException to remove this handler from the loop
     * @throws InvalidMarshallableException if DTO validation fails
     */
    boolean action() throws InvalidEventHandlerException, InvalidMarshallableException;
}
