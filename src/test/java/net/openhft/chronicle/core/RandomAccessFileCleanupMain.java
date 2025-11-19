/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/*
NOTE: RandomAccessFile doesn't clean up it's resources when GC'ed
 */
public class RandomAccessFileCleanupMain {
    public static void main(String[] args) throws IOException {
        File tempDir = IOTools.createTempFile("RandomAccessFileCleanupMain");
        if (!tempDir.mkdir() && !tempDir.isDirectory()) {
            throw new IOException("Unable to create temp directory " + tempDir);
        }
        for (int j = 0; j < 100; j++) {
            int files = new File("/proc/self/fd").list().length;
            System.out.println("File descriptors " + files);
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 100; i++) {
                RandomAccessFile file = new RandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                file.getChannel().write(bb);
            }
            System.gc();
        }
    }
}
