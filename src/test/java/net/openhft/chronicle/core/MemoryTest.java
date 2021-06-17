/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;

public class MemoryTest {

    @Test
    public void testHeapUsed() {
        System.out.println("heap used: " + OS.memory().heapUsed());
    }

    @Test
    public void testReadme() {
        @Nullable Memory memory = OS.memory();
        long address = memory.allocate(1024);
        try {
            memory.writeInt(address, 1);
            assert memory.readInt(address) == 1;
            final boolean swapped = memory.compareAndSwapInt(address, 1, 2);
            assert swapped;
            assert memory.readInt(address) == 2;
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