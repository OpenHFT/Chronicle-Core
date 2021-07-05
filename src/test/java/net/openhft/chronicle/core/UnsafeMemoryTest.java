package net.openhft.chronicle.core;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UnsafeMemoryTest {
    private final UnsafeMemory memory;
    private Boolean onHeap;
    private Object object;
    private long addr;

    public UnsafeMemoryTest(String name, UnsafeMemory memory, Boolean onHeap) {
        this.memory = memory;
        this.onHeap = onHeap;
        if (Boolean.TRUE.equals(onHeap)) {
            object = new byte[128];
            addr = UnsafeMemory.MEMORY.arrayBaseOffset(byte[].class);
        } else {
            object = null;
            addr = UNSAFE.allocateMemory(128);
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        UnsafeMemory memory1 = new UnsafeMemory();
        UnsafeMemory.ARMMemory memory2 = new UnsafeMemory.ARMMemory();
        Object[][] intel = {
                {"UnsafeMemory offheap", memory1, false},
                {"UnsafeMemory onheap", memory1, true},
                {"UnsafeMemory offheap (null)", memory1, null}};
        Object[][] arm = {
                {"ARMMemory offheap", memory2, false},
                {"ARMMemory onheap", memory2, true},
                {"ARMMemory offheap (null)", memory2, null}
        };
        List<Object[]> all = new ArrayList<>();
        Collections.addAll(all, intel);
        Collections.addAll(all, arm);
        return Jvm.isArm() ? Arrays.asList(arm) : all;
    }

    @After
    public void tearDown() {
        if (object == null)
            UNSAFE.freeMemory(addr);
    }

    @Test
    public void writeShort() {
        for (int i = 0; i <= 64; i++) {
            if (onHeap == null)
                memory.writeShort(addr + i, (short) 0);
            else
                memory.writeShort(object, addr + i, (short) 0);
        }
    }

    @Test
    public void readShort() {
        if (onHeap == null) {
            memory.writeLong(addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(addr));
            assertEquals((short) 0xABCD, memory.readShort(addr + 1));
        } else {
            memory.writeLong(object, addr, 0x123456789ABCDEFL);
            assertEquals((short) 0xCDEF, memory.readShort(object, addr));
            assertEquals((short) 0xABCD, memory.readShort(object, addr + 1));
        }
    }

    @Test
    public void writeInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, 0);
            } else {
                memory.writeInt(object, addr + i, 0);
            }
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, 0);
            } else {
                memory.writeOrderedInt(object, addr + i, 0);
            }
    }

    @Test
    public void readInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.readInt(addr + i);
            } else {
                memory.readInt(object, addr + i);
            }
    }

    @Test
    public void writeLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, 0);
            } else {
                memory.writeLong(object, addr + i, 0);
            }
    }

    @Test
    public void readLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.readLong(addr + i);
            } else {
                memory.readLong(object, addr + i);
            }
    }

    @Test
    public void writeFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, 0);
            } else {
                memory.writeFloat(object, addr + i, 0);
            }
    }

    @Test
    public void readFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.readFloat(addr + i);
            } else {
                memory.readFloat(object, addr + i);
            }
    }

    @Test
    public void writeDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, 0);
            } else {
                memory.writeDouble(object, addr + i, 0);
            }
    }

    @Test
    public void readDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.readDouble(addr + i);
            } else {
                memory.readDouble(object, addr + i);
            }
    }

    @Test
    public void writeOrderedLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, 0);
            } else {
                memory.writeOrderedLong(object, addr + i, 0);
            }
    }

    @Test
    public void compareAndSwapInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.compareAndSwapInt(addr + i, 0, 0);
            } else {
                memory.compareAndSwapInt(object, addr + i, 0, 0);
            }
    }

    @Test
    public void compareAndSwapLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.compareAndSwapLong(addr + i, 0, 0);
            } else {
                memory.compareAndSwapLong(object, addr + i, 0, 0);
            }
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.readVolatileByte(addr + i);
            } else {
                memory.readVolatileByte(object, addr + i);
            }
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.readVolatileShort(addr + i);
            } else {
                memory.readVolatileShort(object, addr + i);
            }
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.readVolatileInt(addr + i);
            } else {
                memory.readVolatileInt(object, addr + i);
            }
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.readVolatileFloat(addr + i);
            } else {
                memory.readVolatileFloat(object, addr + i);
            }
    }

    @Test
    public void readVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.readVolatileLong(addr + i);
            } else {
                memory.readVolatileLong(object, addr + i);
            }
    }

    @Test
    public void readVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.readVolatileDouble(addr + i);
            } else {
                memory.readVolatileDouble(object, addr + i);
            }
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, (byte) 0);
            } else {
                memory.writeVolatileByte(object, addr + i, (byte) 0);
            }
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, (short) 0);
            } else {
                memory.writeVolatileShort(object, addr + i, (short) 0);
            }
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, 0);
            } else {
                memory.writeVolatileInt(object, addr + i, 0);
            }
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, 0);
            } else {
                memory.writeVolatileFloat(object, addr + i, 0);
            }
    }

    @Test
    public void writeVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, 0L);
            } else {
                memory.writeVolatileLong(object, addr + i, 0L);
            }
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, 0);
            } else {
                memory.writeVolatileDouble(object, addr + i, 0);
            }
    }

    @Test
    public void addInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.addInt(addr + i, 0);
            } else {
                memory.addInt(object, addr + i, 0);
            }
    }

    @Test
    public void addLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.addLong(addr + i, 0);
            } else {
                memory.addLong(object, addr + i, 0);
            }
    }
}