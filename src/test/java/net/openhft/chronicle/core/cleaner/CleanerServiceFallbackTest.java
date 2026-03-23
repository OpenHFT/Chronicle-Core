/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServiceFallbackTest {

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
    void fallsBackWhenServiceLoadingFails() throws Exception {
        resetLocator();
        Path root = Paths.get("target", "tmp-services-fallback");
        Path serviceFile = root.resolve("META-INF/services/" + ByteBufferCleanerService.class.getName());
        prepareBrokenServiceDescriptor(serviceFile);

        final String serviceName = "META-INF/services/" + ByteBufferCleanerService.class.getName();
        final URL brokenUrl = serviceFile.toUri().toURL();
        ClassLoader parent = CleanerServiceLocator.class.getClassLoader();
        ClassLoader cl = new ClassLoader(parent) {
            @Override
            public URL getResource(String name) {
                if (serviceName.equals(name)) return brokenUrl;
                return super.getResource(name);
            }

            @Override
            public java.util.Enumeration<URL> getResources(String name) throws java.io.IOException {
                if (serviceName.equals(name)) return java.util.Collections.enumeration(java.util.Collections.singleton(brokenUrl));
                return super.getResources(name);
            }

            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                if (serviceName.equals(name)) try {
                    return brokenUrl.openStream();
                } catch (java.io.IOException e) {
                    return null;
                }
                return super.getResourceAsStream(name);
            }
        };
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        try {
            current.setContextClassLoader(cl);
            ByteBufferCleanerService service = CleanerServiceLocator.cleanerService();
            assertSame(ReflectionBasedByteBufferCleanerService.class, service.getClass());
            assertSame(service, CleanerServiceLocator.cleanerService(), "Locator should cache the fallback instance");
        } finally {
            current.setContextClassLoader(previous);
        }
    }

    private static void prepareBrokenServiceDescriptor(Path serviceFile) throws IOException {
        Files.createDirectories(serviceFile.getParent());
        // Reference a class that does not exist so ServiceLoader triggers ServiceConfigurationError
        Files.write(serviceFile, "non.existent.Cleaner\n".getBytes(StandardCharsets.ISO_8859_1));
    }
}
