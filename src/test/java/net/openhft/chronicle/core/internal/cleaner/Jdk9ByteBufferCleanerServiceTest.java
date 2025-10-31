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

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class Jdk9ByteBufferCleanerServiceTest {

    private Jdk9ByteBufferCleanerService cleanerService;

    @BeforeEach
    public void setUp() {
        cleanerService = new Jdk9ByteBufferCleanerService();
    }

    @Test
    public void cleanInvalidByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        assertThrows(Exception.class, () -> cleanerService.clean(buffer));
    }

    @Test
    public void impactShouldBeNoImpact() {
        assertEquals(Impact.NO_IMPACT, cleanerService.impact());
    }
}
