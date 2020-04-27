package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {
    private static final String JDK8_CLEANER_CLASS_NAME = "sun.misc.Cleaner";
    private static final String JDK9_CLEANER_CLASS_NAME = "jdk.internal.ref.Cleaner";

    private static final MethodHandle cleanerMethod, cleanMethod;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        final String cleanerClassname = Jvm.isJava9Plus() ?
                JDK9_CLEANER_CLASS_NAME : JDK8_CLEANER_CLASS_NAME;
        try {
            cleanerMethod = lookup.findVirtual(DirectBuffer.class, "cleaner", MethodType.methodType(Cleaner.class));
            cleanMethod = lookup.findVirtual(Class.forName(cleanerClassname), "clean", MethodType.methodType(void.class));
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void clean(final ByteBuffer buffer) {
        try {
            Object cleaner = cleanerMethod.invoke((DirectBuffer) buffer);
            cleanMethod.invoke(cleaner);
        } catch (Throwable throwable) {
            Jvm.rethrow(throwable);
        }
    }

    @Override
    public int impact() {
        return SOME_IMPACT;
    }
}
