/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Extra coverage for OS edge cases that are environment-sensitive.
 */
public class OSAdditionalTest extends CoreTestCommon {

    @Test
    public void findTmpUsesProjectBuildDirectory() throws IOException {
        final String originalProjectBuildDir = System.getProperty("project.build.directory");
        Path buildDir = Files.createTempDirectory("cc-target");
        try {
            System.setProperty("project.build.directory", buildDir.toString());

            String tmpPath = OS.findTmp();

            assertTrue("tmp path should be created under project.build.directory",
                    tmpPath.endsWith(File.separator + "tmp") || tmpPath.endsWith("/tmp"));
            assertTrue("tmp directory should exist", new File(buildDir.toFile(), "tmp").exists());
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
    public void findTmpFallsBackWhenTmpDirMissing() {
        final String originalTmp = System.getProperty("java.io.tmpdir");
        try {
            System.setProperty("java.io.tmpdir", "nonexistent-tmp-" + System.nanoTime());
            System.clearProperty("project.build.directory");

            assertEquals("tmp", OS.findTmp());
        } finally {
            if (originalTmp == null) {
                System.clearProperty("java.io.tmpdir");
            } else {
                System.setProperty("java.io.tmpdir", originalTmp);
            }
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void findDirThrowsWhenSuffixNotPresent() throws FileNotFoundException {
        OS.findDir("definitely-not-on-classpath-" + System.nanoTime());
    }

    @Test
    public void spaceUsedOnExistingFileReturnsNonNegative() throws IOException {
        File temp = File.createTempFile("os-space-used", ".txt");
        temp.deleteOnExit();
        assertTrue(OS.spaceUsed(temp.getPath()) >= 0L);
    }

    @Test
    public void memoryMappedCounterTracksMapAndUnmap() throws IOException {
        File temp = File.createTempFile("os-map-counter", ".bin");
        temp.deleteOnExit();
        long pageAligned = OS.pageAlign(4096);
        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(pageAligned);

            long before = OS.memoryMapped();
            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, 0L, pageAligned);
            long afterMap = OS.memoryMapped();
            assertTrue("memoryMapped should increase after map", afterMap >= before + pageAligned);

            OS.unmap(address, pageAligned);
            long afterUnmap = OS.memoryMapped();
            assertEquals("memoryMapped should return to previous value after unmap", before, afterUnmap);
        }
    }
}
