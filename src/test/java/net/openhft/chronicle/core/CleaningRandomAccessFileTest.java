/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CleaningRandomAccessFileTest {

    static int getFDs() {
        if (!OS.isLinux())
            return -1;
        return new File("/proc/self/fd").list().length;
    }

    @Test
    public void resourceLeak() throws IOException {
        File tempDir = new File(OS.TMP, "raf-" + System.nanoTime());
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
//                RandomAccessFile file = new RandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                file.getChannel().write(bb);
            }
            System.gc();
            Jvm.pause(10);
        }
    }

    /*
     * This example only leaks resources on a GC.
     */
    final Map<String, WeakReference<RandomAccessFile>> fileCache = new HashMap<>();

    public RandomAccessFile fileFor(String name) throws FileNotFoundException {
        WeakReference<RandomAccessFile> reference = fileCache.get(name);
        RandomAccessFile file = reference == null ? null : reference.get();
        if (file == null) {
            file = new RandomAccessFile(name, "rw");
            reference = new WeakReference<>(file);
            fileCache.put(name, reference);
        }
        return reference.get();
    }

}