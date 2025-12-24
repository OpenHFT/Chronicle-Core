/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleaningRandomAccessFileTest extends CoreTestCommon {

    @Test
    void testOpenAndClose() throws IOException {
        File tempFile = File.createTempFile("test", "raf");
        CleaningRandomAccessFile raf = new CleaningRandomAccessFile(tempFile, "rw");

        // Write and read to verify file is open
        raf.writeUTF("test");
        raf.seek(0);
        assertEquals("test", raf.readUTF(), "readUTF should return the written value");

        raf.close();

        assertThrows(IOException.class, () -> raf.writeUTF("should fail"),
                "writeUTF should fail after close");

        assertTrue(tempFile.delete(), "temp file should be deletable after closing");
    }

    @SuppressWarnings("removal")
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

    @Test
    void resourceLeak() throws IOException {
        CleaningRandomAccessFileTestSupport.assertNoResourceLeak();
    }
}
