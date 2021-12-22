package net.openhft.chronicle.core;

import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UnsafeMemoryTest {

    private static final float EPSILON = 1e-7f;

    private static final byte BYTE_VAL = Byte.MAX_VALUE;
    private static final short SHORT_VAL = Short.MAX_VALUE;
    private static final int INT_VAL = 0x12345678;
    private static final long LONG_VAL = Long.MAX_VALUE;
    private static final float FLOAT_VAL = 1f;
    private static final double DOUBLE_VAL = 1d;

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
            if (onHeap == null) {
                memory.writeShort(addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(addr + i));
            } else {
                memory.writeShort(object, addr + i, (short) 0xABCD);
                assertEquals((short) 0xABCD, memory.readShort(object, addr + i));
            }
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
    public void readWriteInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void writeOrderedInt() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeOrderedInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeOrderedInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void readWriteLong() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @Test
    public void readWriteFloat() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeFloat(object, addr + i, 1);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @Test
    public void readWriteDouble() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @Test
    public void writeOrderedLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeOrderedLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeOrderedLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @Test
    public void compareAndSwapInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, memory.readInt(addr + i));
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapInt(object, addr + i, 0, INT_VAL);
                    assertTrue(actual);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @Test
    public void compareAndSwapLong() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, memory.readLong(addr + i));
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final boolean actual = memory.compareAndSwapLong(object, addr + i, 0, LONG_VAL);
                    assertTrue(actual);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }

    @Test
    public void readVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeByte(addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(addr + i);
                assertEquals(BYTE_VAL, actual);
            } else {
                memory.writeByte(object, addr + i, BYTE_VAL);
                final byte actual = memory.readVolatileByte(object, addr + i);
                assertEquals(BYTE_VAL, actual);
            }
    }

    @Test
    public void readVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeShort(addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(addr + i);
                assertEquals(SHORT_VAL, actual);
            } else {
                memory.writeShort(object, addr + i, SHORT_VAL);
                final short actual = memory.readVolatileShort(object, addr + i);
                assertEquals(SHORT_VAL, actual);
            }
    }

    @Test
    public void readVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeInt(addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(addr + i);
                assertEquals(INT_VAL, actual);
            } else {
                memory.writeInt(object, addr + i, INT_VAL);
                final int actual = memory.readVolatileInt(object, addr + i);
                assertEquals(INT_VAL, actual);
            }
    }

    @Test
    public void readVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeFloat(addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            } else {
                memory.writeFloat(object, addr + i, FLOAT_VAL);
                final float actual = memory.readVolatileFloat(object, addr + i);
                assertEquals(FLOAT_VAL, actual, EPSILON);
            }
    }

    @Test
    public void readVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeLong(addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(addr + i);
                assertEquals(LONG_VAL, actual);
            } else {
                memory.writeLong(object, addr + i, LONG_VAL);
                final long actual = memory.readVolatileLong(object, addr + i);
                assertEquals(LONG_VAL, actual);
            }
    }

    @Test
    public void readVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeDouble(addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            } else {
                memory.writeDouble(object, addr + i, DOUBLE_VAL);
                final double actual = memory.readVolatileDouble(object, addr + i);
                assertEquals(DOUBLE_VAL, actual, EPSILON);
            }
    }

    @Test
    public void writeVolatileByte() {
        for (int i = 0; i <= 64; i++)
            if (onHeap == null) {
                memory.writeVolatileByte(addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(addr + i));
            } else {
                memory.writeVolatileByte(object, addr + i, BYTE_VAL);
                assertEquals(BYTE_VAL, memory.readByte(object, addr + i));
            }
    }

    @Test
    public void writeVolatileShort() {
        for (int i = 0; i <= 64; i += 2)
            if (onHeap == null) {
                memory.writeVolatileShort(addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(addr + i));
            } else {
                memory.writeVolatileShort(object, addr + i, SHORT_VAL);
                assertEquals(SHORT_VAL, memory.readShort(object, addr + i));
            }
    }

    @Test
    public void writeVolatileInt() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileInt(addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(addr + i));
            } else {
                memory.writeVolatileInt(object, addr + i, INT_VAL);
                assertEquals(INT_VAL, memory.readInt(object, addr + i));
            }
    }

    @Test
    public void writeVolatileFloat() {
        for (int i = 0; i <= 64; i += 4)
            if (onHeap == null) {
                memory.writeVolatileFloat(addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(addr + i), EPSILON);
            } else {
                memory.writeVolatileFloat(object, addr + i, FLOAT_VAL);
                assertEquals(FLOAT_VAL, memory.readFloat(object, addr + i), EPSILON);
            }
    }

    @Test
    public void writeVolatileLong() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileLong(addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(addr + i));
            } else {
                memory.writeVolatileLong(object, addr + i, LONG_VAL);
                assertEquals(LONG_VAL, memory.readLong(object, addr + i));
            }
    }

    @Test
    public void writeVolatileDouble() {
        for (int i = 0; i <= 64; i += 8)
            if (onHeap == null) {
                memory.writeVolatileDouble(addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(addr + i), EPSILON);
            } else {
                memory.writeVolatileDouble(object, addr + i, DOUBLE_VAL);
                assertEquals(DOUBLE_VAL, memory.readDouble(object, addr + i), EPSILON);
            }
    }

    @Test
    public void addInt() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 4)
            try {
                if (onHeap == null) {
                    memory.writeInt(addr + i, 0);
                    final int actual = memory.addInt(addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, memory.readInt(addr + i));
                } else {
                    memory.writeInt(object, addr + i, 0);
                    final int actual = memory.addInt(object, addr + i, INT_VAL);
                    assertEquals(INT_VAL, actual);
                    assertEquals(INT_VAL, memory.readInt(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedInt(addr + i))
                    throw e;
            }
    }

    @Test
    public void addLong() throws MisAlignedAssertionError {
        for (int i = 0; i <= 64; i += 8)
            try {
                if (onHeap == null) {
                    memory.writeLong(addr + i, 0);
                    final long actual = memory.addLong(addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, memory.readLong(addr + i));
                } else {
                    memory.writeLong(object, addr + i, 0);
                    final long actual = memory.addLong(object, addr + i, LONG_VAL);
                    assertEquals(LONG_VAL, actual);
                    assertEquals(LONG_VAL, memory.readLong(object, addr + i));
                }
            } catch (MisAlignedAssertionError e) {
                if (memory.safeAlignedLong(addr + i))
                    throw e;
            }
    }
}