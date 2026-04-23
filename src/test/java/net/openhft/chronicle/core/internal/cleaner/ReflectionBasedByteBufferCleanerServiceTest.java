/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedByteBufferCleanerServiceTest {

    private final ReflectionBasedByteBufferCleanerService cleanerService = new ReflectionBasedByteBufferCleanerService();

    @Test
    void cleanShouldWorkOnCurrentJvm() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        String javaFamily = Jvm.isJava9Plus() ? "Java 9+" : "Java 8";
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw on " + javaFamily);
    }

    @Test
    void impactShouldReturnValidImpact() {
        Impact impact = cleanerService.impact();
        assertTrue(impact == Impact.SOME_IMPACT || impact == Impact.UNAVAILABLE, "Impact should be either SOME_IMPACT or UNAVAILABLE");
    }
}
