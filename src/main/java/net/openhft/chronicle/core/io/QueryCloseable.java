/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;

public interface QueryCloseable {
    default boolean isClosing() {
        return isClosed();
    }

    boolean isClosed();

    // throws IllegalStateException
    default void throwExceptionIfClosed() {
        if (isClosing())
            throw new ClosedIllegalStateException(isClosed() ? "Closed" : "Closing");
    }
}
