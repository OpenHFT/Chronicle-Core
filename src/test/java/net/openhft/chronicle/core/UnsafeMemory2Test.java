package net.openhft.chronicle.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE_COPY_THRESHOLD;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

@RunWith(Parameterized.class)
public class UnsafeMemory2Test {
    private final UnsafeMemory memory;

    public UnsafeMemory2Test(UnsafeMemory memory) {
        assumeFalse(Jvm.isArm() && !(memory instanceof UnsafeMemory.ARMMemory));
        this.memory = memory;
    }

    @Parameterized.Parameters(name = "{0}")
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

    @Test
    public void stopBitLengthInt() {
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

    @Test
    public void stopBitLengthLong() {
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

    @Test
    public void is7BitBytes() {
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(memory.is7Bit(bytes, 0, i));
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(memory.is7Bit(bytes, 0, i));
        }
    }

    @Test
    public void is7BitBytes2() {
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
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        memory.is7Bit(bytes, start, length));
        }
    }

    @Test
    public void is7BitChars() {
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(memory.is7Bit(chars, 0, i));
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(memory.is7Bit(chars, 0, i));
        }
    }

    @Test
    public void is7BitChars2() {
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
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        memory.is7Bit(chars, start, length));
        }
    }

    @Test
    public void is7BitAddr() {
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(memory.is7Bit(addr, 0));
        for (int i = 1; i <= 64; i++) {
            memory.writeByte(addr + i - 1, (byte) -1);
            assertFalse(memory.is7Bit(addr, i));
            memory.writeByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @Test
    public void is7BitAddr2() {
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
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        memory.is7Bit(addr + start, length));
        }
        UNSAFE.freeMemory(addr);
    }

    @Test
    public void partialReadBytes() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (0x10 + i);
        String s8 = Long.toHexString(memory.partialRead(bytes, 0, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(memory.partialRead(bytes, 0, i));
            assertEquals(s8.substring(16 - i * 2), s);
        }
    }

    @Test
    public void partialWriteBytes() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(bytes, 0, value, i);
            long l = memory.partialRead(bytes, 0, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals("i: " + i, Long.toHexString(value & mask), Long.toHexString(l));
        }
    }

    @Test
    public void partialReadAddr() {
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

    @Test
    public void partialWriteAddr() {
        long addr = memory.allocate(16);
        memory.partialWrite(addr, 0, 8);
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            memory.partialWrite(addr, value, i);
            long l = memory.partialRead(addr, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals("i: " + i, Long.toHexString(value & mask), Long.toHexString(l));
        }
        memory.freeMemory(addr, 16);
    }

    @Test
    public void copyMemory() {
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

    @Test
    public void copyMemoryMoreThanThreshold() {
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

    @Test
    public void address() {
        assertNotEquals(0, memory.address(ByteBuffer.allocateDirect(32)));
    }

    @Test
    public void setMemory() {
        long[] ds = new long[2];
        memory.setMemory(ds, memory.arrayBaseOffset(long[].class), 2 * Long.BYTES, (byte) 1);
        assertEquals(0x0101010101010101L, ds[0]);
        assertEquals(0x0101010101010101L, ds[1]);
    }

    @Test
    public void copyMemoryEachWayLongArrayMemory() {
        final long[] data = new long[]{1, 2, 3, 4};
        final int lengthInBytes = data.length * Long.BYTES;
        final long addr = memory.allocate(lengthInBytes);
        memory.copyMemory(data, memory.arrayBaseOffset(data.getClass()), addr, lengthInBytes);
        final long[] check = new long[data.length];
        memory.copyMemory(addr, check, memory.arrayBaseOffset(data.getClass()), lengthInBytes);
        assertArrayEquals(data, check);
    }

    @Test
    public void copyMemoryEachWayByteArrayMemory() {
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

    @Test
    public void copyMemoryByteArray() {
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory(data, 0, dest, 0, capacity);
        assertArrayEquals(data, dest);
    }

    @Test
    public void copyMemoryByteArrayAsObject() {
        int capacity = 37;
        final byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++)
            data[i] = (byte) i;
        final byte[] dest = new byte[capacity];
        memory.copyMemory((Object) data, memory.arrayBaseOffset(data.getClass()), dest, memory.arrayBaseOffset(data.getClass()), capacity);
        assertArrayEquals(data, dest);
    }

    @Test
    public void copyMemoryEachWayByteArrayLongArray() {
        final long[] longs = new long[]{0x0706050403020100L, 0x0f0e0d0c0b0a0908L};
        final long[] copy = new long[longs.length];
        System.arraycopy(longs, 0, copy, 0, longs.length);
        final int lengthInBytes = longs.length * Long.BYTES;
        final byte[] bytes = new byte[lengthInBytes];
        memory.copyMemory(longs, memory.arrayBaseOffset(longs.getClass()), bytes, memory.arrayBaseOffset(bytes.getClass()), lengthInBytes);
        for (int i = 0; i < lengthInBytes; i++)
            assertEquals(i, bytes[i]);
        Arrays.fill(longs, 0);
        memory.copyMemory((Object) bytes, memory.arrayBaseOffset(bytes.getClass()), longs, memory.arrayBaseOffset(longs.getClass()), lengthInBytes);
        assertArrayEquals(copy, longs);
    }

    @Test
    public void copyMemoryOverlap() {
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

    @Test
    public void copyMemoryOverlapBackwards() {
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

    @Test
    public void copyMemoryHeapObject() throws NoSuchFieldException {
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        MyDTO from = new MyDTO();
        from.num = 99;
        MyDTO to = new MyDTO();
        memory.copyMemory(from, offset, to, offset, Integer.BYTES);
        assertEquals(from.num, to.num);
    }

    @Test
    public void copyMemoryEachWayByteArrayHeapObject() throws NoSuchFieldException {
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

    @Test
    public void copyMemoryEachWayAddressHeapObject() throws NoSuchFieldException {
        Field num = MyDTO.class.getDeclaredField("num");
        long offset = memory.objectFieldOffset(num);
        final long addr = memory.allocate(Integer.BYTES);
        int expected = 97;
        memory.unsafePutInt(addr, expected);
        MyDTO to = new MyDTO();
        memory.copyMemory(addr, to, offset, Integer.BYTES);
        assertEquals(expected, to.num);
        to.num = 75;
        memory.copyMemory(to, offset, addr, Integer.BYTES);
        assertEquals(to.num, memory.unsafeGetInt(addr));
    }

    @Test
    public void safeAlignTest() {
        for (int i = -1; i < 70; i++) {
            if (memory instanceof UnsafeMemory.ARMMemory)
                assertEquals(i % 4 == 0, memory.safeAlignedInt(i));
            else
                assertEquals((i & 63) + 4 <= 64, memory.safeAlignedInt(i));
        }
    }

    @Test
    public void arrayBaseOffset() {
        assertEquals(12, memory.arrayBaseOffset(byte[].class), 4);
    }

    @Test
    public void objectFieldOffset() throws NoSuchFieldException {
        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, memory.objectFieldOffset(num), 4);
    }

    @Test
    public void directMemoryByte() {
        long memory = this.memory.allocate(32);
        this.memory.writeByte(null, memory, (byte) 12);
        assertEquals(12, this.memory.readByte(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryShort() {
        long memory = this.memory.allocate(32);
        this.memory.writeShort(null, memory, (short) 12345);
        assertEquals(12345, this.memory.readShort(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryInt() {
        long memory = this.memory.allocate(32);
        this.memory.writeInt(null, memory, 0x12345678);
        assertEquals(0x12345678, this.memory.readInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryAddInt() {
        long memory = this.memory.allocate(32);
        this.memory.addInt(null, memory, 0x12345678);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryCASInt() {
        long memory = this.memory.allocate(32);
        this.memory.compareAndSwapInt(null, memory, 0, 0x12345678);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryLong() {
        long memory = this.memory.allocate(32);
        this.memory.writeLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryAddLong() {
        long memory = this.memory.allocate(32);
        this.memory.addLong(null, memory, Long.MAX_VALUE);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryCASLong() {
        long memory = this.memory.allocate(32);
        this.memory.compareAndSwapLong(null, memory, 0L, Long.MAX_VALUE);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryFloat() {
        long memory = this.memory.allocate(32);
        this.memory.writeFloat(null, memory, 1.2345f);
        assertEquals(1.2345f, this.memory.readFloat(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryDouble() {
        long memory = this.memory.allocate(32);
        this.memory.writeDouble(null, memory, 1.2345);
        assertEquals(1.2345, this.memory.readDouble(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = Exception.class)
    public void directMemoryReference1() {
        long memory = this.memory.allocate(32);
        try {
            this.memory.putObject(null, memory, 1.2345);
        } finally {
            this.memory.freeMemory(memory, 32);
        }
        assertEquals(1.2345, this.memory.getObject(null, memory), 0f);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = Exception.class)
    public void directMemoryReference2() {
        long memory = this.memory.allocate(32);
        try {
            this.memory.getObject(null, memory);
            fail();
        } finally {
            this.memory.freeMemory(memory, 32);
        }
    }

    @Test
    public void directMemoryVolatileByte() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileByte(null, memory, (byte) 12);
        assertEquals(12, this.memory.readVolatileByte(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryVolatileShort() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileShort(null, memory, (short) 12345);
        assertEquals(12345, this.memory.readVolatileShort(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryVolatileInt() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileInt(null, memory, 0x12345678);
        assertEquals(0x12345678, this.memory.readVolatileInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryOrderedInt() {
        long memory = this.memory.allocate(32);
        this.memory.writeOrderedInt(null, memory, 0x12345678);
        assertEquals(0x12345678, this.memory.readVolatileInt(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryVolatileLong() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readVolatileLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryOrderedLong() {
        long memory = this.memory.allocate(32);
        this.memory.writeOrderedLong(null, memory, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, this.memory.readVolatileLong(null, memory));
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryVolatileFloat() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileFloat(null, memory, 1.2345f);
        assertEquals(1.2345f, this.memory.readVolatileFloat(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    @Test
    public void directMemoryVolatileDouble() {
        long memory = this.memory.allocate(32);
        this.memory.writeVolatileDouble(null, memory, 1.2345);
        assertEquals(1.2345, this.memory.readVolatileDouble(null, memory), 0f);
        this.memory.freeMemory(memory, 32);
    }

    static class MyDTO {
        int num;
    }

}