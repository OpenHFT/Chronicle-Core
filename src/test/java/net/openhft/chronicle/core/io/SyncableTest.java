/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SyncableTest {

    public static class SyncableImpl implements Syncable {
        boolean synced = false;

        @Override
        public void sync() {
            synced = true;
        }
    }

    @Test
    public void syncIfAvailableShouldCallSyncOnSyncableObjects() {
        Syncable syncableMock = mock(Syncable.class);
        Syncable.syncIfAvailable(syncableMock);

        verify(syncableMock, times(1)).sync();
    }

    @Test
    public void syncIfAvailableShouldNotThrowExceptionForNonSyncableObjects() {
        Object nonSyncableObject = new Object();

        assertDoesNotThrow(() -> Syncable.syncIfAvailable(nonSyncableObject));
    }

    @Test
    public void syncShouldSetSyncedToTrueForSyncableImpl() {
        SyncableImpl syncableImpl = new SyncableImpl();
        assertFalse(syncableImpl.synced);

        syncableImpl.sync();

        assertTrue(syncableImpl.synced);
    }
}
