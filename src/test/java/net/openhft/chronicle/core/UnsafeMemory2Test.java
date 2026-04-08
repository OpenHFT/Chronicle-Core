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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class UnsafeMemory2Test extends CoreTestCommon {
    private static final int INT_VAL = 0x12345678;
    private UnsafeMemory memory;

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
    public void stopBitLengthInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void stopBitLengthLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitBytes(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitBytes2(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitChars(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitChars2(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitAddr(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void is7BitAddr2(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void partialReadBytes(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void partialWriteBytes(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void partialReadAddr(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void partialWriteAddr(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemory(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryMoreThanThreshold(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void address(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        assertNotEquals(0, memory.address(ByteBuffer.allocateDirect(32)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void setMemory(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long[] ds = new long[2];
        memory.setMemory(ds, memory.arrayBaseOffset(long[].class), 2 * Long.BYTES, (byte) 1);
        assertEquals(0x0101010101010101L, ds[0]);
        assertEquals(0x0101010101010101L, ds[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void copyMemoryEachWayLongArrayMemory(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryEachWayByteArrayMemory(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryByteArray(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryByteArrayAsObject(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryEachWayByteArrayLongArray(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryOverlap(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryOverlapBackwards(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryHeapObject(UnsafeMemory unsafeMemory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryEachWayByteArrayHeapObject(UnsafeMemory unsafeMemory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void copyMemoryEachWayAddressHeapObject(UnsafeMemory unsafeMemory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

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
    public void safeAlignTest(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        for (int i = -1; i < 70; i++) {
            if (memory instanceof UnsafeMemory.ARMMemory)
                assertEquals(i % 4 == 0, memory.safeAlignedInt(i));
            else
                assertEquals((i & 63) + 4 <= 64, memory.safeAlignedInt(i));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void arrayBaseOffset(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        assertEquals(12, memory.arrayBaseOffset(byte[].class), 4);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void objectFieldOffset(UnsafeMemory unsafeMemory) throws NoSuchFieldException {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, memory.objectFieldOffset(num), 4);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryByte(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeByte(null, memory, (byte) 12);
        assertEquals(12, this.memory.readByte(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryShort(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeShort(null, memory, (short) 12345);
        assertEquals(12345, this.memory.readShort(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeInt(null, memory, INT_VAL);
        assertEquals(INT_VAL, this.memory.readInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryAddInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeInt(memory, 0);
        final int actual = this.memory.addInt(null, memory, INT_VAL);
        assertEquals(INT_VAL, actual);
        assertEquals(INT_VAL, this.memory.readInt(memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryCASInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeInt(memory, 0);
        final boolean actual = this.memory.compareAndSwapInt(null, memory, 0, INT_VAL);
        assertTrue(actual);
        assertEquals(INT_VAL, this.memory.readInt(memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryAddLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeLong(memory, 0);
        final long actual = this.memory.addLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, actual);
        assertEquals(Long.MAX_VALUE, this.memory.readLong(memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryCASLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeLong(memory, 0);
        final boolean actual = this.memory.compareAndSwapLong(null, memory, 0L, Long.MAX_VALUE);
        assertTrue(actual);
        assertEquals(Long.MAX_VALUE, this.memory.readLong(memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryFloat(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeFloat(null, memory, 1.2345f);
        assertEquals(1.2345f, this.memory.readFloat(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryDouble(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeDouble(null, memory, 1.2345);
        assertEquals(1.2345, this.memory.readDouble(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryReference1(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        assertThrows(Exception.class, () -> {
            try {
                this.memory.putObject(null, memory, 1.2345);
            } finally {
                this.memory.freeMemory(memory, 32);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryReference2(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        assertThrows(Exception.class, () -> {
            try {
                this.memory.getObject(null, memory);
                fail();
            } finally {
                this.memory.freeMemory(memory, 32);
            }
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileByte(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileByte(null, memory, (byte) 12);
        assertEquals(12, this.memory.readVolatileByte(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileShort(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileShort(null, memory, (short) 12345);
        assertEquals(12345, this.memory.readVolatileShort(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileInt(null, memory, INT_VAL);
        assertEquals(INT_VAL, this.memory.readVolatileInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryOrderedInt(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeOrderedInt(null, memory, INT_VAL);
        assertEquals(INT_VAL, this.memory.readVolatileInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readVolatileLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryOrderedLong(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeOrderedLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readVolatileLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileFloat(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileFloat(null, memory, 1.2345f);
        assertEquals(1.2345f, this.memory.readVolatileFloat(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    public void directMemoryVolatileDouble(UnsafeMemory unsafeMemory) {
        assumeFalse(Jvm.isArm() && !(unsafeMemory instanceof UnsafeMemory.ARMMemory));
        this.memory = unsafeMemory;

        long memory = this.memory.allocate(32);
        this.memory.writeVolatileDouble(null, memory, 1.2345);
        assertEquals(1.2345, this.memory.readVolatileDouble(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    static class MyDTO {
        int num;
    }
}
