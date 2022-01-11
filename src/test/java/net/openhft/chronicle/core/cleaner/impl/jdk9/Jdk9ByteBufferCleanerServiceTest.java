package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class Jdk9ByteBufferCleanerServiceTest {
    @Test
    public void shouldCleanBuffer() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        assumeTrue(Jvm.isJava9Plus());

        CleanerTestUtil.test(new Jdk9ByteBufferCleanerService()::clean);
    }
}