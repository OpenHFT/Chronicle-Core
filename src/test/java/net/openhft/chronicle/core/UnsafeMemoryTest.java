package net.openhft.chronicle.core;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Random;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;

public class UnsafeMemoryTest {
    static final long addr = UNSAFE.allocateMemory(128);

    @Test
    public void writeShort() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeShort(addr + i, (short) 0);
    }

    @Test
    public void readShort() {
        MEMORY.writeLong(addr, 0x123456789ABCDEFL);
        assertEquals((short) 0xCDEF, MEMORY.readShort(addr));
        assertEquals((short) 0xABCD, MEMORY.readShort(addr + 1));
    }

    @Test
    public void writeInt() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeInt(addr + i, 0);
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeOrderedInt(addr + i, 0);
    }

    @Test
    public void readInt() {
        for (int i = 0; i <= 64; i++)
            MEMORY.readInt(addr + i);
    }

    @Test
    public void writeLong() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeLong(addr + i, 0);
    }

    @Test
    public void readLong() {
        for (int i = 0; i <= 64; i++)
            MEMORY.readLong(addr + i);
    }

    @Test
    public void writeFloat() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeFloat(addr + i, 0);
    }

    @Test
    public void readFloat() {
        for (int i = 0; i <= 64; i++)
            MEMORY.readFloat(addr + i);
    }

    @Test
    public void writeDouble() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeDouble(addr + i, 0);
    }

    @Test
    public void readDouble() {
        for (int i = 0; i <= 64; i++)
            MEMORY.readDouble(addr + i);
    }

    @Test
    public void writeOrderedLong() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.writeOrderedLong(addr + i, 0);
    }

    @Test
    public void compareAndSwapInt() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.compareAndSwapInt(addr + i, 0, 0);
    }

    @Test
    public void compareAndSwapLong() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.compareAndSwapLong(addr + i, 0, 0);
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            MEMORY.readVolatileByte(addr + i);
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            MEMORY.readVolatileShort(addr + i);
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.readVolatileInt(addr + i);
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.readVolatileFloat(addr + i);
    }

    @Test
    public void readVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.readVolatileLong(addr + i);
    }

    @Test
    public void readVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.readVolatileDouble(addr + i);
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            MEMORY.writeVolatileByte(addr + i, (byte) 0);
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            MEMORY.writeVolatileShort(addr + i, (short) 0);
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.writeVolatileInt(addr + i, 0);
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.writeVolatileFloat(addr + i, 0);
    }

    @Test
    public void writeVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.writeVolatileLong(addr + i, 0L);
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.writeVolatileDouble(addr + i, 0);
    }

    @Test
    public void addInt() {
        for (int i = 0; i <= 64; i += 4)
            MEMORY.addInt(addr + i, 0);
    }

    @Test
    public void addLong() {
        for (int i = 0; i <= 64; i += 8)
            MEMORY.addLong(addr + i, 0);
    }

    @Test
    public void stopBitLengthInt() {
        assertEquals(1, MEMORY.stopBitLength(0));
        assertEquals(2, MEMORY.stopBitLength(~0));

        for (int i = 7; i < 32; i += 7) {
            int j = 1 << i;
            assertEquals(i / 7, MEMORY.stopBitLength(j - 1));
            assertEquals(i / 7 + 1, MEMORY.stopBitLength(j));
            assertEquals(i / 7 + 1, MEMORY.stopBitLength(-j));
            assertEquals(i / 7 + 2, MEMORY.stopBitLength(~j));
        }
    }

    @Test
    public void stopBitLengthLong() {
        assertEquals(1, MEMORY.stopBitLength(0L));
        assertEquals(2, MEMORY.stopBitLength(~0L));

        for (int i = 7; i < 64; i += 7) {
            long j = 1L << i;
            assertEquals(i / 7, MEMORY.stopBitLength(j - 1));
            assertEquals(i / 7 + 1, MEMORY.stopBitLength(j));
            assertEquals(i / 7 + 1, MEMORY.stopBitLength(-j));
            if (i < 63)
                assertEquals(i / 7 + 2, MEMORY.stopBitLength(~j));
        }
    }

    @Test
    public void is7BitBytes() {
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(MEMORY.is7Bit(bytes, 0, i));
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(MEMORY.is7Bit(bytes, 0, i));
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
                assertTrue(MEMORY.is7Bit(bytes, start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        MEMORY.is7Bit(bytes, start, length));
        }
    }

    @Test
    public void is7BitChars() {
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(MEMORY.is7Bit(chars, 0, i));
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(MEMORY.is7Bit(chars, 0, i));
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
                assertTrue(MEMORY.is7Bit(chars, start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        MEMORY.is7Bit(chars, start, length));
        }
    }

    @Test
    public void is7BitAddr() {
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(MEMORY.is7Bit(addr, 0));
        for (int i = 1; i <= 64; i++) {
            MEMORY.writeByte(addr + i - 1, (byte) -1);
            assertFalse(MEMORY.is7Bit(addr, i));
            MEMORY.writeByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @Test
    public void is7BitAddr2() {
        final long addr = UNSAFE.allocateMemory(256);
        for (int i = 0; i < 256; i++)
            MEMORY.writeByte(addr + i, (byte) i);

        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(MEMORY.is7Bit(addr + start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        MEMORY.is7Bit(addr + start, length));
        }
        UNSAFE.freeMemory(addr);
    }

    @Test
    public void partialReadBytes() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (0x10 + i);
        String s8 = Long.toHexString(MEMORY.partialRead(bytes, 0, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(MEMORY.partialRead(bytes, 0, i));
            assertEquals(s8.substring(16 - i * 2), s);
        }
    }

    @Test
    public void partialWriteBytes() {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            MEMORY.partialWrite(bytes, 0, value, i);
            long l = MEMORY.partialRead(bytes, 0, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals("i: " + i, Long.toHexString(value & mask), Long.toHexString(l));
        }
    }

    @Test
    public void partialReadAddr() {
        long addr = MEMORY.allocate(16);
        for (int i = 0; i < 16; i++)
            MEMORY.writeByte(addr + i, (byte) (0x10 + i));
        String s8 = Long.toHexString(MEMORY.partialRead(addr, 8));
        for (int i = 1; i < 8; i++) {
            String s = Long.toHexString(MEMORY.partialRead(addr, i));
            assertEquals(s8.substring(16 - i * 2), s);
        }
        MEMORY.freeMemory(addr, 16);
    }

    @Test
    public void partialWriteAddr() {
        long addr = MEMORY.allocate(16);
        MEMORY.partialWrite(addr, 0, 8);
        for (int i = 0; i < 8; i++) {
            final long value = 0x1011121314151617L;
            MEMORY.partialWrite(addr, value, i);
            long l = MEMORY.partialRead(addr, 8);
            long mask = (1L << (8 * i)) - 1;
            assertEquals("i: " + i, Long.toHexString(value & mask), Long.toHexString(l));
        }
        MEMORY.freeMemory(addr, 16);
    }

    @Test
    public void copyMemory() {
        long addr = MEMORY.allocate(32);
        long addr2 = MEMORY.allocate(32);
        final byte b1 = (byte) 0x7F;
        final byte b2 = (byte) 0x80;
        MEMORY.setMemory(addr2, 32, b1);
        for (int i = 1; i < 31; i++) {
            for (int j = i + 1; j < 31; j++) {
                MEMORY.setMemory(addr, 32, b2);
                MEMORY.copyMemory(addr2, addr + i, j - i);
                assertEquals(b2, MEMORY.readByte(addr + i - 1));
                assertEquals(b1, MEMORY.readByte(addr + i));
                assertEquals(b1, MEMORY.readByte(addr + j - 1));
                assertEquals(b2, MEMORY.readByte(addr + j));
            }
        }
        MEMORY.freeMemory(addr, 32);
        MEMORY.freeMemory(addr2, 32);
    }

    @Test
    public void copyMemoryEachWay() {
        long addr = MEMORY.allocate(32);
        long[] data = new long[4];
        MEMORY.copyMemory(data, 0, addr, 32);
        MEMORY.copyMemory(addr, data, 0, 32);
    }

    @Test
    public void safeAlignTest() {
        for (int i = -1; i < 70; i++) {
            if (Jvm.isArm())
                assertEquals(i % 4 == 0, MEMORY.safeAlignedInt(i));
            else
                assertEquals((i & 63) + 4 <= 64, MEMORY.safeAlignedInt(i));
        }
    }

    @Test
    public void arrayBaseOffset() {
        assertEquals(12, MEMORY.arrayBaseOffset(byte[].class), 4);
    }

    @Test
    public void objectFieldOffset() throws NoSuchFieldException {
        Field num = MyDTO.class.getDeclaredField("num");
        assertEquals(12, MEMORY.objectFieldOffset(num), 4);
    }

    static class MyDTO {
        int num;
    }

}