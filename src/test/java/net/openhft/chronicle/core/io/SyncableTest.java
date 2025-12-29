/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SyncableTest {

    static class SyncableImpl implements Syncable {
        boolean synced = false;

        @Override
        public void sync() {
            synced = true;
        }
    }

    @DisplayName("syncIfAvailable calls sync on Syncable objects")
    @Test
    void syncIfAvailableShouldCallSyncOnSyncableObjects() {
        Syncable syncableMock = mock(Syncable.class);
        Syncable.syncIfAvailable(syncableMock);

        verify(syncableMock, times(1)).sync();
    }

    @DisplayName("syncIfAvailable ignores non-Syncable objects safely")
    @Test
    void syncIfAvailableShouldNotThrowExceptionForNonSyncableObjects() {
        Object nonSyncableObject = new Object();

        assertDoesNotThrow(() -> Syncable.syncIfAvailable(nonSyncableObject),
                "syncIfAvailable should not throw for non-Syncable object");
    }

    @DisplayName("sync sets synced flag for implementation")
    @Test
    void syncShouldSetSyncedToTrueForSyncableImpl() {
        SyncableImpl syncableImpl = new SyncableImpl();
        assertFalse(syncableImpl.synced, "syncable implementation should not be synced initially");

        syncableImpl.sync();

        assertTrue(syncableImpl.synced, "synchronization should be complete");
    }
}
