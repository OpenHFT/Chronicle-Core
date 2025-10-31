/*
 * Copyright 2016-2025 chronicle.software
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

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ARMMemoryObjectOffsetMisalignmentTest {

    @Test
    void volatileShortOnMisalignedObjectOffset() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[8];
        long off = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 1; // odd = misaligned for short
        arm.writeVolatileShort(bytes, off, (short) 0x1234);
        assertEquals((short) 0x1234, arm.readVolatileShort(bytes, off));
    }

    @Test
    void testAndSetIntObjectAlignedMismatchVsMisaligned() {
        UnsafeMemory.ARMMemory arm = new UnsafeMemory.ARMMemory();
        byte[] bytes = new byte[16];
        long aligned = UnsafeMemory.UNSAFE.arrayBaseOffset(byte[].class) + 4L; // 4-byte aligned
        long mis = aligned + 2; // misaligned
        IllegalStateException alignedMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, aligned, 1, 2));
        assertTrue(alignedMsg.getMessage().contains("Cannot change"));

        IllegalStateException misMsg = assertThrows(IllegalStateException.class,
                () -> arm.testAndSetInt(bytes, mis, 1, 2));
        assertTrue(misMsg.getMessage().contains("mis-aligned"));
    }
}
