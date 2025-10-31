/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
