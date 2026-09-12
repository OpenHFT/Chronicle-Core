/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IOToolsDeletionTest {
    @TempDir
    Path temporary;

    @ParameterizedTest
    @CsvSource({"0, false", "0, true", "1, false", "1, true", "2, false", "2, true"})
    void rejectsDirectoriesImmediatelyBeyondLimit(int maxDepth, boolean populated) throws IOException {
        Path root = Files.createDirectory(temporary.resolve("root"));
        Path boundary = root;
        for (int depth = 0; depth <= maxDepth; depth++)
            boundary = Files.createDirectory(boundary.resolve("child"));
        Path file = boundary.resolve("retained.txt");
        if (populated)
            Files.write(file, new byte[]{42});

        assertThrows(AssertionError.class, () -> IOTools.deleteDirWithFiles(root.toFile(), maxDepth));

        assertTrue(Files.isDirectory(boundary), "the over-depth directory must remain");
        if (populated)
            assertArrayEquals(new byte[]{42}, Files.readAllBytes(file));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, Integer.MAX_VALUE})
    void deletesFilesAndDirectoriesWithinLimit(int maxDepth) throws IOException {
        Path root = Files.createDirectory(temporary.resolve("root"));
        Path directory = root;
        for (int depth = 0; depth < Math.min(maxDepth, 2); depth++)
            directory = Files.createDirectory(directory.resolve("child"));
        Files.write(directory.resolve("data.txt"), new byte[]{42});

        assertTrue(IOTools.deleteDirWithFiles(root.toFile(), maxDepth));
        assertFalse(Files.exists(root, LinkOption.NOFOLLOW_LINKS));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void removesChildLinkWithoutTouchingForeignTree(int maxDepth) throws IOException {
        Path root = Files.createDirectory(temporary.resolve("root"));
        Path foreign = Files.createDirectory(temporary.resolve("foreign"));
        Path file = Files.write(foreign.resolve("retained.txt"), new byte[]{42});
        createLink(root.resolve("link"), foreign);

        assertTrue(IOTools.deleteDirWithFiles(root.toFile(), maxDepth));
        assertFalse(Files.exists(root, LinkOption.NOFOLLOW_LINKS));
        assertArrayEquals(new byte[]{42}, Files.readAllBytes(file));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void removesRootLinkWithoutTouchingTarget(boolean directory) throws IOException {
        Path target = temporary.resolve("target");
        Path file = directory ? Files.createDirectory(target).resolve("retained.txt") : target;
        Files.write(file, new byte[]{42});
        Path link = temporary.resolve("root-link");
        createLink(link, target);

        assertTrue(IOTools.deleteDirWithFiles(link.toFile(), 0));
        assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
        assertArrayEquals(new byte[]{42}, Files.readAllBytes(file));
    }

    @Test
    void removesDanglingRootLink() throws IOException {
        Path link = temporary.resolve("root-link");
        createLink(link, temporary.resolve("missing"));
        assertTrue(IOTools.deleteDirWithFiles(link.toFile(), 0));
        assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    void removesDanglingAndCyclicChildLinks() throws IOException {
        Path root = Files.createDirectory(temporary.resolve("root"));
        createLink(root.resolve("missing"), temporary.resolve("missing"));
        createLink(root.resolve("cycle"), root);
        assertTrue(IOTools.deleteDirWithFiles(root.toFile(), 0));
        assertFalse(Files.exists(root, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    void preservesPlainFileAndReportsMissingRoot() throws IOException {
        Path file = Files.write(temporary.resolve("plain.txt"), new byte[]{42});
        assertFalse(IOTools.deleteDirWithFiles(file.toFile(), 0));
        assertArrayEquals(new byte[]{42}, Files.readAllBytes(file));
        assertFalse(IOTools.deleteDirWithFiles(temporary.resolve("missing").toFile(), 0));
    }

    private static void createLink(Path link, Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException e) {
            assumeTrue(false, "symbolic links are unsupported: " + e);
        } catch (FileSystemException e) {
            if (OS.isWindows())
                assumeTrue(false, "Windows symbolic-link creation is unavailable: " + e);
            throw e;
        }
    }
}
