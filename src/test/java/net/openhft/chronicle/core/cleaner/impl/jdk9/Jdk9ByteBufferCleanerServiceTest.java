package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.Jvm;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Jdk9ByteBufferCleanerServiceTest {
    @Test
    public void shouldCleanBuffer() {
        assumeTrue(Jvm.isJava9Plus());
        assertDoesNotThrow(() ->
                new Jdk9ByteBufferCleanerService().clean(ByteBuffer.allocateDirect(64))
        );
    }
}