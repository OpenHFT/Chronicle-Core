package net.openhft.chronicle.core.cleaner.impl;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class CleanerTestUtil {

    private CleanerTestUtil() {
    }

    public static void test(final Consumer<ByteBuffer> cleaner) {
        requireNonNull(cleaner);
        try {
            final AtomicLong reservedMemory;

            // Unable to reflect on Java17+
            if (Jvm.majorVersion() < 16) {
                Class<?> bitsClass = Class.forName("java.nio.Bits");
                Field field;
                try {
                    field = bitsClass.getDeclaredField("RESERVED_MEMORY");
                } catch (NoSuchFieldException nfe) {
                    // Java8 name
                    field = bitsClass.getDeclaredField("reservedMemory");
                }
                field.setAccessible(true);
                reservedMemory = (AtomicLong) field.get(null);
            } else {
                // Just assume zero...
                reservedMemory = new AtomicLong();
            }
            long allocatedBefore = reservedMemory.get();
            final ByteBuffer bb = ByteBuffer.allocateDirect(64);
            cleaner.accept(bb);
            long allocatedAfter = reservedMemory.get();

            // GC may clean up other lingering ByteBuffers
            assertTrue(allocatedBefore <= allocatedAfter);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

}
