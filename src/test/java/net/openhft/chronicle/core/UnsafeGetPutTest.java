package net.openhft.chronicle.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnsafeGetPutTest {

    private static final UnsafeMemory MEMORY = new UnsafeMemory();

    private static class Holder {
        byte b;
        short s;
        int i;
        long l;
    }

    private static final long BYTE_OFFSET;
    private static final long SHORT_OFFSET;
    private static final long INT_OFFSET;
    private static final long LONG_OFFSET;

    static {
        try {
            BYTE_OFFSET = MEMORY.objectFieldOffset(Holder.class.getDeclaredField("b"));
            SHORT_OFFSET = MEMORY.objectFieldOffset(Holder.class.getDeclaredField("s"));
            INT_OFFSET = MEMORY.objectFieldOffset(Holder.class.getDeclaredField("i"));
            LONG_OFFSET = MEMORY.objectFieldOffset(Holder.class.getDeclaredField("l"));
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testByte(boolean offHeap) {
        final byte v1 = 0x12;
        final byte v2 = (byte) 0xA5;

        if (offHeap) {
            long addr = UnsafeMemory.UNSAFE.allocateMemory(Byte.BYTES);
            try {
                UnsafeMemory.unsafePutByte(addr, v1);
                assertEquals(v1, UnsafeMemory.unsafeGetByte(addr));
                UnsafeMemory.unsafePutByte(addr, v2);
                assertEquals(v2, UnsafeMemory.unsafeGetByte(addr));
            } finally {
                UnsafeMemory.UNSAFE.freeMemory(addr);
            }
        } else {
            Holder h = new Holder();
            UnsafeMemory.unsafePutByte(h, BYTE_OFFSET, v1);
            assertEquals(v1, UnsafeMemory.unsafeGetByte(h, BYTE_OFFSET));
            UnsafeMemory.unsafePutByte(h, BYTE_OFFSET, v2);
            assertEquals(v2, UnsafeMemory.unsafeGetByte(h, BYTE_OFFSET));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testShort(boolean offHeap) {
        final short v1 = 0x1234;
        final short v2 = (short) -1234;

        if (offHeap) {
            long addr = UnsafeMemory.UNSAFE.allocateMemory(Short.BYTES);
            try {
                UnsafeMemory.unsafePutShort(null, addr, v1);
                assertEquals(v1, UnsafeMemory.unsafeGetShort(null, addr));
                UnsafeMemory.unsafePutShort(null, addr, v2);
                assertEquals(v2, UnsafeMemory.unsafeGetShort(null, addr));
            } finally {
                UnsafeMemory.UNSAFE.freeMemory(addr);
            }
        } else {
            Holder h = new Holder();
            UnsafeMemory.unsafePutShort(h, SHORT_OFFSET, v1);
            assertEquals(v1, UnsafeMemory.unsafeGetShort(h, SHORT_OFFSET));
            UnsafeMemory.unsafePutShort(h, SHORT_OFFSET, v2);
            assertEquals(v2, UnsafeMemory.unsafeGetShort(h, SHORT_OFFSET));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testInt(boolean offHeap) {
        final int v1 = 0x12345678;
        final int v2 = -12345678;

        if (offHeap) {
            long addr = UnsafeMemory.UNSAFE.allocateMemory(Integer.BYTES);
            try {
                UnsafeMemory.unsafePutInt(addr, v1);
                assertEquals(v1, UnsafeMemory.unsafeGetInt(addr));
                UnsafeMemory.unsafePutInt(addr, v2);
                assertEquals(v2, UnsafeMemory.unsafeGetInt(addr));
            } finally {
                UnsafeMemory.UNSAFE.freeMemory(addr);
            }
        } else {
            Holder h = new Holder();
            UnsafeMemory.unsafePutInt(h, INT_OFFSET, v1);
            assertEquals(v1, UnsafeMemory.unsafeGetInt(h, INT_OFFSET));
            UnsafeMemory.unsafePutInt(h, INT_OFFSET, v2);
            assertEquals(v2, UnsafeMemory.unsafeGetInt(h, INT_OFFSET));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testLong(boolean offHeap) {
        final long v1 = 0x0123456789ABCDEFL;
        final long v2 = -9876543210L;

        if (offHeap) {
            long addr = UnsafeMemory.UNSAFE.allocateMemory(Long.BYTES);
            try {
                UnsafeMemory.unsafePutLong(addr, v1);
                assertEquals(v1, UnsafeMemory.unsafeGetLong(addr));
                UnsafeMemory.unsafePutLong(addr, v2);
                assertEquals(v2, UnsafeMemory.unsafeGetLong(addr));
            } finally {
                UnsafeMemory.UNSAFE.freeMemory(addr);
            }
        } else {
            Holder h = new Holder();
            UnsafeMemory.unsafePutLong(h, LONG_OFFSET, v1);
            assertEquals(v1, UnsafeMemory.unsafeGetLong(h, LONG_OFFSET));
            UnsafeMemory.unsafePutLong(h, LONG_OFFSET, v2);
            assertEquals(v2, UnsafeMemory.unsafeGetLong(h, LONG_OFFSET));
        }
    }
}
