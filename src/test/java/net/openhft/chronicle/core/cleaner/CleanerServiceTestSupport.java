/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cleaner;

import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("removal")
final class CleanerServiceTestSupport {

    private CleanerServiceTestSupport() {
    }

    static void resetLocator() throws Exception {
        java.lang.reflect.Field init = CleanerServiceLocator.class.getDeclaredField("initialised");
        init.setAccessible(true);
        init.setBoolean(null, false);
        java.lang.reflect.Field inst = CleanerServiceLocator.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);
    }

    static ByteBufferCleanerService chooseService(String dirName, String content) throws Exception {
        resetLocator();
        File root = new File("target/" + dirName);
        File svc = new File(root, "META-INF/services/" + ByteBufferCleanerService.class.getName());
        File parent = svc.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create temp services dir");
        }
        try (FileOutputStream fos = new FileOutputStream(svc)) {
            fos.write(content.getBytes(UTF_8));
        }
        URL rootUrl = root.toURI().toURL();
        URLClassLoader cl = java.security.AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(new URL[]{rootUrl},
                        CleanerServiceLocator.class.getClassLoader()));
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(cl);
            return CleanerServiceLocator.cleanerService();
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
