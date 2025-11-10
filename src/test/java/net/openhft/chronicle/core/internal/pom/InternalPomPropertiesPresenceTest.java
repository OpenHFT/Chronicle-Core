//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.pom;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.*;

class InternalPomPropertiesPresenceTest {

    @Test
    void versionLoadedFromResourceAndThenCached() throws Exception {
        String v1 = InternalPomProperties.version("test.group", "test-artifact");
        assertEquals("1.2.3", v1);
        // Now hide resources via an empty TCCL and read again; cache should serve same value
        Thread t = Thread.currentThread();
        ClassLoader prev = t.getContextClassLoader();
        try {
            t.setContextClassLoader(new URLClassLoader(new URL[0], null));
            String v2 = InternalPomProperties.version("test.group", "test-artifact");
            assertEquals("1.2.3", v2);
        } finally {
            t.setContextClassLoader(prev);
        }
    }
}
