package net.openhft.chronicle.core.io;

import org.junit.Test;

import java.nio.ByteBuffer;

public class IOToolsTest {
    @Test
    public void shouldCleanDirectBuffer() throws Exception {
        IOTools.clean(ByteBuffer.allocateDirect(64));
    }
}