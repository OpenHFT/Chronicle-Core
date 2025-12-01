/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
