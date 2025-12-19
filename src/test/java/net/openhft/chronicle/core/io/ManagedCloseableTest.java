/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ManagedCloseableTest {
    @BeforeEach
    void mockitoNotSupportedOnJava21() {
        Assumptions.assumeTrue(Jvm.majorVersion() <= 17);
    }

    @Test
    void testWarnAndCloseIfNotClosed() {
        ManagedCloseable closeable = spy(ManagedCloseable.class);

        when(closeable.isClosing()).thenReturn(false);

        closeable.warnAndCloseIfNotClosed();

        verify(closeable, times(1)).close();
    }

    @Test
    void testThrowExceptionIfClosed() {
        ManagedCloseable closeable = Mockito.spy(ManagedCloseable.class);

        when(closeable.isClosing()).thenReturn(true);
        when(closeable.isClosed()).thenReturn(true);

        assertThrows(ClosedIllegalStateException.class, closeable::throwExceptionIfClosed);
    }

    @Test
    void testCreatedHere() {
        ManagedCloseable closeable = Mockito.spy(ManagedCloseable.class);

        assertNull(closeable.createdHere(), "ManagedCloseable createdHere should return null for mock instance");
    }
}
