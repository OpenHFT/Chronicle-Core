/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedCloseableTest {
    @DisplayName("Warn and close if not closed managed")
    @Test
    void testWarnAndCloseIfNotClosed() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();

        closeable.warnAndCloseIfNotClosed();

        assertTrue(closeable.wasClosed(), "warnAndCloseIfNotClosed should close when not already closing");
    }

    @DisplayName("Throw exception if closed managed closeable")
    @Test
    void testThrowExceptionIfClosed() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();
        closeable.setClosed(true);

        assertThrows(ClosedIllegalStateException.class, closeable::throwExceptionIfClosed,
                "throwExceptionIfClosed should throw when closeable reports closed");
    }

    @DisplayName("createdHere returns null for default closeable implementation")
    @Test
    void testCreatedHere() {
        ManagedCloseable closeable = new ManagedCloseableProbe();

        assertNull(closeable.createdHere(), "ManagedCloseable createdHere should return null for default implementation");
    }

    private static final class ManagedCloseableProbe implements ManagedCloseable {
        private boolean closing;
        private boolean closed;
        private boolean closeCalled;

        @Override
        public void close() {
            closeCalled = true;
            closed = true;
            closing = true;
        }

        @Override
        public boolean isClosing() {
            return closing;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        void setClosed(boolean closed) {
            this.closed = closed;
            if (closed) {
                this.closing = true;
            }
        }

        boolean wasClosed() {
            return closeCalled;
        }
    }
}
