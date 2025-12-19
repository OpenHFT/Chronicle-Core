/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the ServiceConfigurationError path by providing a bogus provider entry.
 */
class CleanerServiceLoaderErrorTest {

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
    void serviceConfigurationErrorFallsBackToReflection() throws Exception {
        resetLocator();
        File root = new File("target/tmp-services-error");
        File svc = new File(root, "META-INF/services/" + ByteBufferCleanerService.class.getName());
        File parent = svc.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IllegalStateException("Cannot create temp services dir");
        try (FileOutputStream fos = new FileOutputStream(svc)) {
            // bogus provider triggers ServiceConfigurationError when iterating
            fos.write("does.not.ExistProvider\n".getBytes(UTF_8));
        }
        // Child-first isolation: no parent to avoid inheriting other service resources from the test classpath
        URLClassLoader cl = new URLClassLoader(new URL[]{root.toURI().toURL()}, null);
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(cl);
            ByteBufferCleanerService selected = CleanerServiceLocator.cleanerService();
            assertNotNull(selected, "service implementation should be found");
            assertTrue(selected.getClass().getName().contains("internal.cleaner"), "service should fall back to reflection-based internal cleaner when ServiceConfigurationError occurs");
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
