//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

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

    @Test
    void syncIfAvailableShouldCallSyncOnSyncableObjects() {
        Syncable syncableMock = mock(Syncable.class);
        Syncable.syncIfAvailable(syncableMock);

        verify(syncableMock, times(1)).sync();
    }

    @Test
    void syncIfAvailableShouldNotThrowExceptionForNonSyncableObjects() {
        Object nonSyncableObject = new Object();

        assertDoesNotThrow(() -> Syncable.syncIfAvailable(nonSyncableObject));
    }

    @Test
    void syncShouldSetSyncedToTrueForSyncableImpl() {
        SyncableImpl syncableImpl = new SyncableImpl();
        assertFalse(syncableImpl.synced);

        syncableImpl.sync();

        assertTrue(syncableImpl.synced);
    }
}
