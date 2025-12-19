/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class IOToolsTempDirectoryTest {

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path))
            return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Unable to delete " + p, e);
                    }
                });
    }

    @Test
    public void createTempDirectoryCreatesUniqueFolders() throws IOException {
        Path dir1 = IOTools.createTempDirectory("temp-test");
        Path dir2 = IOTools.createTempDirectory("temp-test");
        Path base = Paths.get(OS.getTarget()).toAbsolutePath().normalize();
        try {
            assertNotEquals(dir1, dir2, "Each invocation should return a new directory");
            assertTrue(Files.isDirectory(dir1), "first temp directory should exist as a directory");
            assertTrue(Files.isDirectory(dir2), "second temp directory should exist as a directory");
            assertTrue(dir1.toString().contains("temp-test"), "first directory path should contain the specified prefix");
            assertTrue(dir2.toString().contains("temp-test"), "second directory path should contain the specified prefix");
            assertTrue(dir1.toAbsolutePath().normalize().startsWith(base), "first directory should be created under target base path");
            assertTrue(dir2.toAbsolutePath().normalize().startsWith(base), "second directory should be created under target base path");
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
            assertFalse(file.exists(), "Temp file paths are not materialised until needed");
            assertTrue(file.getAbsolutePath().contains("temp-file"), "temp file path should contain the specified prefix");
            assertTrue(file.toPath().toAbsolutePath().normalize().startsWith(base), "temp file should be created under target base path");
            // When callers need a real file they can create it themselves.
            Files.createFile(file.toPath());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }
}
