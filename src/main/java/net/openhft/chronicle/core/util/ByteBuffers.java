package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public enum ByteBuffers {
    ;
    private static final Field ADDRESS;
    private static final Field CAPACITY;

    static {
        ByteBuffer direct = ByteBuffer.allocateDirect(0);
        ADDRESS = Jvm.getField(direct.getClass(), "address");
        CAPACITY = Jvm.getField(direct.getClass(), "capacity");
    }

    public static void setAddressCapacity(ByteBuffer buffer, long address, long capacity) {
        int cap = Math.toIntExact(capacity);
        try {
            ADDRESS.setLong(buffer, address);
            CAPACITY.setInt(buffer, cap);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
