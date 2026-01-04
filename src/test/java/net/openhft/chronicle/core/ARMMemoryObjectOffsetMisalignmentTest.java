/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
class ARMMemoryObjectOffsetMisalignmentTest {

    @Test
    @DisplayName("Volatile short on misaligned object offset")
    void volatileShortOnMisalignedObjectOffset() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[8];
        long off = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 1; // odd = misaligned for short
        arm.writeVolatileShort(bytes, off, (short) 0x1234);
        assertEquals((short) 0x1234, arm.readVolatileShort(bytes, off), "readVolatileShort should return written value at misaligned offset");
    }

    @Test
    @DisplayName("And set int object aligned mismatch vs misaligned")
    void testAndSetIntObjectAlignedMismatchVsMisaligned() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[16];
        long aligned = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 4L; // 4-byte aligned
        long mis = aligned + 2; // misaligned
        IllegalStateException alignedMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, aligned, 1, 2),
                "testAndSetInt should throw on aligned mismatch for object offset");
        assertTrue(alignedMsg.getMessage().contains("Cannot change"),
                "aligned testAndSetInt message should include \"Cannot change\": " + alignedMsg.getMessage());

        IllegalStateException misMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, mis, 1, 2),
                "testAndSetInt should throw on misaligned object offset");
        assertTrue(misMsg.getMessage().contains("mis-aligned"),
                "misaligned testAndSetInt message should include \"mis-aligned\": " + misMsg.getMessage());
    }
}
