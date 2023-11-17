package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
/**
 * This class provides methods to obtain the size of the object header and the base offset for arrays in the JVM.
 * It uses the Unsafe API to calculate these values, which are essential for low-level memory operations.
 */
public final class ObjectHeaderSizeHolder {

    private static final int OBJECT_HEADER_SIZE;

    static {
        try {
            final Field aField = ObjectHeaderSizeHolder.class.getDeclaredField("firstField");
            OBJECT_HEADER_SIZE = (int) UnsafeMemory.INSTANCE.getFieldOffset(aField);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @UsedViaReflection
    int firstField;

    private ObjectHeaderSizeHolder() {
    }

    /**
     * Returns the size of the object header in the JVM.
     * This size is calculated based on the offset of the first field of this class.
     *
     * @return The size of the object header.
     */
    public static int getSize() {
        return OBJECT_HEADER_SIZE;
    }

    /**
     * Calculates the object header size for a given class type.
     * If the class type is an array, it returns the array base offset; otherwise, it returns the object header size.
     *
     * @param type The class for which the object header size is to be calculated.
     * @return The object header size or array base offset, depending on the class type.
     */
    public static int objectHeaderSize(Class<?> type) {
        return type.isArray() ? UnsafeMemory.UNSAFE.arrayBaseOffset(type) : OBJECT_HEADER_SIZE;
    }
}
