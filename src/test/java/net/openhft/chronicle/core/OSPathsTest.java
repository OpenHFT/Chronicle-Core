/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Extra coverage for path utilities, alignment caching, and target directory discovery.
 */
@SuppressWarnings("deprecation")
class OSPathsTest extends CoreTestCommon {

    @Test
    void asRelativePathTrimsUserDirPrefix() {
        String userDirAbs = new File(OS.userDir()).getAbsolutePath();
        String childPath = userDirAbs + File.separator + "child";
        assertEquals("child", OS.testAsRelativePath(childPath),
                "relative path should drop user dir prefix");
        assertEquals(".", OS.testAsRelativePath(userDirAbs.endsWith(File.separator)
                ? userDirAbs
                : userDirAbs + File.separator),
                "user dir should map to dot");

        String other = "/tmp/elsewhere";
        assertEquals(other, OS.testAsRelativePath(other), "unrelated paths stay absolute");
    }

    @Test
    void mapAlignmentDefaultsToPageSizeWhenUnset() {
        OS.testResetMapAlignment();
        long alignment = OS.mapAlignment();
        assertEquals(OS.defaultOsPageSize(), alignment, "default map alignment should match OS page size");
    }

    @Test
    void findTarget0PrefersProjectBuildDirectory() throws Exception {
        String original = System.getProperty("project.build.directory");
        Path tmpBuild = Files.createTempDirectory("cc-target-pref");
        try {
            System.setProperty("project.build.directory", tmpBuild.toFile().getAbsolutePath());
            String path = OS.testFindTarget0();
            assertEquals(tmpBuild.toFile().getAbsolutePath(), path,
                    "findTarget0 should prefer project.build.directory");
        } finally {
            if (original == null) {
                System.clearProperty("project.build.directory");
            } else {
                System.setProperty("project.build.directory", original);
            }
            Files.deleteIfExists(tmpBuild);
        }
    }

    @Test
    void findTarget0FallsBackToTmpWhenNoBuildDirs() throws Exception {
        String originalUserDir = System.getProperty("user.dir");
        String originalProject = System.getProperty("project.build.directory");

        Path isolated = Files.createTempDirectory("cc-no-build-" + System.nanoTime());
        try {
            System.clearProperty("project.build.directory");
            System.setProperty("user.dir", isolated.toFile().getAbsolutePath());
            String path = OS.testFindTarget0();
            // expect a target directory under java.io.tmpdir
            assertEquals(new File(System.getProperty("java.io.tmpdir"), "target").getPath(), path,
                    "findTarget0 should fall back to tmp target directory");
        } finally {
            if (originalUserDir == null) {
                System.clearProperty("user.dir");
            } else {
                System.setProperty("user.dir", originalUserDir);
            }
            if (originalProject == null) {
                System.clearProperty("project.build.directory");
            } else {
                System.setProperty("project.build.directory", originalProject);
            }
            try {
                Files.deleteIfExists(isolated);
            } catch (IOException ignored) {
                // best-effort cleanup
            }
        }
    }
}
