/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.reflect;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionBasedByteBufferCleanerServiceTest extends CoreTestCommon {
    @DisplayName("cleaning buffer leaves reserved memory non-decreasing")
    @Test
    void shouldCleanBuffer() {
        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.captureReservedMemorySnapshot(new ReflectionBasedByteBufferCleanerService()::clean);
        assertTrue(snapshot.before <= snapshot.after,
                "reserved memory should not decrease after clean, before=" + snapshot.before + ", after=" + snapshot.after);
    }
}
