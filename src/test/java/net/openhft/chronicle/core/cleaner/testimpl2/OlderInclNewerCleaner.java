/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.testimpl2;

import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 8, includeNewer = true)
public class OlderInclNewerCleaner implements ByteBufferCleanerService {
    @Override
    public Impact impact() { return Impact.NO_IMPACT; }
    @Override
    public void clean(ByteBuffer buffer) { }
}

