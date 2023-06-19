package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.UnsafeMemory;

import java.lang.reflect.Field;

public final class ObjectHeaderSizeHolder {
    private static final int OBJECT_HEADER_SIZE;

    static {
        final Field[] declaredFields = ObjectHeaderSizeHolder.class.getDeclaredFields();
        OBJECT_HEADER_SIZE = (int) UnsafeMemory.INSTANCE.getFieldOffset(declaredFields[0]);
    }

    int a;

    private ObjectHeaderSizeHolder() {
    }

    public static int getSize() {
        return OBJECT_HEADER_SIZE;
    }
}
