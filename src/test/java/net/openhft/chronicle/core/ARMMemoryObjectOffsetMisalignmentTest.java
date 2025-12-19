/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ARMMemoryObjectOffsetMisalignmentTest {

    @Test
    void volatileShortOnMisalignedObjectOffset() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[8];
        long off = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 1; // odd = misaligned for short
        arm.writeVolatileShort(bytes, off, (short) 0x1234);
        assertEquals((short) 0x1234, arm.readVolatileShort(bytes, off), "readVolatileShort should return written value at misaligned offset");
    }

    @Test
    void testAndSetIntObjectAlignedMismatchVsMisaligned() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[16];
        long aligned = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 4L; // 4-byte aligned
        long mis = aligned + 2; // misaligned
        IllegalStateException alignedMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, aligned, 1, 2));
        assertTrue(alignedMsg.getMessage().contains("Cannot change"), "testAndSetInt should throw exception with 'Cannot change' message when expected value mismatches");

        IllegalStateException misMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, mis, 1, 2));
        assertTrue(misMsg.getMessage().contains("mis-aligned"), "testAndSetInt should throw exception with 'mis-aligned' message at misaligned offset");
    }
}
