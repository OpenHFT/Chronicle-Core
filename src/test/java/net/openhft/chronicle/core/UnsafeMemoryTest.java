/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class UnsafeMemoryTest extends CoreTestCommon {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;

    // These are set up per-test by the helper method
    private UnsafeMemory memory;
    private Boolean onHeap;
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

    private void initParams(String name, UnsafeMemory memory, Boolean onHeap) {
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

    @AfterEach
    public void tearDown() {
        if (object == null && addr != 0) {
            UNSAFE.freeMemory(addr);
            addr = 0;
        }
        // check the state of the heap
        System.gc();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testUnsafeBooleanOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("booleanField"));

        UnsafeMemory.unsafePutBoolean(testObj, offset, true);
        assertTrue(UnsafeMemory.unsafeGetBoolean(testObj, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testUnsafeCharOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        class CharHolder { char value; }
        CharHolder holder = new CharHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(CharHolder.class.getDeclaredField("value"));

        char testChar = 'A';
        UnsafeMemory.unsafePutChar(holder, offset, testChar);
        assertEquals(testChar, UnsafeMemory.unsafeGetChar(holder, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testUnsafeFloatOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        class FloatHolder { float value; }
        FloatHolder holder = new FloatHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(FloatHolder.class.getDeclaredField("value"));

        float testFloat = 1.23f;
        UnsafeMemory.unsafePutFloat(holder, offset, testFloat);
        assertEquals(testFloat, UnsafeMemory.unsafeGetFloat(holder, offset), 0.0f);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testUnsafeDoubleOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("doubleField"));

        double testValue = 123.456;
        UnsafeMemory.unsafePutDouble(testObj, offset, testValue);
        assertEquals(testValue, UnsafeMemory.unsafeGetDouble(testObj, offset), 0.0);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testUnsafeObjectOperations(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        class ObjectHolder { Object value; }
        ObjectHolder holder = new ObjectHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(ObjectHolder.class.getDeclaredField("value"));

        String testObject = "Hello, World!";
        UnsafeMemory.unsafePutObject(holder, offset, testObject);
        assertEquals(testObject, UnsafeMemory.unsafeGetObject(holder, offset));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testWriteReadBytes(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        byte[] originalBytes = {1, 2, 3, 4};
        byte[] buffer = new byte[4];

        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            mem.writeBytes(address, originalBytes, 0, originalBytes.length);

            mem.readBytes(address, buffer, 0, buffer.length);

            assertArrayEquals(originalBytes, buffer);
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testTestAndSetInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            UnsafeMemory.UNSAFE.putInt(address, 0);

            mem.testAndSetInt(address, 0, 0, 10);

            assertEquals(10, UnsafeMemory.UNSAFE.getInt(address));

            assertThrows(IllegalStateException.class, () -> mem.testAndSetInt(address, 0, 0, 20));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testCopy8bitAndIsEqual(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        String testString = "Hello, World!";
        int length = testString.length();

        long address = UnsafeMemory.UNSAFE.allocateMemory(length);

        try {
            mem.copy8bit(testString, 0, length, address);

            assertTrue(mem.isEqual(address, testString, length));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testReadVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            float expectedValue = 12.34f;
            UnsafeMemory.UNSAFE.putFloat(address, expectedValue); // Simulate writing a float

            float actualValue = mem.readVolatileFloat(address);

            assertEquals(expectedValue, actualValue, 0.0f);
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testTestAndSetIntMemoryAddress(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            int expected = 100;
            int newValue = 200;
            UnsafeMemory.UNSAFE.putInt(address, expected);

            mem.testAndSetInt(address, 0, expected, newValue);

            assertEquals(newValue, UnsafeMemory.UNSAFE.getInt(address));

            assertThrows(IllegalStateException.class, () -> mem.testAndSetInt(address, 0, expected, 300));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void testTestAndSetIntObjectField(String name, UnsafeMemory memory, Boolean onHeap) throws NoSuchFieldException {
        initParams(name, memory, onHeap);
        UnsafeMemory mem = UnsafeMemory.INSTANCE;
        TestObject obj = new TestObject();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestObject.class.getDeclaredField("value"));

        int expected = 10;
        int newValue = 20;
        obj.value = expected;

        mem.testAndSetInt(obj, offset, expected, newValue);

        assertEquals(newValue, obj.value);

        assertThrows(IllegalStateException.class, () -> mem.testAndSetInt(obj, offset, expected, 30));
    }

    static class TestObject {
        int value;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++) {
            if (this.onHeap == null) {
                this.memory.writeShort(this.addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, this.memory.readShort(this.addr + i));
            } else {
                this.memory.writeShort(this.object, this.addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, this.memory.readShort(this.object, this.addr + i));
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        if (this.onHeap == null) {
            this.memory.writeLong(this.addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, this.memory.readShort(this.addr));
            assertEquals((short) 0xABCD, this.memory.readShort(this.addr + 1));
        } else {
            this.memory.writeLong(this.object, this.addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, this.memory.readShort(this.object, this.addr));
            assertEquals((short) 0xABCD, this.memory.readShort(this.object, this.addr + 1));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readWriteInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeInt(this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
            } else {
                this.memory.writeInt(this.object, this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeOrderedInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeOrderedInt(this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
            } else {
                this.memory.writeOrderedInt(this.object, this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readWriteLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeLong(this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.addr + i));
            } else {
                this.memory.writeLong(this.object, this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readWriteFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeFloat(this.addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, this.memory.readFloat(this.addr + i), EPSILON);
            } else {
                this.memory.writeFloat(this.object, this.addr + i, 1);
                assertEquals(FLOAT_VAL, this.memory.readFloat(this.object, this.addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readWriteDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeDouble(this.addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, this.memory.readDouble(this.addr + i), EPSILON);
            } else {
                this.memory.writeDouble(this.object, this.addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, this.memory.readDouble(this.object, this.addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeOrderedLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            if (this.onHeap == null) {
                this.memory.writeOrderedLong(this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.addr + i));
            } else {
                this.memory.writeOrderedLong(this.object, this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.object, this.addr + i));
            }
        System.err.println("DONE");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void compareAndSwapInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            try {
                if (this.onHeap == null) {
                    this.memory.writeInt(this.addr + i, 0);
                    final boolean actual = this.memory.compareAndSwapInt(this.addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
                } else {
                    this.memory.writeInt(this.object, this.addr + i, 0);
                    final boolean actual = this.memory.compareAndSwapInt(this.object, this.addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (this.memory.safeAlignedInt(this.addr + i))
                    throw e;
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void compareAndSwapLong(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            try {
                if (this.onHeap == null) {
                    this.memory.writeLong(this.addr + i, 0);
                    final boolean actual = this.memory.compareAndSwapLong(this.addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, this.memory.readLong(this.addr + i));
                } else {
                    this.memory.writeLong(this.object, this.addr + i, 0);
                    final boolean actual = this.memory.compareAndSwapLong(this.object, this.addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, this.memory.readLong(this.object, this.addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (this.memory.safeAlignedLong(this.addr + i))
                    throw e;
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void getAndSetInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(name, memory, onHeap);
        int initialValue = 9876;
        for (int i = 0; i <= 64; i += 4)
            try {
                if (this.onHeap == null) {
                    this.memory.writeInt(this.addr + i, initialValue);
                    final int previous = this.memory.getAndSetInt(this.addr + i, INT_VAL);
                    assertEquals(initialValue, previous);
                    assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
                } else {
                    this.memory.writeInt(this.object, this.addr + i, initialValue);
                    final int previous = this.memory.getAndSetInt(this.object, this.addr + i, INT_VAL);
                    assertEquals(initialValue, previous);
                    assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (this.memory.safeAlignedInt(this.addr + i))
                    throw e;
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileByte(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeByte(this.addr + i, BYTE_VAL);
                final byte actual = this.memory.readVolatileByte(this.addr + i);
                assertEquals(BYTE_VAL, actual);
            } else {
                this.memory.writeByte(this.object, this.addr + i, BYTE_VAL);
                final byte actual = this.memory.readVolatileByte(this.object, this.addr + i);
                assertEquals(BYTE_VAL, actual);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 2)
            if (this.onHeap == null) {
                this.memory.writeShort(this.addr + i, SHORT_VAL);
                final short actual = this.memory.readVolatileShort(this.addr + i);
                assertEquals(SHORT_VAL, actual);
            } else {
                this.memory.writeShort(this.object, this.addr + i, SHORT_VAL);
                final short actual = this.memory.readVolatileShort(this.object, this.addr + i);
                assertEquals(SHORT_VAL, actual);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (this.onHeap == null) {
                this.memory.writeInt(this.addr + i, INT_VAL);
                final int actual = this.memory.readVolatileInt(this.addr + i);
                assertEquals(INT_VAL, actual);
            } else {
                this.memory.writeInt(this.object, this.addr + i, INT_VAL);
                final int actual = this.memory.readVolatileInt(this.object, this.addr + i);
                assertEquals(INT_VAL, actual);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (this.onHeap == null) {
                this.memory.writeFloat(this.addr + i, FLOAT_VAL);
                final float actual = this.memory.readVolatileFloat(this.addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            } else {
                this.memory.writeFloat(this.object, this.addr + i, FLOAT_VAL);
                final float actual = this.memory.readVolatileFloat(this.object, this.addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            if (this.onHeap == null) {
                this.memory.writeLong(this.addr + i, LONG_VAL);
                final long actual = this.memory.readVolatileLong(this.addr + i);
                assertEquals(LONG_VAL, actual);
            } else {
                this.memory.writeLong(this.object, this.addr + i, LONG_VAL);
                final long actual = this.memory.readVolatileLong(this.object, this.addr + i);
                assertEquals(LONG_VAL, actual);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void readVolatileDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            if (this.onHeap == null) {
                this.memory.writeDouble(this.addr + i, DOUBLE_VAL);
                final double actual = this.memory.readVolatileDouble(this.addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            } else {
                this.memory.writeDouble(this.object, this.addr + i, DOUBLE_VAL);
                final double actual = this.memory.readVolatileDouble(this.object, this.addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileByte(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i++)
            if (this.onHeap == null) {
                this.memory.writeVolatileByte(this.addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, this.memory.readByte(this.addr + i));
            } else {
                this.memory.writeVolatileByte(this.object, this.addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, this.memory.readByte(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileShort(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 2)
            if (this.onHeap == null) {
                this.memory.writeVolatileShort(this.addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, this.memory.readShort(this.addr + i));
            } else {
                this.memory.writeVolatileShort(this.object, this.addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, this.memory.readShort(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileInt(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (this.onHeap == null) {
                this.memory.writeVolatileInt(this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
            } else {
                this.memory.writeVolatileInt(this.object, this.addr + i, INT_VAL);
                assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileFloat(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            if (this.onHeap == null) {
                this.memory.writeVolatileFloat(this.addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, this.memory.readFloat(this.addr + i), EPSILON);
            } else {
                this.memory.writeVolatileFloat(this.object, this.addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, this.memory.readFloat(this.object, this.addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileLong(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            if (this.onHeap == null) {
                this.memory.writeVolatileLong(this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.addr + i));
            } else {
                this.memory.writeVolatileLong(this.object, this.addr + i, LONG_VAL);
                assertEquals(LONG_VAL, this.memory.readLong(this.object, this.addr + i));
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void writeVolatileDouble(String name, UnsafeMemory memory, Boolean onHeap) {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            if (this.onHeap == null) {
                this.memory.writeVolatileDouble(this.addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, this.memory.readDouble(this.addr + i), EPSILON);
            } else {
                this.memory.writeVolatileDouble(this.object, this.addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, this.memory.readDouble(this.object, this.addr + i), EPSILON);
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void addInt(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(name, memory, onHeap);
        for (int i = 0; i <= 64; i += 4)
            try {
                if (this.onHeap == null) {
                    this.memory.writeInt(this.addr + i, 0);
                    final int actual = this.memory.addInt(this.addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, this.memory.readInt(this.addr + i));
                } else {
                    this.memory.writeInt(this.object, this.addr + i, 0);
                    final int actual = this.memory.addInt(this.object, this.addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, this.memory.readInt(this.object, this.addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (this.memory.safeAlignedInt(this.addr + i))
                    throw e;
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void addLong(String name, UnsafeMemory memory, Boolean onHeap) throws MisAlignedAssertionError {
        initParams(name, memory, onHeap);
        for (int i = (int) (-this.addr & 7); i <= 64; i += 8)
            try {
                if (this.onHeap == null) {
                    this.memory.writeLong(this.addr + i, 0);
                    final long actual = this.memory.addLong(this.addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, this.memory.readLong(this.addr + i));
                } else {
                    this.memory.writeLong(this.object, this.addr + i, 0);
                    final long actual = this.memory.addLong(this.object, this.addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, this.memory.readLong(this.object, this.addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (this.memory.safeAlignedLong(this.addr + i))
                    throw e;
            }
    }
}
