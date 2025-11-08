/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IOToolsCreateDirectoriesTest {

    @Test
    public void createDirectoriesBuildsNestedStructure() throws IOException {
        Path base = Files.createTempDirectory(Paths.get(OS.getTarget()), "iotools-dir-test");
        Path nested = base.resolve("a/b/c");
        try {
            IOTools.createDirectories(nested);
            assertTrue(Files.isDirectory(nested));
        } finally {
            delete(base.toFile());
        }
    }

    @Test
    public void createDirectoriesFailsWhenFileWithSameNameExists() throws IOException {
        Path base = Files.createTempDirectory(Paths.get(OS.getTarget()), "iotools-file-test");
        Path file = base.resolve("exists");
        Files.write(file, new byte[]{1, 2, 3});
        try {
            IOTools.createDirectories(file);
            fail("expected IOException");
        } catch (IOException expected) {
            // expected
        } finally {
            delete(base.toFile());
        }
    }

    private static void delete(File file) {
        if (!file.exists())
            return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children)
                delete(child);
        }
        file.delete();
    }
}
