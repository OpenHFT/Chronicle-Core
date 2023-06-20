/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;
/**
 * A interface for querying the closeable state of an object.
 * Provides methods to check if the object is closed or in the process of closing.
 */
public interface QueryCloseable {

    /**
     * Checks if this object is in the process of closing.
     * <p>
     * This method should always return true if {@link #isClosed()} returns true.
     *
     * @return true if the {@code close()} method has been called (but not necessarily completed)
     */
    default boolean isClosing() {
        return isClosed();
    }

    /**
     * Checks if this object is closed.
     *
     * @return true if the {@code close()} method has completed, false otherwise
     */
    boolean isClosed();
}
