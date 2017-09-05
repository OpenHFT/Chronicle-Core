package net.openhft.chronicle.core.cleaner.impl.reflect;

import org.junit.Test;

import java.nio.ByteBuffer;

public class ReflectionBasedByteBufferCleanerServiceTest {
    @Test
    public void shouldCleanBuffer() throws Exception {
        new ReflectionBasedByteBufferCleanerService().clean(ByteBuffer.allocateDirect(64));
    }
}