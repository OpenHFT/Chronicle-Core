/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE_COPY_THRESHOLD;
import static org.junit.jupiter.api.Assertions.*;

public class UnsafeMemory2Test extends CoreTestCommon {
    private static final int INT_VAL = 0x12345678;
    private static final Random TEST_RANDOM = new Random(1);
    private static final UnsafeMemory MEMORY = new UnsafeMemory();
    private static final UnsafeMemory.ARMMemory ARM_MEMORY = new UnsafeMemory.ARMMemory();

    static Stream<UnsafeMemory> memories() {
        return Jvm.isArm() ? Stream.of(ARM_MEMORY) : Stream.of(MEMORY, ARM_MEMORY);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    public void stopBitLengthInt(UnsafeMemory memory) {
        assertEquals(1, memory.stopBitLength(0), "stopBitLengthInt: L48");
        assertEquals(2, memory.stopBitLength(~0), "stopBitLengthInt: L49");

        for (int i = 7; i < 32; i += 7) {
            int j = 1 << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1), "stopBitLengthInt: L53");
            assertEquals(i / 7 + 1, memory.stopBitLength(j), "stopBitLengthInt: L54");
            assertEquals(i / 7 + 1, memory.stopBitLength(-j), "stopBitLengthInt: L55");
            assertEquals(i / 7 + 2, memory.stopBitLength(~j), "stopBitLengthInt: L56");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void stopBitLengthLong(UnsafeMemory memory){
        assertEquals(1, memory.stopBitLength(0L), "stopBitLengthLong: L62");
        assertEquals(2, memory.stopBitLength(~0L), "stopBitLengthLong: L63");

        for (int i = 7; i < 64; i += 7) {
            long j = 1L << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1), "stopBitLengthLong: L67");
            assertEquals(i / 7 + 1, memory.stopBitLength(j), "stopBitLengthLong: L68");
            assertEquals(i / 7 + 1, memory.stopBitLength(-j), "stopBitLengthLong: L69");
            if (i < 63)
                assertEquals(i / 7 + 2, memory.stopBitLength(~j), "stopBitLengthLong: L71");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitBytes(UnsafeMemory memory){
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(memory.is7Bit(bytes, 0, i), "is7BitBytes: L79");
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(memory.is7Bit(bytes, 0, i), "is7BitBytes: L83");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitBytes2(UnsafeMemory memory){
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++)
            bytes[i] = (byte) i;
        Random rand = TEST_RANDOM;
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(bytes, start, length), "is7BitBytes2: L99");
            else
                assertEquals(start + length <= 128, memory.is7Bit(bytes, start, length),
                        "start: " + start + ", length: " + length);
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitChars(UnsafeMemory memory){
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(memory.is7Bit(chars, 0, i), "is7BitChars: L110");
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(memory.is7Bit(chars, 0, i), "is7BitChars: L114");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitChars2(UnsafeMemory memory){
        char[] chars = new char[512];
        for (int i = 0; i < 512; i++)
            chars[i] = (char) i;
        Random rand = TEST_RANDOM;
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(512);
            int b = rand.nextInt(512);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(chars, start, length), "is7BitChars2: L130");
            else
                assertEquals(start + length <= 128, memory.is7Bit(chars, start, length),
                        "start: " + start + ", length: " + length);
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitAddr(UnsafeMemory memory){
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(memory.is7Bit(addr, 0), "is7BitAddr: L140");
        for (int i = 1; i <= 64; i++) {
            memory.writeByte(addr + i - 1, (byte) -1);
            assertFalse(memory.is7Bit(addr, i), "is7BitAddr: L143");
            memory.writeByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void is7BitAddr2(UnsafeMemory memory){
        final long addr = UNSAFE.allocateMemory(256);
        for (int i = 0; i < 256; i++)
            memory.writeByte(addr + i, (byte) i);

        Random rand = TEST_RANDOM;
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(memory.is7Bit(addr + start, length), "is7BitAddr2: L162");
            else
                assertEquals(start + length <= 128, memory.is7Bit(addr + start, length),
                        "start: " + start + ", length: " + length);
        }
        UNSAFE.freeMemory(addr);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void partialReadBytes(UnsafeMemory memory){
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (0x10 + i);
        String s8 = Long.toHexString(memory.partialRead(bytes, 0, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(bytes, 0, i));
            assertEquals(s8.substring(16 - i * 2), s, "partialReadBytes: L178");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void partialWriteBytes(UnsafeMemory memory){
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

    @MethodSource("memories")
    public void partialReadAddr(UnsafeMemory memory){
        long addr = memory.allocate(16);
        for (int i = 0; i < 16; i++)
            memory.writeByte(addr + i, (byte) (0x10 + i));
        String s8 = Long.toHexString(memory.partialRead(addr, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(addr, i));
            assertEquals(s8.substring(16 - i * 2), s, "partialReadAddr: L202");
        }
        memory.freeMemory(addr, 16);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void partialWriteAddr(UnsafeMemory memory){
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

    @MethodSource("memories")
    public void copyMemory(UnsafeMemory memory){
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
                assertEquals(b2, memory.readByte(addr + i - 1), "copyMemory: L233");
                assertEquals(b1, memory.readByte(addr + i), "copyMemory: L234");
                assertEquals(b1, memory.readByte(addr + j - 1), "copyMemory: L235");
                assertEquals(b2, memory.readByte(addr + j), "copyMemory: L236");
            }
        }
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryMoreThanThreshold(UnsafeMemory memory){
        final long capacity = (int) (UNSAFE_COPY_THRESHOLD * 2.5d);
        long addr = memory.allocate(capacity);
        long addr2 = memory.allocate(capacity);
        for (int i = 0; i < capacity; i += 4)
            memory.writeInt(addr + i, i);
        final byte b2 = (byte) 0x80;
        memory.setMemory(addr2, capacity, b2);
        memory.copyMemory(addr, addr2, capacity);
        for (int i = 0; i < capacity; i += 4)
            assertEquals(i, memory.readInt(addr2 + i), "copyMemoryMoreThanThreshold: L254");
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void address(UnsafeMemory memory){
        assertNotEquals(0, memory.address(ByteBuffer.allocateDirect(32)));
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void setMemory(UnsafeMemory memory){
        long[] ds = new long[2];
        memory.setMemory(ds, memory.arrayBaseOffset(long[].class), 2 * Long.BYTES, (byte) 1);
        assertEquals(0x0101010101010101L, ds[0], "setMemory: L268");
        assertEquals(0x0101010101010101L, ds[1], "setMemory: L269");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryEachWayLongArrayMemory(UnsafeMemory memory){
        final long[] data = {1, 2, 3, 4};
        final int lengthInBytes = data.length * Long.BYTES;
        final long addr = memory.allocate(lengthInBytes);
        memory.copyMemory(data, memory.arrayBaseOffset(data.getClass()), addr, lengthInBytes);
        final long[] check = new long[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), lengthInBytes);
        assertArrayEquals(data, check, "copyMemoryEachWayLongArrayMemory: L280");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryEachWayByteArrayMemory(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final long addr = memory.allocate(capacity);
        memory.copyMemory(data, 0, addr, capacity);
        final byte[] check = new byte[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, check, "copyMemoryEachWayByteArrayMemory: L293");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryByteArray(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory(data, 0, dest, 0, capacity);
        assertArrayEquals(data, dest, "copyMemoryByteArray: L304");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryByteArrayAsObject(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory((Object) data, memory.arrayBaseOffset(data.getClass()), dest, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, dest, "copyMemoryByteArrayAsObject: L315");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryEachWayByteArrayLongArray(UnsafeMemory memory){
        final long[] longs = {0x0706050403020100L, 0x0f0e0d0c0b0a0908L};
        final long[] copy = new long[longs.length];
        System.arraycopy(longs, 0, copy, 0, longs.length);
        final int lengthInBytes = longs.length * Long.BYTES;
        final byte[] bytes = new byte[lengthInBytes];
        memory.copyMemory(longs, memory.arrayBaseOffset(longs.getClass()), bytes, memory.arrayBaseOffset(bytes.getClass()), lengthInBytes);
        for (int i = 0; i < lengthInBytes; i++)
            assertEquals(i, bytes[i], "copyMemoryEachWayByteArrayLongArray: L327");
        Arrays.fill(longs, 0);
        memory.copyMemory(bytes, 0, longs, memory.arrayBaseOffset(longs.getClass()), lengthInBytes);
        assertArrayEquals(copy, longs, "copyMemoryEachWayByteArrayLongArray: L330");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryOverlap(UnsafeMemory memory){
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, 0, data, offset, length);
        for (int i = 0; i < offset; i++)
            assertEquals(i, data[i], "copyMemoryOverlap: L343");
        for (int i = 0; i < length; i++)
            assertEquals(i, data[i + offset], "copyMemoryOverlap: L345");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryOverlapBackwards(UnsafeMemory memory){
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, offset, data, 0, length);
        for (int i = 0; i < length; i++)
            assertEquals(i + offset, data[i], "copyMemoryOverlapBackwards: L358");
        for (int i = length; i < capacity; i++)
            assertEquals(i, data[i], "copyMemoryOverlapBackwards: L360");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        MyDTO from = new MyDTO();
        from.num = 99;
        MyDTO to = new MyDTO();
        memory.copyMemory(from, offset, to, offset, Integer.BYTES);
        assertEquals(from.num, to.num, "copyMemoryHeapObject: L371");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryEachWayByteArrayHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final byte[] data = new byte[Integer.BYTES];
        data[0] = 98;
        MyDTO to = new MyDTO();

        memory.copyMemory(data, 0, to, offset, Integer.BYTES);

        assertEquals(data[0], to.num, "copyMemoryEachWayByteArrayHeapObject: L384");
        to.num = 77;
        memory.copyMemory(to, offset, data, memory.arrayBaseOffset(data.getClass()), Integer.BYTES);
        assertEquals(to.num, data[0], "copyMemoryEachWayByteArrayHeapObject: L387");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void copyMemoryEachWayAddressHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final long addr = memory.allocate(Integer.BYTES);
        int expected = 97;
        UnsafeMemory.unsafePutInt(addr, expected);
        MyDTO to = new MyDTO();
        memory.copyMemory(addr, to, offset, Integer.BYTES);
        assertEquals(expected, to.num, "copyMemoryEachWayAddressHeapObject: L399");
        to.num = 75;
        memory.copyMemory(to, offset, addr, Integer.BYTES);
        assertEquals(to.num, UnsafeMemory.unsafeGetInt(addr), "copyMemoryEachWayAddressHeapObject: L402");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void safeAlignTest(UnsafeMemory memory){
        for (int i = -1; i < 70; i++) {
            if (memory instanceof UnsafeMemory.ARMMemory)
                assertEquals(i % 4 == 0, memory.safeAlignedInt(i), "safeAlignTest: L409");
            else
                assertEquals((i & 63) + 4 <= 64, memory.safeAlignedInt(i), "safeAlignTest: L411");
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void arrayBaseOffset(UnsafeMemory memory){
        assertEquals(12, memory.arrayBaseOffset(byte[].class), 4, "arrayBaseOffset: L417");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void objectFieldOffset(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, memory.objectFieldOffset(num), 4, "objectFieldOffset: L423");
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryByte(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeByte(null, address, (byte) 12);
        assertEquals(12, memory.readByte(null, address), "directMemoryByte: L430");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryShort(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeShort(null, address, (short) 12345);
        assertEquals(12345, memory.readShort(null, address), "directMemoryShort: L438");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readInt(null, address), "directMemoryInt: L446");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryAddInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(address, 0);
        final int actual = memory.addInt(null, address, INT_VAL);
        assertEquals(INT_VAL, actual, "directMemoryAddInt: L455");
        assertEquals(INT_VAL, memory.readInt(address), "directMemoryAddInt: L456");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryCASInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(address, 0);
        final boolean actual = memory.compareAndSwapInt(null, address, 0, INT_VAL);
        assertTrue(actual, "directMemoryCASInt: L465");
        assertEquals(INT_VAL, memory.readInt(address), "directMemoryCASInt: L466");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readLong(null, address), "directMemoryLong: L474");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryAddLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(address, 0);
        final long actual = memory.addLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, actual, "directMemoryAddLong: L483");
        assertEquals(Long.MAX_VALUE, memory.readLong(address), "directMemoryAddLong: L484");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryCASLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(address, 0);
        final boolean actual = memory.compareAndSwapLong(null, address, 0L, Long.MAX_VALUE);
        assertTrue(actual, "directMemoryCASLong: L493");
        assertEquals(Long.MAX_VALUE, memory.readLong(address), "directMemoryCASLong: L494");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryFloat(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeFloat(null, address, 1.2345f);
        assertEquals(1.2345f, memory.readFloat(null, address), 0f, "directMemoryFloat: L502");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryDouble(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeDouble(null, address, 1.2345);
        assertEquals(1.2345, memory.readDouble(null, address), 0f, "directMemoryDouble: L510");
        memory.freeMemory(address, 32);
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    public void directMemoryReference1(UnsafeMemory memory){
        long address = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.putObject(null, address, 1.2345), "directMemoryReference1: L523");
        } finally {
            memory.freeMemory(address, 32);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    public void directMemoryReference2(UnsafeMemory memory){
        long address = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.getObject(null, address), "directMemoryReference2: L532");
        } finally {
            memory.freeMemory(address, 32);
        }
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileByte(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileByte(null, address, (byte) 12);
        assertEquals(12, memory.readVolatileByte(null, address), "directMemoryVolatileByte: L542");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileShort(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileShort(null, address, (short) 12345);
        assertEquals(12345, memory.readVolatileShort(null, address), "directMemoryVolatileShort: L550");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, address), "directMemoryVolatileInt: L558");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryOrderedInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeOrderedInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, address), "directMemoryOrderedInt: L566");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, address), "directMemoryVolatileLong: L574");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryOrderedLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeOrderedLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, address), "directMemoryOrderedLong: L582");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileFloat(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileFloat(null, address, 1.2345f);
        assertEquals(1.2345f, memory.readVolatileFloat(null, address), 0f, "directMemoryVolatileFloat: L590");
        memory.freeMemory(address, 32);
    }

    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    public void directMemoryVolatileDouble(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileDouble(null, address, 1.2345);
        assertEquals(1.2345, memory.readVolatileDouble(null, address), 0f, "directMemoryVolatileDouble: L598");
        memory.freeMemory(address, 32);
    }

    static class MyDTO {
        int num;
    }
}
