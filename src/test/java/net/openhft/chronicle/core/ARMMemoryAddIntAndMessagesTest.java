//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ARMMemoryAddIntAndMessagesTest {

    private long allocated;

    private long alloc(int bytes) {
        allocated = UnsafeMemory.UNSAFE.allocateMemory(bytes);
        for (int i = 0; i < bytes; i++) UnsafeMemory.UNSAFE.putByte(allocated + i, (byte) 0);
        return allocated;
    }

    @AfterEach
    void tearDown() { if (allocated != 0) UnsafeMemory.UNSAFE.freeMemory(allocated); allocated = 0; }

    @Test
    void addIntAlignedAndMisaligned() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long aligned = base + 4;
        assertEquals(1, arm.addInt(aligned, 1));
        assertEquals(2, arm.addInt(aligned, 1));
        long mis = base + 2;
        assertThrows(MisAlignedAssertionError.class, () -> arm.addInt(mis, 1));
    }

    @Test
    void testAndSetIntAlignedMismatchMessage() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long aligned = base + 4;
        // current value 0, expected 1 -> mismatch
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(aligned, 4L, 1, 2));
        assertTrue(ex.getMessage().contains("Expected") || ex.getMessage().contains("expected"));
        assertFalse(ex.getMessage().contains("mis-aligned"));
    }
}
