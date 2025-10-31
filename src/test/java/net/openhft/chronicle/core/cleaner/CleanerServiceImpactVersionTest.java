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
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceImpactVersionTest {

    private static void resetLocator() throws Exception {
        java.lang.reflect.Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        java.lang.reflect.Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetLocator();
    }

    @Test
    void lowerImpactPreferredAndVersionGateApplied() throws Exception {
        resetLocator();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc);
        String name = svc.getClass().getName();
        // SomeImpactCleaner is available but NO_IMPACT (AllowedCleaner) must be chosen.
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", name);
        // And DisallowedCleaner must never be selected due to @TargetMajorVersion(99)
        assertNotEquals("net.openhft.chronicle.core.cleaner.testimpl.DisallowedCleaner", name);
    }
}

