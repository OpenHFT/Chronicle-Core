/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class UnsafeMemoryTest extends CoreTestCommon {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;
    private static final UnsafeMemory MEMORY = new UnsafeMemory();
    private static final UnsafeMemory.ARMMemory ARM_MEMORY = new UnsafeMemory.ARMMemory();

    private final String caseName;
    private final UnsafeMemory memory;
    private final Boolean onHeap;
    private final Object object;
    private long addr;

    @SuppressWarnings("unused")
    public UnsafeMemoryTest(String name, UnsafeMemory memory, Boolean onHeap) {
        this.caseName = name;
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
            return type == String.class || type == UnsafeMemory.class || type == Boolean.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            Class<?> type = parameterContext.getParameter().getType();
            if (type == String.class)
                return testCase.name;
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

    @TestTemplate
    public void testUnsafeBooleanOperations() throws NoSuchFieldException {
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("booleanField"));

        UnsafeMemory.unsafePutBoolean(testObj, offset, true);
        assertTrue(UnsafeMemory.unsafeGetBoolean(testObj, offset), "testUnsafeBooleanOperations: L99");
    }

    @TestTemplate
    public void testUnsafeCharOperations() throws NoSuchFieldException {
        class CharHolder {
            char value;
        }
        CharHolder holder = new CharHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(CharHolder.class.getDeclaredField("value"));

        char testChar = 'A';
        UnsafeMemory.unsafePutChar(holder, offset, testChar);
        assertEquals(testChar, UnsafeMemory.unsafeGetChar(holder, offset), "testUnsafeCharOperations: L112");
    }

    @TestTemplate
    public void testUnsafeFloatOperations() throws NoSuchFieldException {
        class FloatHolder {
            float value;
        }
        FloatHolder holder = new FloatHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(FloatHolder.class.getDeclaredField("value"));

        float testFloat = 1.23f;
        UnsafeMemory.unsafePutFloat(holder, offset, testFloat);
        assertEquals(testFloat, UnsafeMemory.unsafeGetFloat(holder, offset), 0.0f, "testUnsafeFloatOperations: L125");
    }

    @TestTemplate
    public void testUnsafeDoubleOperations() throws NoSuchFieldException {
        TestClass testObj = new TestClass();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestClass.class.getDeclaredField("doubleField"));

        double testValue = 123.456;
        UnsafeMemory.unsafePutDouble(testObj, offset, testValue);
        assertEquals(testValue, UnsafeMemory.unsafeGetDouble(testObj, offset), 0.0, "testUnsafeDoubleOperations: L135");
    }

    @TestTemplate
    public void testUnsafeObjectOperations() throws NoSuchFieldException {
        class ObjectHolder {
            Object value;
        }
        ObjectHolder holder = new ObjectHolder();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(ObjectHolder.class.getDeclaredField("value"));

        String testObject = "Hello, World!";
        UnsafeMemory.unsafePutObject(holder, offset, testObject);
        assertEquals(testObject, UnsafeMemory.unsafeGetObject(holder, offset), "testUnsafeObjectOperations: L148");
    }

    @TestTemplate
    public void testWriteReadBytes() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        byte[] originalBytes = {1, 2, 3, 4};
        byte[] buffer = new byte[4];

        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            memory.writeBytes(address, originalBytes, 0, originalBytes.length);

            memory.readBytes(address, buffer, 0, buffer.length);

            assertArrayEquals(originalBytes, buffer, "testWriteReadBytes: L164");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @TestTemplate
    public void testTestAndSetInt() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            UnsafeMemory.UNSAFE.putInt(address, 0);

            memory.testAndSetInt(address, 0, 0, 10);

            assertEquals(10, UnsafeMemory.UNSAFE.getInt(address), "testTestAndSetInt: L180");

            assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(address, 0, 0, 20));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @TestTemplate
    public void testCopy8bitAndIsEqual() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        String testString = "Hello, World!";
        int length = testString.length();

        long address = UnsafeMemory.UNSAFE.allocateMemory(length);

        try {
            memory.copy8bit(testString, 0, length, address);

            assertTrue(memory.isEqual(address, testString, length), "testCopy8bitAndIsEqual: L199");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @TestTemplate
    public void testReadVolatileFloat() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            float expectedValue = 12.34f;
            UnsafeMemory.UNSAFE.putFloat(address, expectedValue); // Simulate writing a float

            float actualValue = memory.readVolatileFloat(address);

            assertEquals(expectedValue, actualValue, 0.0f, "testReadVolatileFloat: L216");
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @TestTemplate
    public void testTestAndSetIntMemoryAddress() {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        long address = UnsafeMemory.UNSAFE.allocateMemory(4);

        try {
            int expected = 100;
            int newValue = 200;
            UnsafeMemory.UNSAFE.putInt(address, expected);

            memory.testAndSetInt(address, 0, expected, newValue);

            assertEquals(newValue, UnsafeMemory.UNSAFE.getInt(address), "testTestAndSetIntMemoryAddress: L234");

            assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(address, 0, expected, 300));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(address);
        }
    }

    @TestTemplate
    public void testTestAndSetIntObjectField() throws NoSuchFieldException {
        UnsafeMemory memory = UnsafeMemory.INSTANCE;
        TestObject obj = new TestObject();
        long offset = UnsafeMemory.UNSAFE.objectFieldOffset(TestObject.class.getDeclaredField("value"));

        int expected = 10;
        int newValue = 20;
        obj.value = expected;

        memory.testAndSetInt(obj, offset, expected, newValue);

        assertEquals(newValue, obj.value, "testTestAndSetIntObjectField: L254");

        assertThrows(IllegalStateException.class, () -> memory.testAndSetInt(obj, offset, expected, 30));
    }

    @TestTemplate
    public void writeShort() {
        for (int i = 0; i <= 64; i++) {
            if (onHeap == null) {
                memory.writeShort(addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(addr + i), "writeShort: L264");
            } else {
                memory.writeShort(object, addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(object, addr + i), "writeShort: L267");
            }
        }
    }

    @TestTemplate
    public void readShort() {
        if (onHeap == null) {
            memory.writeLong(addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(addr), "readShort: L276");
            assertEquals((short) 0xABCD, memory.readShort(addr + 1), "readShort: L277");
        } else {
            memory.writeLong(object, addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(object, addr), "readShort: L280");
            assertEquals((short) 0xABCD, memory.readShort(object, addr + 1), "readShort: L281");
        }
    }

    @TestTemplate
    public void readWriteInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "readWriteInt: L290");
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "readWriteInt: L293");
            }
    }

    @TestTemplate
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "writeOrderedInt: L302");
            } else {
                memory.writeOrderedInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "writeOrderedInt: L305");
            }
    }

    @TestTemplate
    public void readWriteLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "readWriteLong: L314");
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "readWriteLong: L317");
            }
    }

    @TestTemplate
    public void readWriteFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON, "readWriteFloat: L326");
            } else {
                memory.writeFloat(object, addr + i, 1);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON, "readWriteFloat: L329");
            }
    }

    @TestTemplate
    public void readWriteDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON, "readWriteDouble: L338");
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON, "readWriteDouble: L341");
            }
    }

    @TestTemplate
    public void writeOrderedLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "writeOrderedLong: L350");
            } else {
                memory.writeOrderedLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "writeOrderedLong: L353");
            }
        System.err.println("DONE");
    }

    @TestTemplate
    public void compareAndSwapInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(addr + i, 0, INT_VAL);
                    assertTrue(actual, "compareAndSwapInt: L365");
                    assertEquals(INT_VAL, memory.readInt(addr + i), "compareAndSwapInt: L366");
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(object, addr + i, 0, INT_VAL);
                    assertTrue(actual, "compareAndSwapInt: L370");
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "compareAndSwapInt: L371");
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @TestTemplate
    public void compareAndSwapLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(addr + i, 0, LONG_VAL);
                    assertTrue(actual, "compareAndSwapLong: L386");
                    assertEquals(LONG_VAL, memory.readLong(addr + i), "compareAndSwapLong: L387");
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(object, addr + i, 0, LONG_VAL);
                    assertTrue(actual, "compareAndSwapLong: L391");
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i), "compareAndSwapLong: L392");
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    @TestTemplate
    public void getAndSetInt() throws MisAlignedAssertionError {
        int initialValue = 9876;
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, initialValue);
                    final int previous = memory.getAndSetInt(addr + i, INT_VAL);
                    assertEquals(initialValue, previous, "getAndSetInt: L408");
                    assertEquals(INT_VAL, memory.readInt(addr + i), "getAndSetInt: L409");
                } else {
                    memory.writeInt(object, addr + i, initialValue);
                    final int previous = memory.getAndSetInt(object, addr + i, INT_VAL);
                    assertEquals(initialValue, previous, "getAndSetInt: L413");
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "getAndSetInt: L414");
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @TestTemplate
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeByte(addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(addr + i);
                assertEquals(BYTE_VAL, actual, "readVolatileByte: L428");
            } else {
                memory.writeByte(object, addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(object, addr + i);
                assertEquals(BYTE_VAL, actual, "readVolatileByte: L432");
            }
    }

    @TestTemplate
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeShort(addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(addr + i);
                assertEquals(SHORT_VAL, actual, "readVolatileShort: L442");
            } else {
                memory.writeShort(object, addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(object, addr + i);
                assertEquals(SHORT_VAL, actual, "readVolatileShort: L446");
            }
    }

    @TestTemplate
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(addr + i);
                assertEquals(INT_VAL, actual, "readVolatileInt: L456");
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(object, addr + i);
                assertEquals(INT_VAL, actual, "readVolatileInt: L460");
            }
    }

    @TestTemplate
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON, "readVolatileFloat: L470");
            } else {
                memory.writeFloat(object, addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(object, addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON, "readVolatileFloat: L474");
            }
    }

    @TestTemplate
    public void readVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(addr + i);
                assertEquals(LONG_VAL, actual, "readVolatileLong: L484");
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(object, addr + i);
                assertEquals(LONG_VAL, actual, "readVolatileLong: L488");
            }
    }

    @TestTemplate
    public void readVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON, "readVolatileDouble: L498");
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(object, addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON, "readVolatileDouble: L502");
            }
    }

    @TestTemplate
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(addr + i), "writeVolatileByte: L511");
            } else {
                memory.writeVolatileByte(object, addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(object, addr + i), "writeVolatileByte: L514");
            }
    }

    @TestTemplate
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(addr + i), "writeVolatileShort: L523");
            } else {
                memory.writeVolatileShort(object, addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(object, addr + i), "writeVolatileShort: L526");
            }
    }

    @TestTemplate
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i), "writeVolatileInt: L535");
            } else {
                memory.writeVolatileInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i), "writeVolatileInt: L538");
            }
    }

    @TestTemplate
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON, "writeVolatileFloat: L547");
            } else {
                memory.writeVolatileFloat(object, addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON, "writeVolatileFloat: L550");
            }
    }

    @TestTemplate
    public void writeVolatileLong() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i), "writeVolatileLong: L559");
            } else {
                memory.writeVolatileLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i), "writeVolatileLong: L562");
            }
    }

    @TestTemplate
    public void writeVolatileDouble() {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON, "writeVolatileDouble: L571");
            } else {
                memory.writeVolatileDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON, "writeVolatileDouble: L574");
            }
    }

    @TestTemplate
    public void addInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final int actual = memory.addInt(addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual, "addInt: L585");
                    assertEquals(INT_VAL, memory.readInt(addr + i), "addInt: L586");
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final int actual = memory.addInt(object, addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual, "addInt: L590");
                    assertEquals(INT_VAL, memory.readInt(object, addr + i), "addInt: L591");
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @TestTemplate
    public void addLong() throws MisAlignedAssertionError {
        for (int i = (int) (-addr & 7); i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final long actual = memory.addLong(addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual, "addLong: L606");
                    assertEquals(LONG_VAL, memory.readLong(addr + i), "addLong: L607");
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final long actual = memory.addLong(object, addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual, "addLong: L611");
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i), "addLong: L612");
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    private static class TestClass {
        final boolean booleanField = false;
        final double doubleField = 0.0;
    }

    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestObject {
        int value;
    }
}
