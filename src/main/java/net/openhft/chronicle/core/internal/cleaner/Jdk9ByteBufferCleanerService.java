/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * A {@link ByteBufferCleanerService} implementation for Java 9 and above that uses the {@link sun.misc.Unsafe#invokeCleaner(ByteBuffer)}
 * method to clean up direct byte buffers.
 * <p>
 * This service leverages the {@link UnsafeMemory#UNSAFE} object to invoke the cleaner method using reflection,
 * allowing compatibility with both Java 8 and Java 9+ environments. It ensures that memory associated with direct byte buffers
 * is properly released.
 * </p>
 */
@TargetMajorVersion(majorVersion = 9, includeNewer = true)
public final class Jdk9ByteBufferCleanerService implements ByteBufferCleanerService {

    // MethodHandle for invoking the invokeCleaner method from Unsafe
    private static final MethodHandle INVOKE_CLEANER_METHOD = getInvokeCleanerMethod();

    /**
     * Retrieves the {@code invokeCleaner} method from {@link sun.misc.Unsafe} using reflection and method handles.
     * This method is available in Java 9 and above.
     *
     * @return A {@link MethodHandle} for the {@code invokeCleaner} method.
     */
    private static MethodHandle getInvokeCleanerMethod() {
        if (!Jvm.isJava9Plus()) {
            return null;
        }
        // Access invokeCleaner() reflectively to support compilation with JDK 8
        MethodType signature = MethodType.methodType(void.class, ByteBuffer.class);
        try {
            // Use MethodHandles to lookup the invokeCleaner method from Unsafe
            return MethodHandles.publicLookup().findVirtual(UnsafeMemory.UNSAFE.getClass(), "invokeCleaner", signature);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Cleans the specified direct {@link ByteBuffer} by invoking the {@code invokeCleaner} method from {@link UnsafeMemory#UNSAFE}.
     * <p>
     * This method ensures that the memory associated with the direct buffer is properly released to avoid memory leaks.
     * </p>
     *
     * @param buffer The direct {@link ByteBuffer} to clean.
     * @throws RuntimeException If any error occurs during the invocation of the cleaner method.
     */
    @Override
    public void clean(final ByteBuffer buffer) {
        try {
            // Invoke the cleaner method using the method handle
            INVOKE_CLEANER_METHOD.invokeExact(UnsafeMemory.UNSAFE, buffer);
        } catch (Throwable throwable) {
            throw Jvm.rethrow(throwable);
        }
    }

    /**
     * Indicates the impact of using this cleaner service. Since the {@code invokeExact} method on a static final
     * {@link MethodHandle} is inlined by the JVM, the impact is minimal.
     *
     * @return {@link Impact#NO_IMPACT}, indicating that the cleaner has no significant impact on performance.
     */
    @Override
    public Impact impact() {
        // invokeExact() on `static final` method handle is inlined to vanilla method call
        return Impact.NO_IMPACT;
    }
}
