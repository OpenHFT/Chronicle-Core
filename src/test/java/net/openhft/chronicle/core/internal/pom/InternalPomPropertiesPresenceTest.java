/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalPomPropertiesPresenceTest {

    @Test
    @DisplayName("Version loaded from resource and then cached")
    void versionLoadedFromResourceAndThenCached() {
        String v1 = InternalPomProperties.version("test.group", "test-artifact");
        assertEquals("1.2.3", v1, "first call should load version '1.2.3' from resource");
        // Now hide resources via an empty TCCL and read again; cache should serve same value
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(new URLClassLoader(new URL[0], null));
            String v2 = InternalPomProperties.version("test.group", "test-artifact");
            assertEquals("1.2.3", v2, "cached version should be returned even with empty classloader");
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
