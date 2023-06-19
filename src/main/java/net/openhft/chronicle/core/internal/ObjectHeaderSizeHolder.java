package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;

import java.lang.reflect.Field;

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

    public static int getSize() {
        return OBJECT_HEADER_SIZE;
    }
}
