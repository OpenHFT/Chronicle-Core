/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.test;

import net.openhft.chronicle.core.io.Closeable;

public class RecordingCloseable implements Closeable {
    private int closeCount;
    private boolean closed;
    private boolean closing;

    @Override
    public void close() {
        closeCount++;
        closing = true;
        closed = true;
    }

    @Override
    public boolean isClosing() {
        return closing || closed;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public int closeCount() {
        return closeCount;
    }

    public void closing(boolean closing) {
        this.closing = closing;
    }

    public void closed(boolean closed) {
        this.closed = closed;
        if (closed)
            this.closing = true;
    }
}
