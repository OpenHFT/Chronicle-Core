/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTest extends CoreTestCommon {

    @Test
    public void testReadme() {
        @Nullable Memory memory = OS.memory();
        long address = memory.allocate(1024);
        try {
            memory.writeInt(address, 1);
            assertEquals(1, memory.readInt(address));
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped);
            assertEquals(2, memory.readInt(address));
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    public void sizeOf() {
        assertEquals(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE, Memory.sizeOf(boolean.class));
        assertEquals(Unsafe.ARRAY_BYTE_INDEX_SCALE, Memory.sizeOf(byte.class));
        assertEquals(Unsafe.ARRAY_CHAR_INDEX_SCALE, Memory.sizeOf(char.class));
        assertEquals(Unsafe.ARRAY_SHORT_INDEX_SCALE, Memory.sizeOf(short.class));
        assertEquals(Unsafe.ARRAY_INT_INDEX_SCALE, Memory.sizeOf(int.class));
        assertEquals(Unsafe.ARRAY_FLOAT_INDEX_SCALE, Memory.sizeOf(float.class));
        assertEquals(Unsafe.ARRAY_DOUBLE_INDEX_SCALE, Memory.sizeOf(double.class));
        assertEquals(Unsafe.ARRAY_LONG_INDEX_SCALE, Memory.sizeOf(long.class));
        assertEquals(Unsafe.ARRAY_OBJECT_INDEX_SCALE, Memory.sizeOf(Long.class));
    }
}
