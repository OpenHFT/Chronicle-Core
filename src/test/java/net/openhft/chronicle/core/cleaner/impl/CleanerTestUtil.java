/*
 * Copyright 2016-2022 chronicle.software
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

package net.openhft.chronicle.core.cleaner.impl;

import net.openhft.chronicle.core.Jvm;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class CleanerTestUtil {

    private CleanerTestUtil() {
    }

    public static void test(final Consumer<ByteBuffer> cleaner) {
        requireNonNull(cleaner);
        try {
            final AtomicLong reservedMemory;

            // Unable to reflect on Java17+
            if (Jvm.majorVersion() < 16) {
                Class<?> bitsClass = Class.forName("java.nio.Bits");
                Field field;
                try {
                    field = bitsClass.getDeclaredField("RESERVED_MEMORY");
                } catch (NoSuchFieldException nfe) {
                    // Java8 name
                    field = bitsClass.getDeclaredField("reservedMemory");
                }
                field.setAccessible(true);
                reservedMemory = (AtomicLong) field.get(null);
            } else {
                // Just assume zero...
                reservedMemory = new AtomicLong();
            }
            long allocatedBefore = reservedMemory.get();
            final ByteBuffer bb = ByteBuffer.allocateDirect(64);
            cleaner.accept(bb);
            long allocatedAfter = reservedMemory.get();

            // GC may clean up other lingering ByteBuffers
            assertTrue(allocatedBefore <= allocatedAfter);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }
}
