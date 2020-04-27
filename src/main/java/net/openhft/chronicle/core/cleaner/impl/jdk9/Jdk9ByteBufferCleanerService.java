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
    private static final MethodHandle invokeCleanerMethod = getInvokeCleanerMethod();

    private static MethodHandle getInvokeCleanerMethod() {
        MethodType signature = MethodType.methodType(void.class, ByteBuffer.class);
        try {
            return MethodHandles.publicLookup().findVirtual(UnsafeMemory.UNSAFE.getClass(), "invokeCleaner", signature);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            if (Jvm.isJava9Plus()) {
                throw new ExceptionInInitializerError(e);
            }
            return null;
        }
    }

    @Override
    public void clean(final ByteBuffer buffer) {
        try {
            invokeCleanerMethod.invokeExact(UnsafeMemory.UNSAFE, buffer);
        } catch (Throwable throwable) {
            Jvm.rethrow(throwable);
        }
    }

    @Override
    public int impact() {
        // invokeExact() + `static final` method handle = inlined to vanilla method call
        return NO_IMPACT;
    }
}
