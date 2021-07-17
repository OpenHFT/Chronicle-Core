/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;

public interface QueryCloseable {

    /**
     * Is this object closed, or in the process of closing
     * <p>
     * This should always be true if {@link #isClosed()} is true
     *
     * @return true if close() has been called (but not necessarily completed)
     */
    default boolean isClosing() {
        return isClosed();
    }

    /**
     * Is this object closed
     *
     * @return true if close() has completed
     */
    boolean isClosed();

    /* to be moved in x.22 to ManagedCloseable*/
    default void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosing())
            throw new ClosedIllegalStateException(isClosed() ? "Closed" : "Closing");
    }
}
