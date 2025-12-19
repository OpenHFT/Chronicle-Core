/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemoryTest extends CoreTestCommon {

    @Test
    public void testReadme() {
        @Nullable Memory memory = OS.memory();
        long address = memory.allocate(1024);
        try {
            memory.writeInt(address, 1);
            assertEquals(1, memory.readInt(address), "Expected memory.readInt() to return the value 1 that was just written");
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped, "Expected compareAndSwapInt to succeed when current value (1) matches expected value");
            assertEquals(2, memory.readInt(address), "Expected memory.readInt() to return the swapped value 2 after compareAndSwapInt");
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    public void sizeOf() {
        assertEquals(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE, Memory.sizeOf(boolean.class), "Expected Memory.sizeOf(boolean.class) to match Unsafe.ARRAY_BOOLEAN_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_BYTE_INDEX_SCALE, Memory.sizeOf(byte.class), "Expected Memory.sizeOf(byte.class) to match Unsafe.ARRAY_BYTE_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_CHAR_INDEX_SCALE, Memory.sizeOf(char.class), "Expected Memory.sizeOf(char.class) to match Unsafe.ARRAY_CHAR_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_SHORT_INDEX_SCALE, Memory.sizeOf(short.class), "Expected Memory.sizeOf(short.class) to match Unsafe.ARRAY_SHORT_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_INT_INDEX_SCALE, Memory.sizeOf(int.class), "Expected Memory.sizeOf(int.class) to match Unsafe.ARRAY_INT_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_FLOAT_INDEX_SCALE, Memory.sizeOf(float.class), "Expected Memory.sizeOf(float.class) to match Unsafe.ARRAY_FLOAT_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_DOUBLE_INDEX_SCALE, Memory.sizeOf(double.class), "Expected Memory.sizeOf(double.class) to match Unsafe.ARRAY_DOUBLE_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_LONG_INDEX_SCALE, Memory.sizeOf(long.class), "Expected Memory.sizeOf(long.class) to match Unsafe.ARRAY_LONG_INDEX_SCALE");
        assertEquals(Unsafe.ARRAY_OBJECT_INDEX_SCALE, Memory.sizeOf(Long.class), "Expected Memory.sizeOf(Long.class) to match Unsafe.ARRAY_OBJECT_INDEX_SCALE for object types");
    }
}
