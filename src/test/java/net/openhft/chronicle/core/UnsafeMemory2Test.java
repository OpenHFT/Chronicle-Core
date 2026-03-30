/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE_COPY_THRESHOLD;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class UnsafeMemory2Test extends CoreTestCommon {
    private static final int INT_VAL = 0x12345678;

    public static Collection<Object[]> data() {
        UnsafeMemory memory1 = new UnsafeMemory();
        UnsafeMemory.ARMMemory memory2 = new UnsafeMemory.ARMMemory();
        Object[][] all = {
                {memory1},
                {memory2}
        };
        Object[][] arm = {
                {memory2}
        };
        return Arrays.asList(Jvm.isArm() ? arm : all);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void stopBitLengthInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        assertEquals(1, memory.stopBitLength(0));
        assertEquals(2, memory.stopBitLength(~0));

        for (int i = 7; i < 32; i += 7) {
            int j = 1 << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1));
            assertEquals(i / 7 + 1, memory.stopBitLength(j));
            assertEquals(i / 7 + 1, memory.stopBitLength(-j));
            assertEquals(i / 7 + 2, memory.stopBitLength(~j));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void stopBitLengthLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        assertEquals(1, memory.stopBitLength(0L));
        assertEquals(2, memory.stopBitLength(~0L));

        for (int i = 7; i < 64; i += 7) {
            long j = 1L << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1));
            assertEquals(i / 7 + 1, memory.stopBitLength(j));
            assertEquals(i / 7 + 1, memory.stopBitLength(-j));
            if (i < 63)
                assertEquals(i / 7 + 2, memory.stopBitLength(~j));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitBytes(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(memory.is7Bit(bytes, 0, i));
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(memory.is7Bit(bytes, 0, i));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitBytes2(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++)
            bytes[i] = (byte) i;
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(bytes, start, length));
            else
                assertEquals(start + length <= 128,
                        memory.is7Bit(bytes, start, length),
                        "start: " + start + ", length: " + length);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitChars(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(memory.is7Bit(chars, 0, i));
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(memory.is7Bit(chars, 0, i));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitChars2(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        char[] chars = new char[512];
        for (int i = 0; i < 512; i++)
            chars[i] = (char) i;
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(512);
            int b = rand.nextInt(512);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(chars, start, length));
            else
                assertEquals(start + length <= 128,
                        memory.is7Bit(chars, start, length),
                        "start: " + start + ", length: " + length);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitAddr(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(memory.is7Bit(addr, 0));
        for (int i = 1; i <= 64; i++) {
            memory.writeByte(addr + i - 1, (byte) -1);
            assertFalse(memory.is7Bit(addr, i));
            memory.writeByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void is7BitAddr2(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final long addr = UNSAFE.allocateMemory(256);
        for (int i = 0; i < 256; i++)
            memory.writeByte(addr + i, (byte) i);

        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(addr + start, length));
            else
                assertEquals(start + length <= 128,
                        memory.is7Bit(addr + start, length),
                        "start: " + start + ", length: " + length);
        }
        UNSAFE.freeMemory(addr);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void partialReadBytes(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (0x10 + i);
        String s8 = Long.toHexString(memory.partialRead(bytes, 0, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(bytes, 0, i));
            assertEquals(s8.substring(16 - i * 2), s);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void partialWriteBytes(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(bytes, 0, value, i);
            long l = memory.partialRead(bytes, 0, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals(Long.toHexString(value & mask), Long.toHexString(l), "i: " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void partialReadAddr(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long addr = memory.allocate(16);
        for (int i = 0; i < 16; i++)
            memory.writeByte(addr + i, (byte) (0x10 + i));
        String s8 = Long.toHexString(memory.partialRead(addr, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(addr, i));
            assertEquals(s8.substring(16 - i * 2), s);
        }
        memory.freeMemory(addr, 16);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void partialWriteAddr(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long addr = memory.allocate(16);
        memory.partialWrite(addr, 0, 8);
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(addr, value, i);
            long l = memory.partialRead(addr, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals(Long.toHexString(value & mask), Long.toHexString(l), "i: " + i);
        }
        memory.freeMemory(addr, 16);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemory(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final int capacity = 37;
        long addr = memory.allocate(capacity);
        long addr2 = memory.allocate(capacity);
        final byte b1 = (byte) 0x7F;
        final byte b2 = (byte) 0x80;
        memory.setMemory(addr2, capacity, b1);
        for (int i = 1; i < capacity - 1; i++) {
            for (int j = i + 1; j < capacity - 1; j++) {
                memory.setMemory(addr, capacity, b2);
                UnsafeMemory.copyMemory(addr2, addr + i, j - i);
                assertEquals(b2, memory.readByte(addr + i - 1));
                assertEquals(b1, memory.readByte(addr + i));
                assertEquals(b1, memory.readByte(addr + j - 1));
                assertEquals(b2, memory.readByte(addr + j));
            }
        }
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryMoreThanThreshold(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final long capacity = (int) (UNSAFE_COPY_THRESHOLD * 2.5d);
        long addr = memory.allocate(capacity);
        long addr2 = memory.allocate(capacity);
        for (int i = 0; i < capacity; i += 4)
            memory.writeInt(addr + i, i);
        final byte b2 = (byte) 0x80;
        memory.setMemory(addr2, capacity, b2);
        memory.copyMemory(addr, addr2, capacity);
        for (int i = 0; i < capacity; i += 4)
            assertEquals(i, memory.readInt(addr2 + i));
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void address(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        assertNotEquals(0, memory.address(ByteBuffer.allocateDirect(32)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void setMemory(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long[] ds = new long[2];
        memory.setMemory(ds, memory.arrayBaseOffset(long[].class), 2 * Long.BYTES, (byte) 1);
        assertEquals(0x0101010101010101L, ds[0]);
        assertEquals(0x0101010101010101L, ds[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryEachWayLongArrayMemory(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final long[] data = new long[]{1, 2, 3, 4};
        final int lengthInBytes = data.length * Long.BYTES;
        final long addr = memory.allocate(lengthInBytes);
        memory.copyMemory(data, memory.arrayBaseOffset(data.getClass()), addr, lengthInBytes);
        final long[] check = new long[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), lengthInBytes);
        assertArrayEquals(data, check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryEachWayByteArrayMemory(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final long addr = memory.allocate(capacity);
        memory.copyMemory(data, 0, addr, capacity);
        final byte[] check = new byte[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryByteArray(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory(data, 0, dest, 0, capacity);
        assertArrayEquals(data, dest);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryByteArrayAsObject(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory((Object) data, memory.arrayBaseOffset(data.getClass()), dest, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, dest);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryEachWayByteArrayLongArray(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        final long[] longs = new long[]{0x0706050403020100L, 0x0f0e0d0c0b0a0908L};
        final long[] copy = new long[longs.length];
        System.arraycopy(longs, 0, copy, 0, longs.length);
        final int lengthInBytes = longs.length * Long.BYTES;
        final byte[] bytes = new byte[lengthInBytes];
        memory.copyMemory(longs, memory.arrayBaseOffset(longs.getClass()), bytes, memory.arrayBaseOffset(bytes.getClass()), lengthInBytes);
        for (int i = 0; i < lengthInBytes; i++)
            assertEquals(i, bytes[i]);
        Arrays.fill(longs, 0);
        memory.copyMemory((Object) bytes, 0, longs, memory.arrayBaseOffset(longs.getClass()), lengthInBytes);
        assertArrayEquals(copy, longs);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryOverlap(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, 0, data, offset, length);
        for (int i = 0; i < offset; i++)
            assertEquals(i, data[i]);
        for (int i = 0; i < length; i++)
            assertEquals(i, data[i + offset]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryOverlapBackwards(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, offset, data, 0, length);
        for (int i = 0; i < length; i++)
            assertEquals(i + offset, data[i]);
        for (int i = length; i < capacity; i++)
            assertEquals(i, data[i]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryHeapObject(UnsafeMemory memory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        MyDTO from = new MyDTO();
        from.num = 99;
        MyDTO to = new MyDTO();
        memory.copyMemory(from, offset, to, offset, Integer.BYTES);
        assertEquals(from.num, to.num);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryEachWayByteArrayHeapObject(UnsafeMemory memory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final byte[] data = new byte[Integer.BYTES];
        data[0] = 98;
        MyDTO to = new MyDTO();

        memory.copyMemory(data, 0, to, offset, Integer.BYTES);

        assertEquals(data[0], to.num);
        to.num = 77;
        memory.copyMemory(to, offset, data, memory.arrayBaseOffset(data.getClass()), Integer.BYTES);
        assertEquals(to.num, data[0]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void copyMemoryEachWayAddressHeapObject(UnsafeMemory memory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final long addr = memory.allocate(Integer.BYTES);
        int expected = 97;
        UnsafeMemory.unsafePutInt(addr, expected);
        MyDTO to = new MyDTO();
        memory.copyMemory(addr, to, offset, Integer.BYTES);
        assertEquals(expected, to.num);
        to.num = 75;
        memory.copyMemory(to, offset, addr, Integer.BYTES);
        assertEquals(to.num, UnsafeMemory.unsafeGetInt(addr));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void safeAlignTest(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        for (int i = -1; i < 70; i++) {
            if (memory instanceof UnsafeMemory.ARMMemory)
                assertEquals(i % 4 == 0, memory.safeAlignedInt(i));
            else
                assertEquals((i & 63) + 4 <= 64, memory.safeAlignedInt(i));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void arrayBaseOffset(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        assertEquals(12, memory.arrayBaseOffset(byte[].class), 4);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void objectFieldOffset(UnsafeMemory memory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, memory.objectFieldOffset(num), 4);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryByte(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeByte(null, mem, (byte) 12);
        assertEquals(12, memory.readByte(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryShort(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeShort(null, mem, (short) 12345);
        assertEquals(12345, memory.readShort(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeInt(null, mem, INT_VAL);
        assertEquals(INT_VAL, memory.readInt(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryAddInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeInt(mem, 0);
        final int actual = memory.addInt(null, mem, INT_VAL);
        assertEquals(INT_VAL, actual);
        assertEquals(INT_VAL, memory.readInt(mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryCASInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeInt(mem, 0);
        final boolean actual = memory.compareAndSwapInt(null, mem, 0, INT_VAL);
        assertTrue(actual);
        assertEquals(INT_VAL, memory.readInt(mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeLong(null, mem, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readLong(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryAddLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeLong(mem, 0);
        final long actual = memory.addLong(null, mem, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, actual);
        assertEquals(Long.MAX_VALUE, memory.readLong(mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryCASLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeLong(mem, 0);
        final boolean actual = memory.compareAndSwapLong(null, mem, 0L, Long.MAX_VALUE);
        assertTrue(actual);
        assertEquals(Long.MAX_VALUE, memory.readLong(mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryFloat(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeFloat(null, mem, 1.2345f);
        assertEquals(1.2345f, memory.readFloat(null, mem), 0f);
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryDouble(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeDouble(null, mem, 1.2345);
        assertEquals(1.2345, memory.readDouble(null, mem), 0f);
        memory.freeMemory(mem, 32);
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryReference1(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.putObject(null, mem, 1.2345));
        } finally {
            memory.freeMemory(mem, 32);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryReference2(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.getObject(null, mem));
        } finally {
            memory.freeMemory(mem, 32);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileByte(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileByte(null, mem, (byte) 12);
        assertEquals(12, memory.readVolatileByte(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileShort(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileShort(null, mem, (short) 12345);
        assertEquals(12345, memory.readVolatileShort(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileInt(null, mem, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryOrderedInt(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeOrderedInt(null, mem, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileLong(null, mem, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryOrderedLong(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeOrderedLong(null, mem, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, mem));
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileFloat(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileFloat(null, mem, 1.2345f);
        assertEquals(1.2345f, memory.readVolatileFloat(null, mem), 0f);
        memory.freeMemory(mem, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void directMemoryVolatileDouble(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        long mem = memory.allocate(32);
        memory.writeVolatileDouble(null, mem, 1.2345);
        assertEquals(1.2345, memory.readVolatileDouble(null, mem), 0f);
        memory.freeMemory(mem, 32);
    }

    static class MyDTO {
        int num;
    }
}
