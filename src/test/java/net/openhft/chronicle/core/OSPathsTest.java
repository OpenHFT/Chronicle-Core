/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Extra coverage for path utilities and alignment caching.
 */
public class OSPathsTest extends CoreTestCommon {

    @Test
    public void asRelativePathTrimsUserDirPrefix() {
        String userDirAbs = new File(OS.userDir()).getAbsolutePath();
        String childPath = userDirAbs + File.separator + "child";
        assertEquals("child", OS.testAsRelativePath(childPath));
        assertEquals(".", OS.testAsRelativePath(userDirAbs.endsWith(File.separator)
                ? userDirAbs
                : userDirAbs + File.separator));

        String other = "/tmp/elsewhere";
        assertEquals("unrelated paths stay absolute", other, OS.testAsRelativePath(other));
    }

    @Test
    public void mapAlignmentDefaultsToPageSizeWhenUnset() {
        OS.testResetMapAlignment();
        long alignment = OS.mapAlignment();
        assertEquals(OS.defaultOsPageSize(), alignment);
    }

    @Test
    public void findTarget0PrefersProjectBuildDirectory() throws Exception {
        String original = System.getProperty("project.build.directory");
        Path tmpBuild = Files.createTempDirectory("cc-target-pref");
        try {
            System.setProperty("project.build.directory", tmpBuild.toFile().getAbsolutePath());
            String path = OS.testFindTarget0();
            assertEquals(tmpBuild.toFile().getAbsolutePath(), path);
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
    public void findTarget0FallsBackToTmpWhenNoBuildDirs() throws Exception {
        String originalUserDir = System.getProperty("user.dir");
        String originalProject = System.getProperty("project.build.directory");

        Path isolated = Files.createTempDirectory("cc-no-build-" + System.nanoTime());
        try {
            System.clearProperty("project.build.directory");
            System.setProperty("user.dir", isolated.toFile().getAbsolutePath());
            String path = OS.testFindTarget0();
            // expect a target directory under java.io.tmpdir
            assertEquals(new File(System.getProperty("java.io.tmpdir"), "target").getPath(), path);
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
