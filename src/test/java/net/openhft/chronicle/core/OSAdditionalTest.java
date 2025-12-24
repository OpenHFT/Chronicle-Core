/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra coverage for OS edge cases that are environment-sensitive.
 */
@SuppressWarnings("deprecation")
class OSAdditionalTest extends CoreTestCommon {

    @Test
    void findTmpUsesProjectBuildDirectory() throws IOException {
        final String originalProjectBuildDir = System.getProperty("project.build.directory");
        Path buildDir = Files.createTempDirectory("cc-target");
        try {
            System.setProperty("project.build.directory", buildDir.toString());

            String tmpPath = OS.findTmp();

            assertTrue(tmpPath.endsWith(File.separator + "tmp") || tmpPath.endsWith("/tmp"),
                    "tmp path should be created under project.build.directory");
            assertTrue(new File(buildDir.toFile(), "tmp").exists(), "tmp directory should exist");
        } finally {
            if (originalProjectBuildDir == null) {
                System.clearProperty("project.build.directory");
            } else {
                System.setProperty("project.build.directory", originalProjectBuildDir);
            }
            Files.walk(buildDir)
                    .map(Path::toFile)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(File::delete);
        }
    }

    @Test
    void findTmpFallsBackWhenTmpDirMissing() {
        final String originalTmp = System.getProperty("java.io.tmpdir");
        try {
            System.setProperty("java.io.tmpdir", "nonexistent-tmp-" + System.nanoTime());
            System.clearProperty("project.build.directory");

            assertEquals("tmp", OS.findTmp(), "fallback tmp directory should be named tmp");
        } finally {
            if (originalTmp == null) {
                System.clearProperty("java.io.tmpdir");
            } else {
                System.setProperty("java.io.tmpdir", originalTmp);
            }
        }
    }

    @Test
    void findDirThrowsWhenSuffixNotPresent() {
        assertThrows(FileNotFoundException.class,
                () -> OS.findDir("definitely-not-on-classpath-" + System.nanoTime()),
                "findDir should throw when suffix is not on the classpath");
    }

    @Test
    void spaceUsedOnExistingFileReturnsNonNegative() throws IOException {
        File temp = File.createTempFile("os-space-used", ".txt");
        temp.deleteOnExit();
        long spaceUsed = OS.spaceUsed(temp.getPath());
        assertTrue(spaceUsed >= 0L, "spaceUsed should be non-negative for existing file, was " + spaceUsed);
    }

    @Test
    void memoryMappedCounterTracksMapAndUnmap() throws IOException {
        File temp = File.createTempFile("os-map-counter", ".bin");
        temp.deleteOnExit();
        long pageAligned = OS.pageAlign(4096);
        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(pageAligned);

            long before = OS.memoryMapped();
            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, 0L, pageAligned);
            long afterMap = OS.memoryMapped();
            assertTrue(afterMap >= before + pageAligned,
                    "memoryMapped should increase after map: before=" + before + ", after=" + afterMap);

            OS.unmap(address, pageAligned);
            long afterUnmap = OS.memoryMapped();
            assertEquals(before, afterUnmap, "memoryMapped should return to previous value after unmap");
        }
    }
}
