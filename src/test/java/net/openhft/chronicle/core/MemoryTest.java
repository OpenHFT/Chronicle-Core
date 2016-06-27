/*
 * Copyright 2016 higherfrequencytrading.com
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

import org.junit.Test;

/**
 * Created by peter on 21/06/15.
 */
public class MemoryTest {
    @Test
    public void testHeapUsed() {
        System.out.println("heap used: " + OS.memory().heapUsed());
    }

    @Test
    public void testReadme() {
        Memory memory = OS.memory();
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
}