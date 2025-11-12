/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.Assert.*;

public class IOToolsTempDirectoryTest {

    @Test
    public void createTempDirectoryCreatesUniqueFolders() throws IOException {
        Path dir1 = IOTools.createTempDirectory("temp-test");
        Path dir2 = IOTools.createTempDirectory("temp-test");
        Path base = Paths.get(OS.getTarget()).toAbsolutePath().normalize();
        try {
            assertNotEquals("Each invocation should return a new directory", dir1, dir2);
            assertTrue(Files.isDirectory(dir1));
            assertTrue(Files.isDirectory(dir2));
            assertTrue(dir1.toString().contains("temp-test"));
            assertTrue(dir2.toString().contains("temp-test"));
            assertTrue(dir1.toAbsolutePath().normalize().startsWith(base));
            assertTrue(dir2.toAbsolutePath().normalize().startsWith(base));
        } finally {
            deleteRecursively(dir1);
            deleteRecursively(dir2);
        }
    }

    @Test
    public void createTempFileUsesTempDirectory() throws IOException {
        File file = IOTools.createTempFile("temp-file");
        Path base = Paths.get(OS.getTarget()).toAbsolutePath().normalize();
        try {
            assertFalse("Temp file paths are not materialised until needed", file.exists());
            assertTrue(file.getAbsolutePath().contains("temp-file"));
            assertTrue(file.toPath().toAbsolutePath().normalize().startsWith(base));
            // When callers need a real file they can create it themselves.
            Files.createFile(file.toPath());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path))
            return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }
}
