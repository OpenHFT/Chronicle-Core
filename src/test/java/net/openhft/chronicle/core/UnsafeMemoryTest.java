package net.openhft.chronicle.core;

import org.junit.Test;

import java.util.Random;

import static net.openhft.chronicle.core.UnsafeMemory.INSTANCE;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;

public class UnsafeMemoryTest {
    static final long addr = UNSAFE.allocateMemory(128);

    @Test
    public void writeShort() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeShort(addr + i, (short) 0);
    }

    @Test
    public void readShort() {
        INSTANCE.writeLong(addr, 0x123456789ABCDEFL);
        assertEquals((short) 0xCDEF, INSTANCE.readShort(addr));
        assertEquals((short) 0xABCD, INSTANCE.readShort(addr + 1));
    }

    @Test
    public void writeInt() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeInt(addr + i, 0);
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeOrderedInt(addr + i, 0);
    }

    @Test
    public void readInt() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.readInt(addr + i);
    }

    @Test
    public void writeLong() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeLong(addr + i, 0);
    }

    @Test
    public void readLong() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.readLong(addr + i);
    }

    @Test
    public void writeFloat() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeFloat(addr + i, 0);
    }

    @Test
    public void readFloat() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.readFloat(addr + i);
    }

    @Test
    public void writeDouble() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeDouble(addr + i, 0);
    }

    @Test
    public void readDouble() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.readDouble(addr + i);
    }

    @Test
    public void writeOrderedLong() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.writeOrderedLong(addr + i, 0);
    }

    @Test
    public void compareAndSwapInt() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.compareAndSwapInt(addr + i, 0, 0);
    }

    @Test
    public void compareAndSwapLong() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.compareAndSwapLong(addr + i, 0, 0);
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.readVolatileByte(addr + i);
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            INSTANCE.readVolatileShort(addr + i);
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.readVolatileInt(addr + i);
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.readVolatileFloat(addr + i);
    }

    @Test
    public void readVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.readVolatileLong(addr + i);
    }

    @Test
    public void readVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.readVolatileDouble(addr + i);
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            INSTANCE.writeVolatileByte(addr + i, (byte) 0);
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            INSTANCE.writeVolatileShort(addr + i, (short) 0);
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.writeVolatileInt(addr + i, 0);
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.writeVolatileFloat(addr + i, 0);
    }

    @Test
    public void writeVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.writeVolatileLong(addr + i, 0L);
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.writeVolatileDouble(addr + i, 0);
    }

    @Test
    public void addInt() {
        for (int i = 0; i <= 64; i += 4)
            INSTANCE.addInt(addr + i, 0);
    }

    @Test
    public void addLong() {
        for (int i = 0; i <= 64; i += 8)
            INSTANCE.addLong(addr + i, 0);
    }

    @Test
    public void is7BitBytes() {
        for (int i = 0; i <= 64; i++) {
            byte[] bytes = new byte[i];
            assertTrue(INSTANCE.is7Bit(bytes, 0, i));
            if (i == 0)
                continue;
            bytes[i - 1] = -1;
            assertFalse(INSTANCE.is7Bit(bytes, 0, i));
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
                assertTrue(INSTANCE.is7Bit(bytes, start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        INSTANCE.is7Bit(bytes, start, length));
        }
    }

    @Test
    public void is7BitChars() {
        for (int i = 0; i <= 64; i++) {
            char[] chars = new char[i];
            assertTrue(INSTANCE.is7Bit(chars, 0, i));
            if (i == 0)
                continue;
            chars[i - 1] = 0x8000;
            assertFalse(INSTANCE.is7Bit(chars, 0, i));
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
                assertTrue(INSTANCE.is7Bit(chars, start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        INSTANCE.is7Bit(chars, start, length));
        }
    }

    @Test
    public void is7BitAddr() {
        final long addr = UNSAFE.allocateMemory(64);
        assertTrue(INSTANCE.is7Bit(addr, 0));
        for (int i = 1; i <= 64; i++) {
            UNSAFE.putByte(addr + i - 1, (byte) -1);
            assertFalse(INSTANCE.is7Bit(addr, i));
            UNSAFE.putByte(addr + i - 1, (byte) 0);
        }
        UNSAFE.freeMemory(addr);
    }

    @Test
    public void is7BitAddr2() {
        final long addr = UNSAFE.allocateMemory(256);
        for (int i = 0; i < 256; i++)
            UNSAFE.putByte(addr + i, (byte) i);

        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = rand.nextInt(256);
            int b = rand.nextInt(256);
            int start = Math.min(a, b);
            int length = Math.abs(a - b);
            if (length == 0)
                assertTrue(INSTANCE.is7Bit(addr + start, length));
            else
                assertEquals("start: " + start + ", length: " + length, start + length <= 128,
                        INSTANCE.is7Bit(addr + start, length));
        }
        UNSAFE.freeMemory(addr);
    }
}