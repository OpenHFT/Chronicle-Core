/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class ByteBufferCleanerServiceTest {

    @Test
    public void jdk9CleanerHasNoImpactAndCleans() {
        Assumptions.assumeTrue(Jvm.isJava9Plus(), "JDK9+ required for Jdk9ByteBufferCleanerService");
        ByteBufferCleanerService service = new Jdk9ByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(64);

        service.clean(buffer);

        assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, service.impact(), "jdk9CleanerHasNoImpactAndCleans: L25");
        assertTrue(buffer.isDirect(), "jdk9CleanerHasNoImpactAndCleans: L26");
    }

    @Test
    public void reflectionBasedCleanerDoesNotThrow() {
        ReflectionBasedByteBufferCleanerService service = new ReflectionBasedByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(32);

        service.clean(buffer);

        assertNotNull(service.impact(), "Impact should always be reported");
        assertTrue(buffer.isDirect(), "reflectionBasedCleanerDoesNotThrow: L37");
    }
}
