package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assume.assumeTrue;

public class Jdk9ByteBufferCleanerServiceTest {
    @Test
    public void shouldCleanBuffer() {
        assumeTrue(Jvm.isJava9Plus());
        new Jdk9ByteBufferCleanerService().clean(ByteBuffer.allocateDirect(64));
    }
}