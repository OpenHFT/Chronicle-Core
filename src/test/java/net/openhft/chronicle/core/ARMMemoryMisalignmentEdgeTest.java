/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
class ARMMemoryMisalignmentEdgeTest {

    private long allocated = 0;

    private long alloc(int bytes) {
        if (allocated != 0) throw new IllegalStateException("memory block is already allocated for this test");
        allocated = UnsafeMemory.UNSAFE.allocateMemory(bytes);
        // zero it for predictable assertions
        for (int i = 0; i < bytes; i++) UnsafeMemory.UNSAFE.putByte(allocated + i, (byte) 0);
        return allocated;
    }

    @AfterEach
    void tearDown() {
        if (allocated != 0) {
            UnsafeMemory.UNSAFE.freeMemory(allocated);
            allocated = 0;
        }
    }

    @Test
    @DisplayName("Volatile short on misaligned address offheap")
    void volatileShortOnMisalignedAddressOffheap() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(8);
        long mis = base + 1; // not 2-byte aligned
        short v = (short) 0x7B0F;
        arm.writeVolatileShort(mis, v);
        assertEquals(v, arm.readVolatileShort(mis), "volatile short read should return written value at misaligned offheap address");
        // aligned fast-path still works
        arm.writeVolatileShort(base, (short) 123);
        assertEquals(123, arm.readVolatileShort(base), "volatile short read should return written value at aligned offheap address");
    }

    @Test
    @DisplayName("Compare and swap int misaligned throws")
    void compareAndSwapIntMisalignedThrows() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long mis = base + 1; // not 4-byte aligned
        assertThrows(MisAlignedAssertionError.class, () -> arm.compareAndSwapInt(mis, 0, 1),
                "compareAndSwapInt should throw for misaligned offheap address");
    }

    @Test
    @DisplayName("And set int misaligned mismatch includes tag")
    void testAndSetIntMisalignedMismatchIncludesTag() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long mis = base + 2; // 2 mod 4 -> misaligned for int
        // initial value is 0; expect!=actual triggers error mentioning mis-aligned
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(mis, 1L, /*expected*/ 1, /*value*/ 2));
        assertTrue(ex.getMessage().contains("mis-aligned"),
                "testAndSet failure message should include \"mis-aligned\": " + ex.getMessage());
    }

    @Test
    @DisplayName("Float read write on misaligned address offheap")
    void floatReadWriteOnMisalignedAddressOffheap() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long mis = base + 1; // not 4-byte aligned
        float f = Float.intBitsToFloat(0x7F00FF00);
        arm.writeFloat(mis, f);
        assertEquals(f, arm.readFloat(mis), "float read should return written value at misaligned offheap address");
        // volatile path goes through fence + read
        arm.writeVolatileFloat(mis, f);
        assertEquals(f, arm.readVolatileFloat(mis), "volatile float read should return written value at misaligned offheap address");
    }

    @Test
    @DisplayName("Volatile long on misaligned address offheap")
    void volatileLongOnMisalignedAddressOffheap() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(24);
        long mis = base + 4; // not 8-byte aligned
        long v = 0x7FFF_0000_00FF_F00FL;
        arm.writeVolatileLong(mis, v);
        assertEquals(v, arm.readVolatileLong(mis), "volatile long read should return written value at misaligned offheap address");
    }
}
