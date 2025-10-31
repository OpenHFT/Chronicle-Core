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

import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService.Impact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionBasedByteBufferCleanerServiceTest {

    private final ReflectionBasedByteBufferCleanerService cleanerService = new ReflectionBasedByteBufferCleanerService();

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = "1\\.8.*")
    public void cleanShouldWorkOnJava8() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw an exception on Java 8");
    }

    @Test
    @EnabledIfSystemProperty(named = "java.version", matches = "9|1[0-9].*")
    public void cleanShouldWorkOnJava9Plus() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        assertDoesNotThrow(() -> cleanerService.clean(buffer), "Cleaning a direct buffer should not throw an exception on Java 9+");
    }

    @Test
    public void impactShouldReturnValidImpact() {
        Impact impact = cleanerService.impact();
        assertTrue(impact == Impact.SOME_IMPACT || impact == Impact.UNAVAILABLE, "Impact should be either SOME_IMPACT or UNAVAILABLE");
    }
}
