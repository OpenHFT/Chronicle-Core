/*
 * Copyright 2016-2025 chronicle.software
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
