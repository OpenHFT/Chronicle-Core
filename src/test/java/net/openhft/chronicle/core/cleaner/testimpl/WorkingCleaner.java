/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.testimpl;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;

import java.nio.ByteBuffer;

/** Test cleaner that genuinely frees the buffer, on both Java 8 and 9+. Not registered via META-INF/services. */
@TargetMajorVersion(majorVersion = 0, includeNewer = true, includeOlder = true)
public class WorkingCleaner implements ByteBufferCleanerService {
    // Jdk9 (Unsafe.invokeCleaner) works on 9+; the reflection cleaner (sun.misc.Cleaner) works on 8.
    private static final ByteBufferCleanerService DELEGATE = Jvm.isJava9Plus()
            ? new Jdk9ByteBufferCleanerService()
            : new ReflectionBasedByteBufferCleanerService();

    @Override
    public Impact impact() {
        return Impact.NO_IMPACT;
    }

    @Override
    public void clean(ByteBuffer buffer) {
        DELEGATE.clean(buffer);
    }
}
