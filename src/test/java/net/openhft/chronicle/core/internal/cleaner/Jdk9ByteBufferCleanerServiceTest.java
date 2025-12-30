/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class Jdk9ByteBufferCleanerServiceTest {

    private Jdk9ByteBufferCleanerService cleanerService;

    @BeforeEach
    void setUp() {
        cleanerService = new Jdk9ByteBufferCleanerService();
    }

    @DisplayName("clean rejects heap buffer without cleaner")
    @Test
    void cleanInvalidByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        assertThrows(Exception.class, () -> cleanerService.clean(buffer),
                "clean should throw when invoked on a non-direct heap buffer");
    }

    @DisplayName("Impact reports no performance impact jdk")
    @Test
    void impactShouldBeNoImpact() {
        assertEquals(Impact.NO_IMPACT, cleanerService.impact(), "cleaner service impact should be NO_IMPACT");
    }
}
