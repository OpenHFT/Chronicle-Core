/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IOToolsFileUrlTest {
    private static final ClassLoader NO_RESOURCES = new ClassLoader(null) {
        @Override
        public URL getResource(String name) {
            return null;
        }
    };

    @TempDir
    Path temporary;

    @ParameterizedTest
    @ValueSource(strings = {"a#b.txt", "a b.txt", "a%b.txt", "a+b.txt", "plain.txt"})
    void opensTheExactFilename(String name) throws IOException {
        byte[] content = {1, 2, 3, 42};
        Path file = Files.write(temporary.resolve(name), content);

        URL url = IOTools.urlFor(NO_RESOURCES, file.toString());

        assertEquals("file", url.getProtocol());
        assertNull(url.getRef(), "a filename must not create a URL fragment");
        assertNull(url.getQuery(), "a filename must not create a URL query");
        try (InputStream input = url.openStream()) {
            assertArrayEquals(content, IOTools.readAsBytes(input));
        }
    }

    @Test
    void preservesSymlinkPathRatherThanCanonicalisingIt() throws Exception {
        Path target = Files.write(temporary.resolve("target.txt"), new byte[]{42});
        Path link = temporary.resolve("link#name.txt");
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException e) {
            assumeTrue(false, "symbolic links are unsupported: " + e);
        } catch (FileSystemException e) {
            if (OS.isWindows())
                assumeTrue(false, "Windows symbolic-link creation is unavailable: " + e);
            throw e;
        }

        URL url = IOTools.urlFor(NO_RESOURCES, link.toString());

        assertEquals(link.toAbsolutePath().toUri(), url.toURI());
        assertNotEquals(target.toAbsolutePath().toUri(), url.toURI());
        try (InputStream input = url.openStream()) {
            assertArrayEquals(new byte[]{42}, IOTools.readAsBytes(input));
        }
    }

    @Test
    void preservesResourceLookupAndCompressedFallback() throws Exception {
        URL resource = temporary.resolve("resource.txt").toUri().toURL();
        ClassLoader loader = new ClassLoader(null) {
            @Override
            public URL getResource(String name) {
                return "direct".equals(name) || "compressed.gz".equals(name) ? resource : null;
            }
        };
        assertSame(resource, IOTools.urlFor(loader, "direct"));
        assertSame(resource, IOTools.urlFor(loader, "/direct"));
        assertSame(resource, IOTools.urlFor(loader, "compressed"));
    }

    @Test
    void stillReportsMissingFiles() {
        assertThrows(FileNotFoundException.class,
                () -> IOTools.urlFor(NO_RESOURCES, temporary.resolve("missing#file.txt").toString()));
    }
}
