package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assume.assumeFalse;

public class ReflectionBasedByteBufferCleanerServiceTest {
    @Test
    public void shouldCleanBuffer() {
        assumeFalse(Jvm.isJava12Plus());
        new ReflectionBasedByteBufferCleanerService().clean(ByteBuffer.allocateDirect(64));
    }
}