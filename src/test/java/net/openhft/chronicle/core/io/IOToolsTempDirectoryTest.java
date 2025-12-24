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

class IOToolsTempDirectoryTest {

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
    void createTempDirectoryCreatesUniqueFolders() throws IOException {
        Path dir1 = IOTools.createTempDirectory("temp-test");
        Path dir2 = IOTools.createTempDirectory("temp-test");
        Path base = Paths.get(OS.getTarget()).toAbsolutePath().normalize();
        try {
            assertNotEquals(dir1, dir2, "Each invocation should return a new directory");
            assertTrue(Files.isDirectory(dir1), "first temp directory should exist as a directory");
            assertTrue(Files.isDirectory(dir2), "second temp directory should exist as a directory");
            String dir1Path = dir1.toString();
            String dir2Path = dir2.toString();
            assertTrue(dir1Path.contains("temp-test"), "dir1 path " + dir1Path + " should contain temp-test");
            assertTrue(dir2Path.contains("temp-test"), "dir2 path " + dir2Path + " should contain temp-test");
            Path dir1Base = dir1.toAbsolutePath().normalize();
            Path dir2Base = dir2.toAbsolutePath().normalize();
            assertTrue(dir1Base.startsWith(base), "dir1 path " + dir1Base + " should start with base " + base);
            assertTrue(dir2Base.startsWith(base), "dir2 path " + dir2Base + " should start with base " + base);
        } finally {
            deleteRecursively(dir1);
            deleteRecursively(dir2);
        }
    }

    @Test
    void createTempFileUsesTempDirectory() throws IOException {
        File file = IOTools.createTempFile("temp-file");
        Path base = Paths.get(OS.getTarget()).toAbsolutePath().normalize();
        try {
            assertFalse(file.exists(), "Temp file paths are not materialised until needed");
            String filePath = file.getAbsolutePath();
            assertTrue(filePath.contains("temp-file"), "temp file path " + filePath + " should contain temp-file");
            Path fileBase = file.toPath().toAbsolutePath().normalize();
            assertTrue(fileBase.startsWith(base), "temp file path " + fileBase + " should start with base " + base);
            // When callers need a real file they can create it themselves.
            Files.createFile(file.toPath());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }
}
