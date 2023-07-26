/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UnsafeMemoryTest extends CoreTestCommon {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;
    @Rule
    public final TestName testName = new TestName();

    private final String name;
    private final UnsafeMemory memory;
    private Boolean onHeap;
    private Object object;
    private long addr;

    public UnsafeMemoryTest(String name, UnsafeMemory memory, Boolean onHeap) {
        this.name = name;
        this.memory = memory;
        this.onHeap = onHeap;
        if (Boolean.TRUE.equals(onHeap)) {
            object = new byte[128];
            addr = memory.arrayBaseOffset(byte[].class);
        } else {
            object = null;
            addr = UNSAFE.allocateMemory(128);
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        UnsafeMemory.ARMMemory memory2 = new UnsafeMemory.ARMMemory();
        Object[][] arm = {
                {"ARMMemory onheap", memory2, true},
                {"ARMMemory offheap", memory2, false},
                {"ARMMemory offheap (no object)", memory2, null},
                {"ARMMemory onheap (2)", memory2, true}
        };
        if (Jvm.isArm())
            return Arrays.asList(arm);
        UnsafeMemory memory1 = new UnsafeMemory();
        Object[][] intel = {
                {"UnsafeMemory offheap", memory1, false},
                {"UnsafeMemory onheap", memory1, true},
                {"UnsafeMemory offheap (no object)", memory1, null}};

        List<Object[]> all = new ArrayList<>();
        Collections.addAll(all, intel);
        Collections.addAll(all, arm);
        return all;
    }

    @Before
    public void setUp() {
        System.err.println("testName: " + testName.getMethodName() + ", object: " + object + ", addr: " + addr);
        if (object == null && addr == 0)
            addr = UNSAFE.allocateMemory(128);
    }

    @After
    public void tearDown() {
        if (object == null) {
            UNSAFE.freeMemory(addr);
            addr = 0;
        }
        // check the state of the heap
        System.gc();
    }

    @Test
    public void writeShort() {
        for (int i = 0; i <= 64; i++) {
            if (onHeap == null) {
                memory.writeShort(addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(addr + i));
            } else {
                memory.writeShort(object, addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(object, addr + i));
            }
        }
    }

    @Test
    public void readShort() {
        if (onHeap == null) {
            memory.writeLong(addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(addr));
            assertEquals((short) 0xABCD, memory.readShort(addr + 1));
        } else {
            memory.writeLong(object, addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(object, addr));
            assertEquals((short) 0xABCD, memory.readShort(object, addr + 1));
        }
    }

    @Test
    public void readWriteInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeOrderedInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void readWriteLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @Test
    public void readWriteFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeFloat(object, addr + i, 1);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @Test
    public void readWriteDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @Test
    public void writeOrderedLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeOrderedLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
        System.err.println("DONE");
    }

    @Test
    public void compareAndSwapInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, memory.readInt(addr + i));
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(object, addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @Test
    public void compareAndSwapLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, memory.readLong(addr + i));
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(object, addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    @Test
    public void getAndSetInt() throws MisAlignedAssertionError {
        int initialValue = 9876;
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, initialValue);
                    final int previous = memory.getAndSetInt(addr + i, INT_VAL);
                    assertEquals(initialValue, previous);
                    assertEquals(INT_VAL, memory.readInt(addr + i));
                } else {
                    memory.writeInt(object, addr + i, initialValue);
                    final int previous = memory.getAndSetInt(object, addr + i, INT_VAL);
                    assertEquals(initialValue, previous);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeByte(addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(addr + i);
                assertEquals(BYTE_VAL, actual);
            } else {
                memory.writeByte(object, addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(object, addr + i);
                assertEquals(BYTE_VAL, actual);
            }
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeShort(addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(addr + i);
                assertEquals(SHORT_VAL, actual);
            } else {
                memory.writeShort(object, addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(object, addr + i);
                assertEquals(SHORT_VAL, actual);
            }
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(addr + i);
                assertEquals(INT_VAL, actual);
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(object, addr + i);
                assertEquals(INT_VAL, actual);
            }
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            } else {
                memory.writeFloat(object, addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(object, addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            }
    }

    @Test
    public void readVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(addr + i);
                assertEquals(LONG_VAL, actual);
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(object, addr + i);
                assertEquals(LONG_VAL, actual);
            }
    }

    @Test
    public void readVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(object, addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            }
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(addr + i));
            } else {
                memory.writeVolatileByte(object, addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(object, addr + i));
            }
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(addr + i));
            } else {
                memory.writeVolatileShort(object, addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(object, addr + i));
            }
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeVolatileInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeVolatileFloat(object, addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @Test
    public void writeVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeVolatileLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeVolatileDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @Test
    public void addInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final int actual = memory.addInt(addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, memory.readInt(addr + i));
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final int actual = memory.addInt(object, addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @Test
    public void addLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final long actual = memory.addLong(addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, memory.readLong(addr + i));
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final long actual = memory.addLong(object, addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }
}