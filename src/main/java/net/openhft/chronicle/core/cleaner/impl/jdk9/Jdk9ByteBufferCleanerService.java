/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.cleaner.impl.jdk9;

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
