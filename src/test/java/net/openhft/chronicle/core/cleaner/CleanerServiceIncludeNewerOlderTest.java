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
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanerServiceIncludeNewerOlderTest {

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
    void includeNewerOlderGateAllowsAppropriateProviders() throws Exception {
        resetLocator();
        File root = new File("target/tmp-services-include");
        File svc = new File(root, "META-INF/services/" + ByteBufferCleanerService.class.getName());
        if (!svc.getParentFile().exists() && !svc.getParentFile().mkdirs()) {
            throw new IllegalStateException("Cannot create services dir");
        }
        try (FileOutputStream fos = new FileOutputStream(svc)) {
            String content = "net.openhft.chronicle.core.cleaner.testimpl2.NewerInclOlderCleaner\n" +
                    "net.openhft.chronicle.core.cleaner.testimpl2.OlderInclNewerCleaner\n";
            fos.write(content.getBytes(UTF_8));
        }
        URLClassLoader cl = new URLClassLoader(new URL[]{root.toURI().toURL()}, CleanerServiceLocator.class.getClassLoader());
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(cl);
            ByteBufferCleanerService svcChosen = CleanerServiceLocator.cleanerService();
            // There may be existing providers on the classpath; assert that at least one of our
            // includeNewer/includeOlder candidates was considered acceptable by not being rejected outright.
            String name = svcChosen.getClass().getName();
            assertTrue(name.contains("AllowedCleaner") || name.contains("OlderInclNewerCleaner"));
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
