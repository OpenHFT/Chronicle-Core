/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.*;
import java.util.stream.Stream;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
@ExtendWith(UnsafeMemoryTest.UnsafeMemoryTemplateProvider.class)
class UnsafeMemoryTest extends CoreTestCommon {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;
    private static final UnsafeMemory MEMORY = new UnsafeMemory();
    private static final UnsafeMemory.ARMMemory ARM_MEMORY = new UnsafeMemory.ARMMemory();

    private final UnsafeMemory memory;
    private final Boolean onHeap;
    private final Object object;
    private long addr;

    public UnsafeMemoryTest(UnsafeMemory memory, Boolean onHeap) {
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

    private static Stream<UnsafeMemoryCase> cases() {
        UnsafeMemoryCase[] arm = {
                new UnsafeMemoryCase("ARMMemory onheap", ARM_MEMORY, true),
                new UnsafeMemoryCase("ARMMemory offheap", ARM_MEMORY, false),
                new UnsafeMemoryCase("ARMMemory offheap (no object)", ARM_MEMORY, null),
                new UnsafeMemoryCase("ARMMemory onheap (2)", ARM_MEMORY, true),
        };
        if (Jvm.isArm()) {
            return Arrays.stream(arm);
        }
        UnsafeMemoryCase[] intel = {
                new UnsafeMemoryCase("UnsafeMemory offheap", MEMORY, false),
                new UnsafeMemoryCase("UnsafeMemory onheap", MEMORY, true),
                new UnsafeMemoryCase("UnsafeMemory offheap (no object)", MEMORY, null),
        };
        return Stream.concat(Arrays.stream(intel), Arrays.stream(arm));
    }

    private static final class UnsafeMemoryCase {
        private final String name;
        private final UnsafeMemory memory;
        private final Boolean onHeap;

        private UnsafeMemoryCase(String name, UnsafeMemory memory, Boolean onHeap) {
            this.name = name;
            this.memory = memory;
            this.onHeap = onHeap;
        }
    }

    static final class UnsafeMemoryTemplateProvider implements TestTemplateInvocationContextProvider {
        @Override
        public boolean supportsTestTemplate(ExtensionContext context) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
            return cases().map(UnsafeMemoryInvocationContext::new);
        }
    }

    private static final class UnsafeMemoryInvocationContext implements TestTemplateInvocationContext {
        private final UnsafeMemoryCase testCase;

        private UnsafeMemoryInvocationContext(UnsafeMemoryCase testCase) {
            this.testCase = testCase;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return testCase.name;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.singletonList(new UnsafeMemoryParameterResolver(testCase));
        }
    }

    private static final class UnsafeMemoryParameterResolver implements ParameterResolver {
        private final UnsafeMemoryCase testCase;

        private UnsafeMemoryParameterResolver(UnsafeMemoryCase testCase) {
            this.testCase = testCase;
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            Class<?> type = parameterContext.getParameter().getType();
            return type == UnsafeMemory.class || type == Boolean.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            Class<?> type = parameterContext.getParameter().getType();
            if (type == UnsafeMemory.class)
                return testCase.memory;
            if (type == Boolean.class)
                return testCase.onHeap;
            throw new IllegalArgumentException("Unsupported parameter: " + parameterContext.getParameter());
        }
    }

    @BeforeEach
    public void setUp() {
        if (object == null && addr == 0)
            addr = UNSAFE.allocateMemory(128);
    }

    @AfterEach
    public void tearDown() {
        if (object == null) {
            UNSAFE.freeMemory(addr);
            addr = 0;
        }
        // check the state of the heap
        System.gc();
    }

    @DisplayName("Unsafe boolean operations round trip written values")
    @TestTemplate
    void testUnsafeBooleanOperations() throws NoSuchFieldException {
        SampleClass testObj = new SampleClass();
        assertFalse(testObj.booleanField, "initial boolean field should be false");
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(SampleClass.class.getDeclaredField("booleanField"));

        UnsafeMemory.unsafePutBoolean(testObj, offset, true);
        assertTrue(UnsafeMemory.unsafeGetBoolean(testObj, offset), "unsafe boolean get should return true after putting true");
    }

    @DisplayName("Unsafe char operations round trip written values")
    @TestTemplate
    void testUnsafeCharOperations() throws NoSuchFieldException {
        class CharHolder {
            char value;
        }
        CharHolder holder = new CharHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(CharHolder.class.getDeclaredField("value"));

        char testChar = 'A';
        UnsafeMemory.unsafePutChar(holder, offset, testChar);
        assertEquals(testChar, UnsafeMemory.unsafeGetChar(holder, offset), "unsafe char get should return written char value");
    }

