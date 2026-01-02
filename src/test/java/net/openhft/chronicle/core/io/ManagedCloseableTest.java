/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagedCloseableTest {
    @Test
    @DisplayName("Warn and close if not closed managed")
    void testWarnAndCloseIfNotClosed() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();

        closeable.warnAndCloseIfNotClosed();

        assertTrue(closeable.wasClosed(), "warnAndCloseIfNotClosed should close when not already closing");
    }

    @Test
    @DisplayName("Throw exception if closed managed closeable")
    void testThrowExceptionIfClosed() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();
        closeable.setClosed(true);

        assertThrows(ClosedIllegalStateException.class, closeable::throwExceptionIfClosed,
                "throwExceptionIfClosed should throw when closeable reports closed");
    }

    @Test
    @DisplayName("createdHere returns null for default closeable implementation")
    void testCreatedHere() {
        ManagedCloseable closeable = new ManagedCloseableProbe();

        assertNull(closeable.createdHere(), "ManagedCloseable createdHere should return null for default implementation");
    }

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("warnAndCloseIfNotClosed does nothing when already closing")
    void warnAndCloseDoesNothingWhenAlreadyClosing() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();
        closeable.setClosing(true);

        closeable.warnAndCloseIfNotClosed();

        assertFalse(closeable.wasClosed(), "warnAndCloseIfNotClosed should not close when already closing");
    }

    @Test
    @DisplayName("throwExceptionIfClosed reports Closing when closing but not closed")
    void throwExceptionIfClosedReportsClosing() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();
        closeable.setClosing(true);

        ClosedIllegalStateException exception = assertThrows(
                ClosedIllegalStateException.class,
                closeable::throwExceptionIfClosed,
                "throwExceptionIfClosed should throw when closing");
        assertTrue(exception.getMessage().contains("Closing"),
                "exception message should contain Closing");
    }

    @Test
    @DisplayName("throwExceptionIfClosed does not throw when not closing")
    void throwExceptionIfClosedDoesNotThrowWhenOpen() {
        ManagedCloseableProbe closeable = new ManagedCloseableProbe();

        assertDoesNotThrow(closeable::throwExceptionIfClosed,
                "throwExceptionIfClosed should not throw when not closing");
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

        void setClosing(boolean closing) {
            this.closing = closing;
        }

        boolean wasClosed() {
            return closeCalled;
        }
    }
}
