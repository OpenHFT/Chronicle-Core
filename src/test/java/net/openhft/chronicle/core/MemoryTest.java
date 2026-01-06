/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryTest extends CoreTestCommon {

    @Test
    @DisplayName("Readme memory example writes reads and swaps values")
    void testReadme() {
        @Nullable Memory memory = OS.memory();
        long address = memory.allocate(1024);
        try {
            memory.writeInt(address, 1);
            assertEquals(1, memory.readInt(address), "memory.readInt(address) should return the value 1 that was written");
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped, "memory.compareAndSwapInt(address, 1, 2) should succeed when the current value is 1");
            assertEquals(2, memory.readInt(address), "memory.readInt(address) should return the swapped value 2 after compareAndSwapInt");
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    @DisplayName("Memory sizeOf reports primitive byte widths")
    void sizeOf() {
        assertEquals(1, Memory.sizeOf(boolean.class), "Memory.sizeOf(boolean.class) should match a single byte");
        assertEquals(Byte.BYTES, Memory.sizeOf(byte.class), "Memory.sizeOf(byte.class) should match Byte.BYTES");
        assertEquals(Character.BYTES, Memory.sizeOf(char.class), "Memory.sizeOf(char.class) should match Character.BYTES");
        assertEquals(Short.BYTES, Memory.sizeOf(short.class), "Memory.sizeOf(short.class) should match Short.BYTES");
        assertEquals(Integer.BYTES, Memory.sizeOf(int.class), "Memory.sizeOf(int.class) should match Integer.BYTES");
        assertEquals(Float.BYTES, Memory.sizeOf(float.class), "Memory.sizeOf(float.class) should match Float.BYTES");
        assertEquals(Double.BYTES, Memory.sizeOf(double.class), "Memory.sizeOf(double.class) should match Double.BYTES");
        assertEquals(Long.BYTES, Memory.sizeOf(long.class), "Memory.sizeOf(long.class) should match Long.BYTES");
        int objectScale = Memory.sizeOf(Long.class);
        assertTrue(objectScale == 4 || objectScale == 8, "Memory.sizeOf(Long.class) should be 4 or 8 bytes on supported JVMs");
    }
}
