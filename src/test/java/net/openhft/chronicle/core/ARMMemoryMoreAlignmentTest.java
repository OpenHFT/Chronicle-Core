//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ARMMemoryMoreAlignmentTest {

    private long allocated;

    private long alloc(int bytes) {
        allocated = UnsafeMemory.UNSAFE.allocateMemory(bytes);
        for (int i = 0; i < bytes; i++) UnsafeMemory.UNSAFE.putByte(allocated + i, (byte) 0);
        return allocated;
    }

    @AfterEach
    void tearDown() {
        if (allocated != 0) UnsafeMemory.UNSAFE.freeMemory(allocated);
        allocated = 0;
    }

    @Test
    void volatileCharMisalignedObjectOffset() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[8];
        long off = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 1; // odd offset
        arm.writeVolatileShort(bytes, off, (short) 0x1234);
        assertEquals((short) 0x1234, arm.readVolatileShort(bytes, off));
    }

    @Test
    void getAndSetAndCasAlignedObjectOffset() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[16];
        long aligned = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 4L;
        // initial 0, CAS expect 1 fails returns false
        assertFalse(arm.compareAndSwapInt(bytes, aligned, 1, 2));
        // getAndSet returns previous and sets new value
        assertEquals(0, arm.getAndSetInt(bytes, aligned, 7));
        assertTrue(arm.compareAndSwapInt(bytes, aligned, 7, 9));
        assertEquals(9, UnsafeMemory.UNSAFE.getInt(bytes, aligned));
    }

    @Test
    void writeOrderedLongMisalignedAddress() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        long base = alloc(16);
        long mis = base + 4; // not 8-byte aligned
        long v = 0x0102_0304_0506_0708L;
        arm.writeOrderedLong(mis, v);
        assertEquals(v, arm.readLong(mis));
    }
}
