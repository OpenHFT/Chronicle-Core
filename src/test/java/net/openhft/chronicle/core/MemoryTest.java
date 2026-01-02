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
            assertEquals(1, memory.readInt(address), "Expected memory.readInt() to return the value 1 that was just written");
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assertTrue(swapped, "Expected compareAndSwapInt to succeed when current value (1) matches expected value");
            assertEquals(2, memory.readInt(address), "Expected memory.readInt() to return the swapped value 2 after compareAndSwapInt");
        } finally {
            memory.freeMemory(address, 1024);
        }
    }

    @Test
    @DisplayName("Memory sizeOf reports primitive byte widths")
    void sizeOf() {
        assertEquals(1, Memory.sizeOf(boolean.class), "Expected Memory.sizeOf(boolean.class) to match a single byte");
        assertEquals(Byte.BYTES, Memory.sizeOf(byte.class), "Expected Memory.sizeOf(byte.class) to match Byte.BYTES");
        assertEquals(Character.BYTES, Memory.sizeOf(char.class), "Expected Memory.sizeOf(char.class) to match Character.BYTES");
        assertEquals(Short.BYTES, Memory.sizeOf(short.class), "Expected Memory.sizeOf(short.class) to match Short.BYTES");
        assertEquals(Integer.BYTES, Memory.sizeOf(int.class), "Expected Memory.sizeOf(int.class) to match Integer.BYTES");
        assertEquals(Float.BYTES, Memory.sizeOf(float.class), "Expected Memory.sizeOf(float.class) to match Float.BYTES");
        assertEquals(Double.BYTES, Memory.sizeOf(double.class), "Expected Memory.sizeOf(double.class) to match Double.BYTES");
        assertEquals(Long.BYTES, Memory.sizeOf(long.class), "Expected Memory.sizeOf(long.class) to match Long.BYTES");
        int objectScale = Memory.sizeOf(Long.class);
        assertTrue(objectScale == 4 || objectScale == 8, "Expected Memory.sizeOf(Long.class) to be 4 or 8 bytes on supported JVMs");
    }
}
