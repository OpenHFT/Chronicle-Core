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

        URLClassLoader cl = new URLClassLoader(new URL[]{root.toUri().toURL()}, CleanerServiceLocator.class.getClassLoader());
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        try {
            current.setContextClassLoader(cl);
            ByteBufferCleanerService service = CleanerServiceLocator.cleanerService();
            assertEquals(ReflectionBasedByteBufferCleanerService.class, service.getClass());
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
