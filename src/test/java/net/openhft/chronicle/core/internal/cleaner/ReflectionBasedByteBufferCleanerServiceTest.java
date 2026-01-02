/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedByteBufferCleanerServiceTest {

    private final ReflectionBasedByteBufferCleanerService cleanerService = new ReflectionBasedByteBufferCleanerService();

    @Test
    @DisplayName("cleaning direct buffer works across supported runtimes")
    void cleanShouldWorkOnSupportedJdks() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        String version = System.getProperty("java.version");
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw on Java " + version);
    }

    @Test
    @DisplayName("impact reports supported reflection-based outcomes")
    void impactShouldReturnValidImpact() {
        Impact impact = cleanerService.impact();
        assertTrue(impact == Impact.SOME_IMPACT || impact == Impact.UNAVAILABLE, "Impact should be either SOME_IMPACT or UNAVAILABLE");
    }
}
