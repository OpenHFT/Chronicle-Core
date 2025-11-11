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
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CleanerServicePriorityTest {

    private static void resetLocator() throws Exception {
        java.lang.reflect.Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        java.lang.reflect.Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    @AfterEach
    void tearDown() throws Exception { resetLocator(); }

    @Test
    void lowestImpactChosenRegardlessOfDiscoveryOrder() throws Exception {
        resetLocator();
        // Build a temp services file with reversed order
        File root = new File("target/tmp-services-priority");
        File svc = new File(root, "META-INF/services/" + ByteBufferCleanerService.class.getName());
        File parent = svc.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IllegalStateException("Cannot create temp services dir");
        try (FileOutputStream fos = new FileOutputStream(svc)) {
            String content = "net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner\n" +
                    "net.openhft.chronicle.core.cleaner.testimpl.SomeImpactCleaner\n";
            fos.write(content.getBytes(StandardCharsets.ISO_8859_1));
        }
        URLClassLoader cl = new URLClassLoader(new URL[]{root.toURI().toURL()}, CleanerServiceLocator.class.getClassLoader());
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(cl);
            ByteBufferCleanerService svcChosen = CleanerServiceLocator.cleanerService();
            assertEquals("net.openhft.chronicle.core.cleaner.testimpl.AllowedCleaner", svcChosen.getClass().getName());
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
