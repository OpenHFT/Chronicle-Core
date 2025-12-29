/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ByteBufferCleanerServiceTest {

    @DisplayName("JDK9 cleaner has no impact and cleans")
    @Test
    void jdk9CleanerHasNoImpactAndCleans() {
        Assumptions.assumeTrue(Jvm.isJava9Plus(), "JDK9+ required for Jdk9ByteBufferCleanerService");
        ByteBufferCleanerService service = new Jdk9ByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(64);

        service.clean(buffer);

        assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, service.impact(), "impact should return NO_IMPACT for JDK9 cleaner service");
        assertTrue(buffer.isDirect(), "buffer should remain direct after JDK9 cleaner run");
    }

    @DisplayName("reflection-based cleaner completes without throwing")
    @Test
    void reflectionBasedCleanerDoesNotThrow() {
        ReflectionBasedByteBufferCleanerService service = new ReflectionBasedByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(32);

        service.clean(buffer);

        assertNotNull(service.impact(), "Impact should always be reported");
        assertTrue(buffer.isDirect(), "buffer should remain direct after reflection cleaner run");
    }
}
