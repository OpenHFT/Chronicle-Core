/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class CleaningRandomAccessFileTest {

    static int getFDs() {
        if (!OS.isLinux())
            return -1;
        return new File("/proc/self/fd").list().length;
    }

    @Before
    public void cleanUp() {
        System.gc();
        Jvm.pause(100);
    }

    @Test
    public void resourceLeak() throws IOException {
        File tempDir = IOTools.createTempFile("resourceLeak");
        tempDir.mkdir();
        for (int j = 0; j < 50; j++) {
            int files = getFDs();
            if (files > 0) {
//                System.out.println("File descriptors " + files);
                assertEquals(200, files, 200);
            }
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 200; i++) {
                RandomAccessFile file = new CleaningRandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                file.getChannel().write(bb);
            }
            System.gc();
            Jvm.pause(10);
        }
    }
}