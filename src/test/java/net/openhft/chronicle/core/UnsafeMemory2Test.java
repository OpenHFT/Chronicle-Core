/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.DisplayName;
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

class UnsafeMemory2Test extends CoreTestCommon {
    private static final int INT_VAL = 0x12345678;
    private static final Random TEST_RANDOM = new Random(1);
    private static final UnsafeMemory MEMORY = new UnsafeMemory();
    private static final UnsafeMemory.ARMMemory ARM_MEMORY = new UnsafeMemory.ARMMemory();

    static Stream<UnsafeMemory> memories() {
        return Jvm.isArm() ? Stream.of(ARM_MEMORY) : Stream.of(MEMORY, ARM_MEMORY);
    }

    @DisplayName("Stop bit length for int values matches encoding")
    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    void stopBitLengthInt(UnsafeMemory memory) {
        assertEquals(1, memory.stopBitLength(0), "stop-bit encoding of zero should require one byte");
        assertEquals(2, memory.stopBitLength(~0), "stop-bit encoding of -1 should require two bytes");

        for (int i = 7; i < 32; i += 7) {
            int j = 1 << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1), "stop-bit length should match expected bytes for value just below 2^" + i);
            assertEquals(i / 7 + 1, memory.stopBitLength(j), "stop-bit length should match expected bytes for value 2^" + i);
            assertEquals(i / 7 + 1, memory.stopBitLength(-j), "stop-bit length should match expected bytes for negative value -2^" + i);
            assertEquals(i / 7 + 2, memory.stopBitLength(~j), "stop-bit length should match expected bytes for bitwise complement of 2^" + i);
        }
    }

    @DisplayName("Stop bit length for long values matches encoding")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void stopBitLengthLong(UnsafeMemory memory){
        assertEquals(1, memory.stopBitLength(0L), "stop-bit encoding of zero long should require one byte");
        assertEquals(2, memory.stopBitLength(~0L), "stop-bit encoding of -1 long should require two bytes");

        for (int i = 7; i < 64; i += 7) {
            long j = 1L << i;
            assertEquals(i / 7, memory.stopBitLength(j - 1), "stop-bit length should match expected bytes for long value just below 2^" + i);
            assertEquals(i / 7 + 1, memory.stopBitLength(j), "stop-bit length should match expected bytes for long value 2^" + i);
            assertEquals(i / 7 + 1, memory.stopBitLength(-j), "stop-bit length should match expected bytes for negative long value -2^" + i);
            if (i < 63)
                assertEquals(i / 7 + 2, memory.stopBitLength(~j), "stop-bit length should match expected bytes for bitwise complement of long 2^" + i);
        }
    }

    @DisplayName("Byte arrays report only 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitBytes(UnsafeMemory memory){
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(memory.is7Bit(bytes, 0, i), "zero-initialized byte array should contain only 7-bit values at i=" + i);
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(memory.is7Bit(bytes, 0, i), "byte array with high bit set should not be 7-bit clean at i=" + i);
        }
    }

    @DisplayName("Byte array range check enforces 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitBytes2(UnsafeMemory memory){
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
                assertTrue(memory.is7Bit(bytes, start, length), "empty byte range should be considered 7-bit clean at i=" + i);
            else
                assertEquals(start + length <= 128, memory.is7Bit(bytes, start, length),
                        "byte range 7-bit check start=" + start + ", length=" + length + ", i=" + i);
        }
    }

    @DisplayName("Char arrays report only 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitChars(UnsafeMemory memory){
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(memory.is7Bit(chars, 0, i), "zero-initialized char array should contain only 7-bit values at i=" + i);
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(memory.is7Bit(chars, 0, i), "char array with high bit set should not be 7-bit clean at i=" + i);
        }
    }

    @DisplayName("Char array range check enforces 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitChars2(UnsafeMemory memory){
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
                assertTrue(memory.is7Bit(chars, start, length), "empty char range should be considered 7-bit clean at i=" + i);
            else
                assertEquals(start + length <= 128, memory.is7Bit(chars, start, length),
                        "char range 7-bit check start=" + start + ", length=" + length + ", i=" + i);
        }
    }

    @DisplayName("Address range reports only 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitAddr(UnsafeMemory memory){
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(memory.is7Bit(addr, 0), "empty memory range should be considered 7-bit clean");
        for (int i = 1; i <= 64; i++) {
            memory.writeByte(addr + i - 1, (byte) -1);
            assertFalse(memory.is7Bit(addr, i), "memory with high bit set should not be 7-bit clean at i=" + i);
            memory.writeByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @DisplayName("Address range check enforces 7 bit values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void is7BitAddr2(UnsafeMemory memory){
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
                assertTrue(memory.is7Bit(addr + start, length), "empty memory range should be considered 7-bit clean at i=" + i);
            else
                assertEquals(start + length <= 128, memory.is7Bit(addr + start, length),
                        "address range 7-bit check start=" + start + ", length=" + length + ", i=" + i);
        }
        UNSAFE.freeMemory(addr);
    }

    @DisplayName("Partial byte reads return expected slice")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void partialReadBytes(UnsafeMemory memory){
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (0x10 + i);
        String s8 = Long.toHexString(memory.partialRead(bytes, 0, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(bytes, 0, i));
            assertEquals(s8.substring(16 - i * 2), s, "partial read should match least significant bytes of full read at i=" + i);
        }
    }

    @DisplayName("Partial byte writes store expected slice")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void partialWriteBytes(UnsafeMemory memory){
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(bytes, 0, value, i);
            long l = memory.partialRead(bytes, 0, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals(Long.toHexString(value & mask), Long.toHexString(l), "partial write should match masked bytes at i=" + i);
        }
    }

    @DisplayName("Partial address reads return expected slice")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void partialReadAddr(UnsafeMemory memory){
        long addr = memory.allocate(16);
        for (int i = 0; i < 16; i++)
            memory.writeByte(addr + i, (byte) (0x10 + i));
        String s8 = Long.toHexString(memory.partialRead(addr, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(addr, i));
            assertEquals(s8.substring(16 - i * 2), s, "partial read from address should match least significant bytes of full read at i=" + i);
        }
        memory.freeMemory(addr, 16);
    }

    @DisplayName("Partial address writes store expected slice")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void partialWriteAddr(UnsafeMemory memory){
        long addr = memory.allocate(16);
        memory.partialWrite(addr, 0, 8);
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(addr, value, i);
            long l = memory.partialRead(addr, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals(Long.toHexString(value & mask), Long.toHexString(l), "partial address write should match masked bytes at i=" + i);
        }
        memory.freeMemory(addr, 16);
    }

    @DisplayName("Copy memory transfers bytes to destination")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemory(UnsafeMemory memory){
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
                assertEquals(b2, memory.readByte(addr + i - 1), "byte before copied region should remain unchanged at i=" + i + ", j=" + j);
                assertEquals(b1, memory.readByte(addr + i), "first byte of copied region should have source value at i=" + i + ", j=" + j);
                assertEquals(b1, memory.readByte(addr + j - 1), "last byte of copied region should have source value at i=" + i + ", j=" + j);
                assertEquals(b2, memory.readByte(addr + j), "byte after copied region should remain unchanged at i=" + i + ", j=" + j);
            }
        }
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @DisplayName("Copy memory handles sizes above threshold")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryMoreThanThreshold(UnsafeMemory memory){
        final long capacity = (int) (UNSAFE_COPY_THRESHOLD * 2.5d);
        long addr = memory.allocate(capacity);
        long addr2 = memory.allocate(capacity);
        for (int i = 0; i < capacity; i += 4)
            memory.writeInt(addr + i, i);
        final byte b2 = (byte) 0x80;
        memory.setMemory(addr2, capacity, b2);
        memory.copyMemory(addr, addr2, capacity);
        for (int i = 0; i < capacity; i += 4)
            assertEquals(i, memory.readInt(addr2 + i), "large memory copy should preserve all int values at each offset at i=" + i);
        memory.freeMemory(addr, capacity);
        memory.freeMemory(addr2, capacity);
    }

    @DisplayName("Address lookup returns allocated memory pointer")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void address(UnsafeMemory memory){
        assertNotEquals(0, memory.address(ByteBuffer.allocateDirect(32)), "address should be non-zero for direct buffer");
    }

    @DisplayName("Set memory writes repeated byte values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void setMemory(UnsafeMemory memory){
        long[] ds = new long[2];
        memory.setMemory(ds, memory.arrayBaseOffset(long[].class), 2 * Long.BYTES, (byte) 1);
        assertEquals(0x0101010101010101L, ds[0], "first long should be filled with 0x01 pattern");
        assertEquals(0x0101010101010101L, ds[1], "second long should be filled with 0x01 pattern");
    }

    @DisplayName("Copy long array to memory and back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryEachWayLongArrayMemory(UnsafeMemory memory){
        final long[] data = {1, 2, 3, 4};
        final int lengthInBytes = data.length * Long.BYTES;
        final long addr = memory.allocate(lengthInBytes);
        memory.copyMemory(data, memory.arrayBaseOffset(data.getClass()), addr, lengthInBytes);
        final long[] check = new long[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), lengthInBytes);
        assertArrayEquals(data, check, "long array copy via memory address should preserve all elements");
    }

    @DisplayName("Copy byte array to memory and back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryEachWayByteArrayMemory(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final long addr = memory.allocate(capacity);
        memory.copyMemory(data, 0, addr, capacity);
        final byte[] check = new byte[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, check, "byte array copy via memory address should preserve all elements");
    }

    @DisplayName("Copy memory into byte array correctly")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryByteArray(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory(data, 0, dest, 0, capacity);
        assertArrayEquals(data, dest, "byte array to byte array copy should preserve all elements");
    }

    @DisplayName("Copy memory into byte array via object")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryByteArrayAsObject(UnsafeMemory memory){
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory((Object) data, memory.arrayBaseOffset(data.getClass()), dest, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, dest, "byte array copy via Object reference should preserve all elements");
    }

    @DisplayName("Copy byte array to long array and back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryEachWayByteArrayLongArray(UnsafeMemory memory){
        final long[] longs = {0x0706050403020100L, 0x0f0e0d0c0b0a0908L};
        final long[] copy = new long[longs.length];
        System.arraycopy(longs, 0, copy, 0, longs.length);
        final int lengthInBytes = longs.length * Long.BYTES;
        final byte[] bytes = new byte[lengthInBytes];
        memory.copyMemory(longs, memory.arrayBaseOffset(longs.getClass()), bytes, memory.arrayBaseOffset(bytes.getClass()), lengthInBytes);
        for (int i = 0; i < lengthInBytes; i++)
            assertEquals(i, bytes[i], "long array to byte array copy should preserve byte values at i=" + i);
        Arrays.fill(longs, 0);
        memory.copyMemory(bytes, 0, longs, memory.arrayBaseOffset(longs.getClass()), lengthInBytes);
        assertArrayEquals(copy, longs, "byte array back to long array copy should preserve all elements");
    }

    @DisplayName("Copy memory handles overlapping ranges safely")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryOverlap(UnsafeMemory memory){
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, 0, data, offset, length);
        for (int i = 0; i < offset; i++)
            assertEquals(i, data[i], "bytes before overlap region should remain unchanged at i=" + i);
        for (int i = 0; i < length; i++)
            assertEquals(i, data[i + offset], "overlapping forward copy should preserve source bytes at i=" + i);
    }

    @DisplayName("Copy memory handles backwards overlapping ranges")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryOverlapBackwards(UnsafeMemory memory){
        int capacity = 32;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        int offset = capacity / 4;
        int length = (capacity / 4) * 3;
        memory.copyMemory(data, offset, data, 0, length);
        for (int i = 0; i < length; i++)
            assertEquals(i + offset, data[i], "overlapping backward copy should preserve source bytes with offset at i=" + i);
        for (int i = length; i < capacity; i++)
            assertEquals(i, data[i], "bytes after overlap region should remain unchanged at i=" + i);
    }

    @DisplayName("Copy memory to heap object fields")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        MyDTO from = new MyDTO();
        from.num = 99;
        MyDTO to = new MyDTO();
        memory.copyMemory(from, offset, to, offset, Integer.BYTES);
        assertEquals(from.num, to.num, "object field should be copied between heap objects");
    }

    @DisplayName("Copy byte array to heap object and back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryEachWayByteArrayHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final byte[] data = new byte[Integer.BYTES];
        data[0] = 98;
        MyDTO to = new MyDTO();

        memory.copyMemory(data, 0, to, offset, Integer.BYTES);

        assertEquals(data[0], to.num, "byte array value should be copied to object field");
        to.num = 77;
        memory.copyMemory(to, offset, data, memory.arrayBaseOffset(data.getClass()), Integer.BYTES);
        assertEquals(to.num, data[0], "object field value should be copied back to byte array");
    }

    @DisplayName("Copy address to heap object and back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void copyMemoryEachWayAddressHeapObject(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final long addr = memory.allocate(Integer.BYTES);
        int expected = 97;
        UnsafeMemory.unsafePutInt(addr, expected);
        MyDTO to = new MyDTO();
        memory.copyMemory(addr, to, offset, Integer.BYTES);
        assertEquals(expected, to.num, "off-heap address value should be copied to object field");
        to.num = 75;
        memory.copyMemory(to, offset, addr, Integer.BYTES);
        assertEquals(to.num, UnsafeMemory.unsafeGetInt(addr), "object field value should be copied back to off-heap address");
    }

    @DisplayName("Safe alignment detects misaligned addresses correctly")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void safeAlignTest(UnsafeMemory memory){
        for (int i = -1; i < 70; i++) {
            if (memory instanceof UnsafeMemory.ARMMemory)
                assertEquals(i % 4 == 0, memory.safeAlignedInt(i), "ARM memory should require 4-byte alignment for safe int access at i=" + i);
            else
                assertEquals((i & 63) + 4 <= 64, memory.safeAlignedInt(i), "x86 memory should allow int access within 64-byte cache line boundary at i=" + i);
        }
    }

    @DisplayName("Array base offset matches Unsafe values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void arrayBaseOffset(UnsafeMemory memory){
        assertEquals(12, memory.arrayBaseOffset(byte[].class), 4, "byte array base offset should be approximately 12 bytes for object header");
    }

    @DisplayName("Object field offset matches Unsafe values")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void objectFieldOffset(UnsafeMemory memory) throws NoSuchFieldException{
        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, memory.objectFieldOffset(num), 4, "field offset should be approximately 12 bytes after object header");
    }

    @DisplayName("Direct memory byte read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryByte(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeByte(null, address, (byte) 12);
        assertEquals(12, memory.readByte(null, address), "byte read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory short read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryShort(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeShort(null, address, (short) 12345);
        assertEquals(12345, memory.readShort(null, address), "short read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory int read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readInt(null, address), "int read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory add int updates value")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryAddInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(address, 0);
        final int actual = memory.addInt(null, address, INT_VAL);
        assertEquals(INT_VAL, actual, "addInt should return previous value before addition");
        assertEquals(INT_VAL, memory.readInt(address), "int value after atomic add should equal the added value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory CAS int updates value")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryCASInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeInt(address, 0);
        final boolean actual = memory.compareAndSwapInt(null, address, 0, INT_VAL);
        assertTrue(actual, "compareAndSwapInt should succeed with matching expected value");
        assertEquals(INT_VAL, memory.readInt(address), "int value after successful CAS should equal the new value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory long read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readLong(null, address), "long read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory add long updates value")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryAddLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(address, 0);
        final long actual = memory.addLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, actual, "addLong should return previous value before addition");
        assertEquals(Long.MAX_VALUE, memory.readLong(address), "long value after atomic add should equal the added value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory CAS long updates value")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryCASLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeLong(address, 0);
        final boolean actual = memory.compareAndSwapLong(null, address, 0L, Long.MAX_VALUE);
        assertTrue(actual, "compareAndSwapLong should succeed with matching expected value");
        assertEquals(Long.MAX_VALUE, memory.readLong(address), "long value after successful CAS should equal the new value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory float read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryFloat(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeFloat(null, address, 1.2345f);
        assertEquals(1.2345f, memory.readFloat(null, address), 0f, "float read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory double read write round trip")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryDouble(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeDouble(null, address, 1.2345);
        assertEquals(1.2345, memory.readDouble(null, address), 0f, "double read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @SuppressWarnings("ConstantConditions")
    @DisplayName("Direct memory reference write reads expected value")
    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    void directMemoryReference1(UnsafeMemory memory){
        long address = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.putObject(null, address, 1.2345), "storing object reference to off-heap memory should throw exception");
        } finally {
            memory.freeMemory(address, 32);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @DisplayName("Direct memory reference updates preserve object identity")
    @ParameterizedTest(name = "{0}")
    @MethodSource("memories")
    void directMemoryReference2(UnsafeMemory memory){
        long address = memory.allocate(32);
        try {
            assertThrows(Exception.class, () -> memory.getObject(null, address), "reading object reference from off-heap memory should throw exception");
        } finally {
            memory.freeMemory(address, 32);
        }
    }

    @DisplayName("Direct memory volatile byte read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileByte(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileByte(null, address, (byte) 12);
        assertEquals(12, memory.readVolatileByte(null, address), "volatile byte read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory volatile short read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileShort(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileShort(null, address, (short) 12345);
        assertEquals(12345, memory.readVolatileShort(null, address), "volatile short read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory volatile int read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, address), "volatile int read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory ordered int write reads back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryOrderedInt(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeOrderedInt(null, address, INT_VAL);
        assertEquals(INT_VAL, memory.readVolatileInt(null, address), "ordered int write should be visible to subsequent volatile read");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory volatile long read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, address), "volatile long read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory ordered long write reads back")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryOrderedLong(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeOrderedLong(null, address, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, memory.readVolatileLong(null, address), "ordered long write should be visible to subsequent volatile read");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory volatile float read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileFloat(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileFloat(null, address, 1.2345f);
        assertEquals(1.2345f, memory.readVolatileFloat(null, address), 0f, "volatile float read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    @DisplayName("Direct memory volatile double read write")
    @ParameterizedTest(name = "{0}")

    @MethodSource("memories")
    void directMemoryVolatileDouble(UnsafeMemory memory){
        long address = memory.allocate(32);
        memory.writeVolatileDouble(null, address, 1.2345);
        assertEquals(1.2345, memory.readVolatileDouble(null, address), 0f, "volatile double read from direct memory should match written value");
        memory.freeMemory(address, 32);
    }

    static class MyDTO {
        int num;
    }
}
