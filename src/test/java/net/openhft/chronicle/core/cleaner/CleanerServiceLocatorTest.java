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

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceLocatorTest {

    private static void resetLocator() throws Exception {
        Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetLocator();
    }

    @Test
    void picksAllowedServiceWithLowestImpact() throws Exception {
        resetLocator();
        ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
        assertNotNull(svc);
        // Should be our test implementation from META-INF/services
        assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", svc.getClass().getName());
        assertEquals(ByteBufferCleanerService.Impact.NO_IMPACT, svc.impact());
    }

    @Test
    void fallsBackToReflectionCleanerWhenNoProviders() throws Exception {
        resetLocator();
        final String serviceName = "META-INF/services/" + ByteBufferCleanerService.class.getName();
        ClassLoader parent = CleanerServiceLocator.class.getClassLoader();
        ClassLoader noServiceCL = new ClassLoader(parent) {
            @Override
            public java.net.URL getResource(String name) {
                if (serviceName.equals(name)) return null;
                return super.getResource(name);
            }

            @Override
            public java.util.Enumeration<java.net.URL> getResources(String name) {
                if (serviceName.equals(name)) return java.util.Collections.emptyEnumeration();
                try {
                    return super.getResources(name);
                } catch (java.io.IOException e) {
                    return java.util.Collections.emptyEnumeration();
                }
            }

            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                if (serviceName.equals(name)) return null;
                return super.getResourceAsStream(name);
            }
        };
        Thread current = Thread.currentThread();
        ClassLoader prev = current.getContextClassLoader();
        try {
            current.setContextClassLoader(noServiceCL);
            ByteBufferCleanerService svc = CleanerServiceLocator.cleanerService();
            assertNotNull(svc);
            assertTrue(svc.getClass().getName().contains("internal.cleaner"));
        } finally {
            current.setContextClassLoader(prev);
        }
    }
}
