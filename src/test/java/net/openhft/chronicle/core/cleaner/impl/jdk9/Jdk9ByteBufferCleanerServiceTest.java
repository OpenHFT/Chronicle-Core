/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner.impl.jdk9;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.internal.cleaner.Jdk9ByteBufferCleanerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Jdk9ByteBufferCleanerServiceTest extends CoreTestCommon {
    @Test
    @DisplayName("cleaning buffer leaves reserved memory non-decreasing")
    void shouldCleanBuffer() {
        assumeTrue(Jvm.isJava9Plus(), "requires Java 9+ for JDK 9 cleaner service");

        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.captureReservedMemorySnapshot(new Jdk9ByteBufferCleanerService()::clean);
        assertTrue(snapshot.before <= snapshot.after,
                "reserved memory should not decrease after clean, before=" + snapshot.before + ", after=" + snapshot.after);
    }
}
