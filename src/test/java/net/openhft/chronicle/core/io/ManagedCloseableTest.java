/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.test.RecordingManagedCloseable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagedCloseableTest {
    @Test
    void testWarnAndCloseIfNotClosed() {
        RecordingManagedCloseable closeable = new RecordingManagedCloseable();

        closeable.warnAndCloseIfNotClosed();

        assertEquals(1, closeable.closeCount());
    }

    @Test
    void testThrowExceptionIfClosed() {
        RecordingManagedCloseable closeable = new RecordingManagedCloseable();
        closeable.closed(true);

        assertThrows(ClosedIllegalStateException.class, closeable::throwExceptionIfClosed);
    }

    @Test
    void testCreatedHere() {
        RecordingManagedCloseable closeable = new RecordingManagedCloseable();

        assertNull(closeable.createdHere());
    }
}
