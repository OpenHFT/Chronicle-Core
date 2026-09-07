/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.testimpl;

import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 0, includeNewer = true, includeOlder = true)
public class AllowedCleaner implements ByteBufferCleanerService {
    @Override
    public Impact impact() {
        return Impact.NO_IMPACT;
    }

    @Override
    public void clean(ByteBuffer buffer) {
        // no-op for tests
    }
}

