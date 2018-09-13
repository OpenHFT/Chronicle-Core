package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnsafeMemoryTest {
    static final long addr = UnsafeMemory.UNSAFE.allocateMemory(128);

    @Test
    public void writeShort() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeShort(addr + i, (short) 0);
    }

    @Test
    public void readShort() {
        UnsafeMemory.INSTANCE.writeLong(addr, 0x123456789ABCDEFL);
        assertEquals((short) 0xCDEF, UnsafeMemory.INSTANCE.readShort(addr));
        assertEquals((short) 0xABCD, UnsafeMemory.INSTANCE.readShort(addr + 1));
    }

    @Test
    public void writeInt() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeInt(addr + i, 0);
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeOrderedInt(addr + i, 0);
    }

    @Test
    public void readInt() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.readInt(addr + i);
    }

    @Test
    public void writeLong() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeLong(addr + i, 0);
    }

    @Test
    public void readLong() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.readLong(addr + i);
    }

    @Test
    public void writeFloat() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeFloat(addr + i, 0);
    }

    @Test
    public void readFloat() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.readFloat(addr + i);
    }

    @Test
    public void writeDouble() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeDouble(addr + i, 0);
    }

    @Test
    public void readDouble() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.readDouble(addr + i);
    }

    @Test
    public void writeOrderedLong() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.writeOrderedLong(addr + i, 0);
    }

    @Test
    public void compareAndSwapInt() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.compareAndSwapInt(addr + i, 0, 0);
    }

    @Test
    public void compareAndSwapLong() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.compareAndSwapLong(addr + i, 0, 0);
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.readVolatileByte(addr + i);
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            UnsafeMemory.INSTANCE.readVolatileShort(addr + i);
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.readVolatileInt(addr + i);
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.readVolatileFloat(addr + i);
    }

    @Test
    public void readVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.readVolatileLong(addr + i);
    }

    @Test
    public void readVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.readVolatileDouble(addr + i);
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            UnsafeMemory.INSTANCE.writeVolatileByte(addr + i, (byte) 0);
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            UnsafeMemory.INSTANCE.writeVolatileShort(addr + i, (short) 0);
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.writeVolatileInt(addr + i, 0);
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.writeVolatileFloat(addr + i, 0);
    }

    @Test
    public void writeVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.writeVolatileLong(addr + i, 0L);
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.writeVolatileDouble(addr + i, 0);
    }

    @Test
    public void addInt() {
        for (int i = 0; i <= 64; i += 4)
            UnsafeMemory.INSTANCE.addInt(addr + i, 0);
    }

    @Test
    public void addLong() {
        for (int i = 0; i <= 64; i += 8)
            UnsafeMemory.INSTANCE.addLong(addr + i, 0);
    }
}