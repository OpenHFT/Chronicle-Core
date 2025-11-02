/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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
            fos.write("does.not.ExistProvider\n".getBytes(StandardCharsets.ISO_8859_1));
        }
        // Child-first isolation: no parent to avoid inheriting other service resources from the test classpath
        URLClassLoader cl = new URLClassLoader(new URL[]{root.toURI().toURL()}, null);
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(cl);
            ByteBufferCleanerService selected = CleanerServiceLocator.cleanerService();
            assertNotNull(selected);
            assertTrue(selected.getClass().getName().contains("internal.cleaner"));
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
