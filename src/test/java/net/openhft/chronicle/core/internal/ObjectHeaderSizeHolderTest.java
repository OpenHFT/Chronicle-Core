package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectHeaderSizeHolderTest {
    @Test
    public void objectHeaderSize() {
        assertEquals(ObjectHeaderSizeHolder.getSize(),
                Jvm.objectHeaderSize(getClass()));
        assertEquals(ObjectHeaderSizeHolder.getSize(),
                Jvm.objectHeaderSize(String.class));

        assertEquals(UnsafeMemory.UNSAFE.ARRAY_BYTE_BASE_OFFSET,
                Jvm.objectHeaderSize(byte[].class));
        // these might need to be 8-byte aligned
        assertEquals(UnsafeMemory.UNSAFE.ARRAY_LONG_BASE_OFFSET,
                Jvm.objectHeaderSize(long[].class));
        assertEquals(UnsafeMemory.UNSAFE.ARRAY_DOUBLE_BASE_OFFSET,
                Jvm.objectHeaderSize(double[].class));
        assertEquals(UnsafeMemory.UNSAFE.ARRAY_OBJECT_BASE_OFFSET,
                Jvm.objectHeaderSize(String[].class));
    }

    @Test
    public void getSizeShouldReturnPositiveValue() {
        int size = ObjectHeaderSizeHolder.getSize();
        assertTrue(size > 0, "Object header size should be positive");
    }

    @Test
    public void objectHeaderSizeShouldReturnPositiveForNonArrayClass() {
        int size = ObjectHeaderSizeHolder.objectHeaderSize(Object.class);
        assertTrue(size > 0, "Object header size for non-array class should be positive");
    }

    @Test
    public void objectHeaderSizeShouldReturnPositiveForArrayClass() {
        int size = ObjectHeaderSizeHolder.objectHeaderSize(int[].class);
        assertTrue(size >= 0, "Array base offset for array class should be non-negative");
    }
}