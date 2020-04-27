package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 9)
public final class Jdk9ByteBufferCleanerService implements ByteBufferCleanerService {
    private static final MethodHandle invokeCleaner_Method = get_invokeCleaner_Method();

    private static MethodHandle get_invokeCleaner_Method() {
        if (!Jvm.isJava9Plus()) {
            return null;
        }
        // Access invokeCleaner() reflectively to support compilation with JDK 8
        MethodType signature = MethodType.methodType(void.class, ByteBuffer.class);
        try {
            return MethodHandles.publicLookup().findVirtual(UnsafeMemory.UNSAFE.getClass(), "invokeCleaner", signature);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void clean(final ByteBuffer buffer) {
        try {
            invokeCleaner_Method.invokeExact(UnsafeMemory.UNSAFE, buffer);
        } catch (Throwable throwable) {
            Jvm.rethrow(throwable);
        }
    }

    @Override
    public int impact() {
        // invokeExact() on `static final` method handle is inlined to vanilla method call
        return NO_IMPACT;
    }
}
