//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;

/**
 * Lightweight helper implementing {@link Closeable} and {@link ManagedCloseable}.
 * A boolean tracks the closed state. Override {@link #performClose()} for custom clean-up.
 */
public abstract class SimpleCloseable implements Closeable, ReferenceOwner, ManagedCloseable {
    private transient volatile boolean closed;

    /**
     * Constructs a new instance of {@code SimpleCloseable}.
     * This constructor is protected to encourage inheritance and prevent direct instantiation.
     */
    protected SimpleCloseable() {
    }

    /**
     * Idempotent close for use with try-with-resources.
     */
    @Override
    public final void close() {
        if (closed)
            return;
        closed = true;
        performClose();
    }

    /**
     * Hook for subclasses to release resources. Called once from {@link #close()}.
     */
    protected void performClose() {
        // might be nothing.
    }

    /**
     * Checks if the resource is closed.
     *
     * @return {@code true} if the resource is closed, {@code false} otherwise.
     */
    @Override
    public boolean isClosed() {
        return closed;
    }
}
