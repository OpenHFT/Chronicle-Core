//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.cleaner;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.Assume;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class ByteBufferCleanerServiceTest {

    @Test
    public void jdk9CleanerHasNoImpactAndCleans() {
        Assume.assumeTrue("JDK9+ required for Jdk9ByteBufferCleanerService", Jvm.isJava9Plus());
        ByteBufferCleanerService service = new Jdk9ByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(64);

        service.clean(buffer);

        assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, service.impact());
        assertTrue(buffer.isDirect());
    }

    @Test
    public void reflectionBasedCleanerDoesNotThrow() {
        ReflectionBasedByteBufferCleanerService service = new ReflectionBasedByteBufferCleanerService();
        ByteBuffer buffer = ByteBuffer.allocateDirect(32);

        service.clean(buffer);

        assertNotNull("Impact should always be reported", service.impact());
        assertTrue(buffer.isDirect());
    }
}
