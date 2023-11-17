package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectHeaderSizeHolderTest {
    @Test
    public void objectHeaderSize() {
        assertEquals(ObjectHeaderSizeHolder.getSize(),
                Jvm.objectHeaderSize(getClass()));
        assertEquals(ObjectHeaderSizeHolder.getSize(),
                Jvm.objectHeaderSize(String.class));

        assertEquals(ObjectHeaderSizeHolder.getArrayBaseOffset(),
                Jvm.objectHeaderSize(byte[].class));
        assertEquals(ObjectHeaderSizeHolder.getArrayBaseOffset(),
                Jvm.objectHeaderSize(String[].class));
    }
}