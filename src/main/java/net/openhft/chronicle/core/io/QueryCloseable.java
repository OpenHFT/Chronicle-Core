/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.io;

public interface QueryCloseable {
    boolean isClosed();

    default void throwExceptionIfClosed() throws IllegalStateException {
        if (isClosed())
            throw new IllegalStateException("Closed");
    }
}