    @DisplayName("Unsafe float operations round trip written values")
    @TestTemplate
    void testUnsafeFloatOperations() throws NoSuchFieldException {
        class FloatHolder {
            float value;
        }
        FloatHolder holder = new FloatHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(FloatHolder.class.getDeclaredField("value"));

        float testFloat = 1.23f;
        UnsafeMemory.unsafePutFloat(holder, offset, testFloat);
        assertEquals(testFloat, UnsafeMemory.unsafeGetFloat(holder, offset), 0.0f, "unsafe float get should return written float value");
    }

    @DisplayName("Unsafe double operations round trip written values")
    @TestTemplate
    void testUnsafeDoubleOperations() throws NoSuchFieldException {
        SampleClass testObj = new SampleClass();
        assertEquals(0.0, testObj.doubleField, 0.0, "initial double field should be 0.0");
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(SampleClass.class.getDeclaredField("doubleField"));

        double testValue = 123.456;
        UnsafeMemory.unsafePutDouble(testObj, offset, testValue);
        assertEquals(testValue, UnsafeMemory.unsafeGetDouble(testObj, offset), 0.0, "unsafe double get should return written double value");
    }

    @DisplayName("Unsafe object operations round trip written references")
    @TestTemplate
    void testUnsafeObjectOperations() throws NoSuchFieldException {
        class ObjectHolder {
            Object value;
        }
        ObjectHolder holder = new ObjectHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(ObjectHolder.class.getDeclaredField("value"));

        String testObject = "Hello, World!";
        UnsafeMemory.unsafePutObject(holder, offset, testObject);
        assertEquals(testObject, UnsafeMemory.unsafeGetObject(holder, offset), "unsafe object get should return written object reference");
    }

    @DisplayName("Write and read bytes round trip values")
    @TestTemplate
    void testWriteReadBytes() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        byte[] originalBytes = {1, 2, 3, 4};
        byte[] buffer = new byte[4];

        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            memory.writeBytes(address, originalBytes, 0, originalBytes.length);

            memory.readBytes(address, buffer, 0, buffer.length);

            assertArrayEquals(originalBytes, buffer, "arrays should contain identical elements");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @DisplayName("Atomic int set returns expected previous value for address")
    @TestTemplate
    void testTestAndSetInt() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            UnsafeMemory.UNSAFE.putInt(address, 0);

            memory.testAndSetInt(address, 0, 0, 10);

            assertEquals(10, UnsafeMemory.UNSAFE.getInt(address), "testAndSetInt should update value when expected matches");

            assertThrows(IllegalStateException.class,
                    () -> memory.testAndSetInt(address, 0, 0, 20),
                    "testAndSetInt should throw for address when expected value mismatches");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @DisplayName("Copy 8 bit values and compare equality")
    @TestTemplate
    void testCopy8bitAndIsEqual() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        String testString = "Hello, World!";
        int length = testString.length();

        long address = UnsafeMemory.UNSAFE.allocateMemory(length);

