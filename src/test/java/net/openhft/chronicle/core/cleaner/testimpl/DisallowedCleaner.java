/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.testimpl;

import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 99)
public class DisallowedCleaner implements ByteBufferCleanerService {
    @Override
    public Impact impact() {
        return Impact.SOME_IMPACT;
    }

    @Override
    public void clean(ByteBuffer buffer) {
        // no-op for tests
    }
}

