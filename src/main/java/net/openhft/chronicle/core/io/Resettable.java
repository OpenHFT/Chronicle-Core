/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

/**
 * An interface for components that support reuse by resetting their state.
 * Typical implementations are simple DTOs used in object pools.
 */
public interface Resettable {
    /**
     * Restore the object to its initial state. Called before returning an object
     * to a pool or when reusing the instance for another operation.
     */
    void reset();
}
