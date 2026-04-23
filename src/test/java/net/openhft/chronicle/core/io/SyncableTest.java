/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

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
        SyncableImpl syncable = new SyncableImpl();
        Syncable.syncIfAvailable(syncable);

        assertTrue(syncable.synced);
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
