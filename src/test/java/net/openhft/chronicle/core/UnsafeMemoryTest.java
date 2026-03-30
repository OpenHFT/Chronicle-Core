/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class UnsafeMemoryTest extends CoreTestCommon {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;

    // These are set up per-test by the helper method
    private Object object;
    private long addr;

    private static class TestClass {
        boolean booleanField = false;
        double doubleField = 0.0;
    }

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

    private void initParams(UnsafeMemory memory, Boolean onHeap) {
        if (Boolean.TRUE.equals(onHeap)) {
            object = new byte[128];
            addr = memory.arrayBaseOffset(byte[].class);
        } else {
            object = null;
            addr = UNSAFE.allocateMemory(128);
        }
    }

    @AfterEach
    void tearDown() {
        if (object == null && addr != 0) {
            UNSAFE.freeMemory(addr);
            addr = 0;
        }
        // check the state of the heap
        System.gc();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testUnsafeBooleanOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("booleanField"));

        UnsafeMemory.unsafePutBoolean(testObj, offset, true);
        assertTrue(UnsafeMemory.unsafeGetBoolean(testObj, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testUnsafeCharOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        class CharHolder {
            char value;
        }
        CharHolder holder = new CharHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(CharHolder.class.getDeclaredField("value"));

        char testChar = 'A';
        UnsafeMemory.unsafePutChar(holder, offset, testChar);
        assertEquals(testChar, UnsafeMemory.unsafeGetChar(holder, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testUnsafeFloatOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        class FloatHolder {
            float value;
        }
        FloatHolder holder = new FloatHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(FloatHolder.class.getDeclaredField("value"));

        float testFloat = 1.23f;
        UnsafeMemory.unsafePutFloat(holder, offset, testFloat);
        assertEquals(testFloat, UnsafeMemory.unsafeGetFloat(holder, offset), 0.0f);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testUnsafeDoubleOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("doubleField"));

        double testValue = 123.456;
        UnsafeMemory.unsafePutDouble(testObj, offset, testValue);
        assertEquals(testValue, UnsafeMemory.unsafeGetDouble(testObj, offset), 0.0);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testUnsafeObjectOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        class ObjectHolder {
            Object value;
        }
        ObjectHolder holder = new ObjectHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(ObjectHolder.class.getDeclaredField("value"));

        String testObject = "Hello, World!";
        UnsafeMemory.unsafePutObject(holder, offset, testObject);
        assertEquals(testObject, UnsafeMemory.unsafeGetObject(holder, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testWriteReadBytes(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        byte[] originalBytes = {1, 2, 3, 4};
        byte[] buffer = new byte[4];

        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            memory.writeBytes(address, originalBytes, 0, originalBytes.length);

            memory.readBytes(address, buffer, 0, buffer.length);

            assertArrayEquals(originalBytes, buffer);
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testTestAndSetInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            UnsafeMemory.UNSAFE.putInt(address, 0);

            memory.testAndSetInt(address, 0, 0, 10);

            assertEquals(10, UnsafeMemory.UNSAFE.getInt(address));

            assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(address, 0, 0, 20));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testCopy8bitAndIsEqual(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        String testString = "Hello, World!";
        int length = testString.length();

        long address = UnsafeMemory.UNSAFE.allocateMemory(length);

        try {
            memory.copy8bit(testString, 0, length, address);

            assertTrue(memory.isEqual(address, testString, length));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testReadVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            float expectedValue = 12.34f;
            UnsafeMemory.UNSAFE.putFloat(address, expectedValue); // Simulate writing a float

            float actualValue = memory.readVolatileFloat(address);

            assertEquals(expectedValue, actualValue, 0.0f);
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testTestAndSetIntMemoryAddress(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            int expected = 100;
            int newValue = 200;
            UnsafeMemory.UNSAFE.putInt(address, expected);

            memory.testAndSetInt(address, 0, expected, newValue);

            assertEquals(newValue, UnsafeMemory.UNSAFE.getInt(address));

            assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(address, 0, expected, 300));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void testTestAndSetIntObjectField(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(memory, onHeap);
        TestObject obj = new TestObject();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestObject.class.getDeclaredField("value"));

        int expected = 10;
        int newValue = 20;
        obj.value = expected;

        memory.testAndSetInt(obj, offset, expected, newValue);

        assertEquals(newValue, obj.value);

        assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(obj, offset, expected, 30));
    }

    static class TestObject {
        int value;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readWriteInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeOrderedInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeOrderedInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readWriteLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readWriteFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeFloat(object, addr + i, 1);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readWriteDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeOrderedLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeOrderedLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void compareAndSwapInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void compareAndSwapLong(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void getAndSetInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileByte(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void readVolatileDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileByte(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(addr + i));
            } else {
                memory.writeVolatileByte(object, addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(addr + i));
            } else {
                memory.writeVolatileShort(object, addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeVolatileInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeVolatileFloat(object, addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeVolatileLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void writeVolatileDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(memory, onHeap);
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeVolatileDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void addInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(memory, onHeap);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void addLong(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(memory, onHeap);
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
