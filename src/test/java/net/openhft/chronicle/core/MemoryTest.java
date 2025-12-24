/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryTest extends CoreTestCommon {

    @Test
    void testReadme() {
        @Nullable Memory memory = OS.memory();
        long address = memory.allocate(1024);
        try {
            memory.writeInt(address, 1);
            assertEquals(1, memory.readInt(address), "Expected memory.readInt() to return the value 1 that was just written");
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped, "CAS int succeeds on match");
            assertEquals(2, memory.readInt(address), "read should return swapped value after CAS succeeds");
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    void sizeOf() {
        assertEquals(1, Memory.sizeOf(boolean.class), "boolean array scale should be 1");
        assertEquals(1, Memory.sizeOf(byte.class), "byte array scale should be 1");
        assertEquals(2, Memory.sizeOf(char.class), "char array scale should be 2");
        assertEquals(2, Memory.sizeOf(short.class), "short array scale should be 2");
        assertEquals(4, Memory.sizeOf(int.class), "int array scale should be 4");
        assertEquals(4, Memory.sizeOf(float.class), "float array scale should be 4");
        assertEquals(8, Memory.sizeOf(double.class), "double array scale should be 8");
        assertEquals(8, Memory.sizeOf(long.class), "long array scale should be 8");
        int objectScale = Memory.sizeOf(Long.class);
        assertTrue(objectScale == 4 || objectScale == 8,
                "object reference scale should be 4 or 8, actual=" + objectScale);
    }
}
