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
package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import sun.nio.ch.DirectBuffer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

public final class ReflectionBasedByteBufferCleanerService implements ByteBufferCleanerService {
    private static final String JDK8_CLEANER_CLASS_NAME = "sun.misc.Cleaner";
    private static final String JDK9_CLEANER_CLASS_NAME = "jdk.internal.ref.Cleaner";

    private static final MethodHandle CLEANER_METHOD;
    private static final MethodHandle CLEAN_METHOD;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        final String cleanerClassname = Jvm.isJava9Plus() ?
                JDK9_CLEANER_CLASS_NAME : JDK8_CLEANER_CLASS_NAME;
        try {
            final Class<?> cleanerClass = Class.forName(cleanerClassname);
            CLEANER_METHOD = lookup.findVirtual(DirectBuffer.class, "cleaner", MethodType.methodType(cleanerClass));
            CLEAN_METHOD = lookup.findVirtual(cleanerClass, "clean", MethodType.methodType(void.class));
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            Jvm.fatal().on(ReflectionBasedByteBufferCleanerService.class, "Make sure you have set the command line option \"--illegal-access=permit --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\"");
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void clean(final ByteBuffer buffer) {
        try {
            final Object cleaner = CLEANER_METHOD.invoke((DirectBuffer) buffer);
            CLEAN_METHOD.invoke(cleaner);
        } catch (Throwable throwable) {
            Jvm.rethrow(throwable);
        }
    }

    @Override
    public int impact() {
        return SOME_IMPACT;
    }
}