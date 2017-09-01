package net.openhft.chronicle.core.cleaner.impl.jdk8;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionBasedByteBufferCleanerService.class);
    private static final String CLEANER_CLASS_NAME = "sun.misc.Cleaner";

    @Override
    public void clean(final ByteBuffer buffer) {
        final Method cleanerMethod;
        try {
            cleanerMethod = DirectBuffer.class.getDeclaredMethod("cleaner");
            final Object cleaner = cleanerMethod.invoke(buffer);
            final Method cleanMethod = Class.forName(CLEANER_CLASS_NAME).getDeclaredMethod("clean");
            cleanMethod.invoke(cleaner);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            LOGGER.warn("Failed to clean buffer", e);
        }
    }

    @Override
    public int impact() {
        return SOME_IMPACT;
    }
}
