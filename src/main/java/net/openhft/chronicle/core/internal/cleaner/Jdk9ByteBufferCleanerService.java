/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 9, includeNewer = true)
public final class Jdk9ByteBufferCleanerService implements ByteBufferCleanerService {

    private static final MethodHandle INVOKE_CLEANER_METHOD = getInvokeCleanerMethod();

    private static MethodHandle getInvokeCleanerMethod() {
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
            INVOKE_CLEANER_METHOD.invokeExact(UnsafeMemory.UNSAFE, buffer);
        } catch (Throwable throwable) {
            throw Jvm.rethrow(throwable);
        }
    }

    @Override
    public Impact impact() {
        // invokeExact() on `static final` method handle is inlined to vanilla method call
        return Impact.NO_IMPACT;
    }
}
