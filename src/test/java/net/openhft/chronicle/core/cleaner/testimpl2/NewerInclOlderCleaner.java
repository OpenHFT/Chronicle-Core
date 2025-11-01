/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.testimpl2;

import net.openhft.chronicle.core.annotation.TargetMajorVersion;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.nio.ByteBuffer;

@TargetMajorVersion(majorVersion = 21, includeOlder = true)
public class NewerInclOlderCleaner implements ByteBufferCleanerService {
    @Override
    public Impact impact() { return Impact.SOME_IMPACT; }
    @Override
    public void clean(ByteBuffer buffer) { }
}

