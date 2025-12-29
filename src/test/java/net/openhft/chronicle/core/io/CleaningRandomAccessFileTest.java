/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleaningRandomAccessFileTest extends CoreTestCommon {

    private static int getFDs() {
        if (!OS.isLinux())
            return -1;
        //noinspection DataFlowIssue
        return new File("/proc/self/fd").list().length;
    }

    public static void assertNoResourceLeak() throws IOException {
        File tempDir = IOTools.createTempFile("resourceLeak");
        if (!tempDir.mkdir() && !tempDir.isDirectory()) {
            throw new IOException("Unable to create temp directory " + tempDir);
        }
        int repeat = Jvm.isArm() ? 6 : OS.isWindows() ? 25 : 50;
        for (int j = 0; j < repeat; j++) {
            int files = getFDs();
            if (files > 0) {
                assertEquals(200, files, 200, "file descriptor count should remain stable j=" + j);
            }
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 200; i++) {
                @SuppressWarnings("resource")
                RandomAccessFile file = new CleaningRandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                //noinspection ResultOfMethodCallIgnored
                file.getChannel().write(bb);
            }
            long start = System.currentTimeMillis();
            System.gc();
            for (int i = 0; i < 40; i++) {
                Jvm.pause(20);
                if (getFDs() < 200) {
                    double time = (System.currentTimeMillis() - start) / 1e3;
                    if (time > 0.1)
                        System.out.println("resourceLeak() - Took " + time + " seconds.");
                    break;
                }
            }
        }
        IOTools.deleteDirWithFiles(tempDir);
    }

    @DisplayName("testOpenAndClose behaviour under expected input and output conditions")
    @Test
    void testOpenAndClose() throws IOException {
        File tempFile = File.createTempFile("test", "raf");
        CleaningRandomAccessFile raf = new CleaningRandomAccessFile(tempFile, "rw");

        // Write and read to verify file is open
        raf.writeUTF("test");
        raf.seek(0);
        assertEquals("test", raf.readUTF(), "readUTF should read back 'test' string written to file");

        raf.close();

        assertThrows(IOException.class, () -> raf.writeUTF("should fail"),
                "writeUTF should throw after file is closed");

        assertTrue(tempFile.delete(), "temp file should be deletable after closing");
    }

    @SuppressWarnings("removal")
    @DisplayName("testFinalizeAndCleanup behaviour under expected input and output conditions")
    @Test
    void testFinalizeAndCleanup() throws IOException {
        File tempFile = File.createTempFile("test", "raf");

        //noinspection resource
        new CleaningRandomAccessFile(tempFile, "rw");

        System.gc();
        System.runFinalization();

        if (!tempFile.delete()) {
            Jvm.pause(100);
            Files.delete(tempFile.toPath());
        }
        assertTrue(true, "execution should reach this point without exception"); // If we reach here, the test passes
    }

    @DisplayName("resourceLeak behaviour under expected input and output conditions")
    @Test
    void resourceLeak() throws IOException {
        assertNoResourceLeak();
    }
}
