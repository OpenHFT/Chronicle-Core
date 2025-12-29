/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
class IOToolsCreateDirectoriesTest {

    private static void delete(File file) throws IOException {
        if (!file.exists())
            return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children)
                delete(child);
        }
        if (!file.delete() && file.exists())
            throw new IOException("Failed to delete " + file);
    }

    @DisplayName("createDirectoriesBuildsNestedStructure behaviour under expected input and output conditions")
    @Test
    void createDirectoriesBuildsNestedStructure() throws IOException {
        Path base = Files.createTempDirectory(Paths.get(OS.getTarget()), "iotools-dir-test");
        Path nested = base.resolve("a/b/c");
        try {
            IOTools.createDirectories(nested);
            assertTrue(Files.isDirectory(nested), "nested directory path should be created successfully");
        } finally {
            delete(base.toFile());
        }
    }

    @DisplayName("createDirectoriesFailsWhenFileWithSameNameExists behaviour under expected input and output conditions")
    @Test
    void createDirectoriesFailsWhenFileWithSameNameExists() throws IOException {
        Path base = Files.createTempDirectory(Paths.get(OS.getTarget()), "iotools-file-test");
        Path file = base.resolve("exists");
        Files.write(file, new byte[]{1, 2, 3});
        try {
            assertThrows(IOException.class, () -> IOTools.createDirectories(file),
                    "createDirectories should throw when path already exists as a file");
        } finally {
            delete(base.toFile());
        }
    }
}