        try {
            memory.copy8bit(testString, 0, length, address);

            assertTrue(memory.isEqual(address, testString, length), "isEqual should return true after copy8bit copies matching content");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @DisplayName("Volatile float read returns latest written value for allocated address")
    @TestTemplate
    void testReadVolatileFloat() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            float expectedValue = 12.34f;
            UnsafeMemory.UNSAFE.putFloat(address, expectedValue); // Simulate writing a float

            float actualValue = memory.readVolatileFloat(address);

            assertEquals(expectedValue, actualValue, 0.0f, "readVolatileFloat should return previously written value");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @DisplayName("Atomic int set at memory address returns expected value")
    @TestTemplate
    void testTestAndSetIntMemoryAddress() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            int expected = 100;
            int newValue = 200;
            UnsafeMemory.UNSAFE.putInt(address, expected);

            memory.testAndSetInt(address, 0, expected, newValue);

            assertEquals(newValue, UnsafeMemory.UNSAFE.getInt(address), "testAndSetInt should update memory to new value");

            assertThrows(IllegalStateException.class,
                    () -> memory.testAndSetInt(address, 0, expected, 300),
                    "testAndSetInt should throw for allocated address when expected value mismatches");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @DisplayName("Atomic int set on object field returns expected value")
    @TestTemplate
    void testTestAndSetIntObjectField() throws NoSuchFieldException {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        SampleObject obj = new SampleObject();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(SampleObject.class.getDeclaredField("value"));

        int expected = 10;
        int newValue = 20;
        obj.value = expected;

        memory.testAndSetInt(obj, offset, expected, newValue);

        assertEquals(newValue, obj.value, "testAndSetInt should update object field when current value matches expected");

        assertThrows(IllegalStateException.class,
                () -> memory.testAndSetInt(obj, offset, expected, 30),
                "testAndSetInt should throw for object field when expected value mismatches");
    }

    @DisplayName("Write short stores value correctly in memory")
    @TestTemplate
    void writeShort() {
        for (int i = 0; i <= 64; i++) {
            if (onHeap == null) {
                memory.writeShort(addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(addr + i), "off-heap writeShort should store value readable at offset at i=" + i);
            } else {
                memory.writeShort(object, addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(object, addr + i), "on-heap writeShort should store value readable at offset at i=" + i);
            }
        }
    }

    @DisplayName("Read short returns stored value from off-heap memory")
    @TestTemplate
    void readShort() {
        if (onHeap == null) {
            memory.writeLong(addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(addr), "off-heap readShort at offset 0 should read first written value");
            assertEquals((short) 0xABCD, memory.readShort(addr + 1), "off-heap readShort at offset 1 should read second written value");
        } else {
            memory.writeLong(object, addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(object, addr), "on-heap readShort at offset 0 should read first written value");
            assertEquals((short) 0xABCD, memory.readShort(object, addr + 1), "on-heap readShort at offset 1 should read second written value");
        }
    }

    @DisplayName("Read and write int round trip values")
    @TestTemplate
    void readWriteInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap int read should return written value at i=" + i);
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap int read should return written value at i=" + i);
            }
    }

    @DisplayName("Ordered int write stores value correctly in memory")
    @TestTemplate
    void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap ordered int write should be visible to read at i=" + i);
            } else {
                memory.writeOrderedInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap ordered int write should be visible to read at i=" + i);
            }
    }

    @DisplayName("Read and write long round trip values")
    @TestTemplate
    void readWriteLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "off-heap long read should return written value at i=" + i);
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "on-heap long read should return written value at i=" + i);
            }
    }

    @DisplayName("Read and write float round trip values")
    @TestTemplate
    void readWriteFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON, "off-heap float read should return written value at i=" + i);
            } else {
                memory.writeFloat(object, addr + i, 1);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON, "on-heap float read should return written value at i=" + i);
            }
    }

    @DisplayName("Read and write double round trip values")
    @TestTemplate
    void readWriteDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON, "off-heap double read should return written value at i=" + i);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON, "on-heap double read should return written value at i=" + i);
            }
    }

    @DisplayName("Ordered long write stores value correctly in memory")
    @TestTemplate
    void writeOrderedLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "off-heap ordered long write should be visible to read at i=" + i);
            } else {
                memory.writeOrderedLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "on-heap ordered long write should be visible to read at i=" + i);
            }
        System.err.println("DONE");
    }

    @DisplayName("Compare and swap int updates expected value")
    @TestTemplate
    void compareAndSwapInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(addr + i, 0, INT_VAL);
                    assertTrue(actual, "off-heap compareAndSwapInt should succeed with matching expected value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap CAS int should update value when expected matches at i=" + i);
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(object, addr + i, 0, INT_VAL);
                    assertTrue(actual, "on-heap compareAndSwapInt should succeed with matching expected value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap CAS int should update value when expected matches at i=" + i);
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @DisplayName("Compare and swap long updates expected value")
    @TestTemplate
    void compareAndSwapLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(addr + i, 0, LONG_VAL);
                    assertTrue(actual, "off-heap compareAndSwapLong should succeed with matching expected value at i=" + i);
                    assertEquals(LONG_VAL, memory.readLong(addr + i), "off-heap CAS long should update value when expected matches at i=" + i);
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(object, addr + i, 0, LONG_VAL);
                    assertTrue(actual, "on-heap compareAndSwapLong should succeed with matching expected value at i=" + i);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i), "on-heap CAS long should update value when expected matches at i=" + i);
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    @DisplayName("Get and set int returns previous stored value behaviour under expected input and output conditions")
    @TestTemplate
    void getAndSetInt() throws MisAlignedAssertionError {
        int initialValue = 9876;
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, initialValue);
                    final int previous = memory.getAndSetInt(addr + i, INT_VAL);
                    assertEquals(initialValue, previous, "off-heap getAndSetInt should return previous value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap getAndSetInt should update memory to new value at i=" + i);
                } else {
                    memory.writeInt(object, addr + i, initialValue);
                    final int previous = memory.getAndSetInt(object, addr + i, INT_VAL);
                    assertEquals(initialValue, previous, "on-heap getAndSetInt should return previous value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap getAndSetInt should update memory to new value at i=" + i);
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @DisplayName("Volatile byte read returns latest written value")
    @TestTemplate
    void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeByte(addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(addr + i);
                assertEquals(BYTE_VAL, actual, "off-heap readVolatileByte should return written value at i=" + i);
            } else {
                memory.writeByte(object, addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(object, addr + i);
                assertEquals(BYTE_VAL, actual, "on-heap readVolatileByte should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile short read returns latest written value")
    @TestTemplate
    void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeShort(addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(addr + i);
                assertEquals(SHORT_VAL, actual, "off-heap readVolatileShort should return written value at i=" + i);
            } else {
                memory.writeShort(object, addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(object, addr + i);
                assertEquals(SHORT_VAL, actual, "on-heap readVolatileShort should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile int read returns latest written value")
    @TestTemplate
    void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(addr + i);
                assertEquals(INT_VAL, actual, "off-heap readVolatileInt should return written value at i=" + i);
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(object, addr + i);
                assertEquals(INT_VAL, actual, "on-heap readVolatileInt should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile float read returns latest written value")
    @TestTemplate
    void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON, "off-heap readVolatileFloat should return written value at i=" + i);
            } else {
                memory.writeFloat(object, addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(object, addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON, "on-heap readVolatileFloat should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile long read returns latest written value")
    @TestTemplate
    void readVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(addr + i);
                assertEquals(LONG_VAL, actual, "off-heap readVolatileLong should return written value at i=" + i);
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(object, addr + i);
                assertEquals(LONG_VAL, actual, "on-heap readVolatileLong should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile double read returns latest written value")
    @TestTemplate
    void readVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON, "off-heap readVolatileDouble should return written value at i=" + i);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(object, addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON, "on-heap readVolatileDouble should return written value at i=" + i);
            }
    }

    @DisplayName("Volatile byte write stores value correctly")
    @TestTemplate
    void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(addr + i), "off-heap writeVolatileByte should be visible to read at i=" + i);
            } else {
                memory.writeVolatileByte(object, addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(object, addr + i), "on-heap writeVolatileByte should be visible to read at i=" + i);
            }
    }

    @DisplayName("Volatile short write stores value correctly")
    @TestTemplate
    void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(addr + i), "off-heap writeVolatileShort should be visible to read at i=" + i);
            } else {
                memory.writeVolatileShort(object, addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(object, addr + i), "on-heap writeVolatileShort should be visible to read at i=" + i);
            }
    }

    @DisplayName("Volatile int write stores value correctly")
    @TestTemplate
    void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap writeVolatileInt should be visible to read at i=" + i);
            } else {
                memory.writeVolatileInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap writeVolatileInt should be visible to read at i=" + i);
            }
    }

    @DisplayName("Volatile float write stores value correctly")
    @TestTemplate
    void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON, "off-heap writeVolatileFloat should be visible to read at i=" + i);
            } else {
                memory.writeVolatileFloat(object, addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON, "on-heap writeVolatileFloat should be visible to read at i=" + i);
            }
    }

    @DisplayName("Volatile long write stores value correctly")
    @TestTemplate
    void writeVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "off-heap writeVolatileLong should be visible to read at i=" + i);
            } else {
                memory.writeVolatileLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "on-heap writeVolatileLong should be visible to read at i=" + i);
            }
    }

    @DisplayName("Volatile double write stores value correctly")
    @TestTemplate
    void writeVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON, "off-heap writeVolatileDouble should be visible to read at i=" + i);
            } else {
                memory.writeVolatileDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON, "on-heap writeVolatileDouble should be visible to read at i=" + i);
            }
    }

    @DisplayName("Add int updates value at offset")
    @TestTemplate
    void addInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final int actual = memory.addInt(addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual, "off-heap addInt should return updated value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(addr + i), "off-heap addInt should update memory to new value at i=" + i);
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final int actual = memory.addInt(object, addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual, "on-heap addInt should return updated value at i=" + i);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "on-heap addInt should update memory to new value at i=" + i);
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @DisplayName("Add long updates value at offset")
    @TestTemplate
    void addLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final long actual = memory.addLong(addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual, "off-heap addLong should return updated value at i=" + i);
                    assertEquals(LONG_VAL, memory.readLong(addr + i), "off-heap addLong should update memory to new value at i=" + i);
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final long actual = memory.addLong(object, addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual, "on-heap addLong should return updated value at i=" + i);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i), "on-heap addLong should update memory to new value at i=" + i);
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    private static class SampleClass {
        boolean booleanField;
        double doubleField;
    }

    static class SampleObject {
        int value;
    }
}
