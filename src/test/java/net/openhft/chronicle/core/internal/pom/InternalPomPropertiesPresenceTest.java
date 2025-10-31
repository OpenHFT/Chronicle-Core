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
