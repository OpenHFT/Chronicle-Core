/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReflectionBasedByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    public void shouldCleanBuffer() {
        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.test(new ReflectionBasedByteBufferCleanerService()::clean);
        assertTrue(snapshot.before <= snapshot.after,
                "shouldCleanBuffer: reservedMemory before=" + snapshot.before + ", after=" + snapshot.after);
    }
}
