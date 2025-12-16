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
            assertEquals(1, memory.readInt(address), "testReadme: L21");
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped, "testReadme: L23");
            assertEquals(2, memory.readInt(address), "testReadme: L24");
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    public void sizeOf() {
        assertEquals(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE, Memory.sizeOf(boolean.class), "sizeOf: L32");
        assertEquals(Unsafe.ARRAY_BYTE_INDEX_SCALE, Memory.sizeOf(byte.class), "sizeOf: L33");
        assertEquals(Unsafe.ARRAY_CHAR_INDEX_SCALE, Memory.sizeOf(char.class), "sizeOf: L34");
        assertEquals(Unsafe.ARRAY_SHORT_INDEX_SCALE, Memory.sizeOf(short.class), "sizeOf: L35");
        assertEquals(Unsafe.ARRAY_INT_INDEX_SCALE, Memory.sizeOf(int.class), "sizeOf: L36");
        assertEquals(Unsafe.ARRAY_FLOAT_INDEX_SCALE, Memory.sizeOf(float.class), "sizeOf: L37");
        assertEquals(Unsafe.ARRAY_DOUBLE_INDEX_SCALE, Memory.sizeOf(double.class), "sizeOf: L38");
        assertEquals(Unsafe.ARRAY_LONG_INDEX_SCALE, Memory.sizeOf(long.class), "sizeOf: L39");
        assertEquals(Unsafe.ARRAY_OBJECT_INDEX_SCALE, Memory.sizeOf(Long.class), "sizeOf: L40");
    }
}
