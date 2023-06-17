/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.io;

/**
 * An abstract class providing a basic implementation of the {@link Closeable}, {@link ReferenceOwner}, and {@link ManagedCloseable} interfaces.
 * It maintains a flag to track the closed state of the resource and provides methods for closing the resource.
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
     * Closes the resource so that it cannot be used again.
     * Once closed, subsequent calls to this method will have no effect.
     */
    @Override
    public final void close() {
        if (closed)
            return;
        closed = true;
        performClose();
    }

    /**
     * Performs the actual close operation.
     * Subclasses can override this method to provide custom close logic.
     * The default implementation does nothing.
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
