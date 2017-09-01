package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionBasedByteBufferCleanerService.class);
    private static final String JDK8_CLEANER_CLASS_NAME = "sun.misc.Cleaner";
    private static final String JDK9_CLEANER_CLASS_NAME = "jdk.internal.ref.Cleaner";

    @Override
    public void clean(final ByteBuffer buffer) {
        final Method cleanerMethod;
        try {
            cleanerMethod = DirectBuffer.class.getDeclaredMethod("cleaner");
            log("cleaner method: %s%n", cleanerMethod);
            final Object cleaner = cleanerMethod.invoke(buffer);
            log("cleaner: %s%n", cleaner);
            final String cleanerClassname = Jvm.isJava9Plus() ? JDK9_CLEANER_CLASS_NAME : JDK8_CLEANER_CLASS_NAME;

            log("cleaner class name: %s%n", cleanerClassname);
            final Method cleanMethod = Class.forName(cleanerClassname).getDeclaredMethod("clean");
            log("clean method: %s%n", cleanMethod);

            cleanMethod.invoke(cleaner);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            LOGGER.warn("Failed to clean buffer", e);
        }
    }

    private void log(final String fmt, final Object arg) {
        System.out.printf(fmt, arg);
        LOGGER.warn(String.format(fmt, arg));
    }

    @Override
    public int impact() {
        return SOME_IMPACT;
    }
}
