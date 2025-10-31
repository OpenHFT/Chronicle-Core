/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.CleaningRandomAccessFile;
import net.openhft.chronicle.core.io.IOTools;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class CleaningRandomAccessFileTest extends CoreTestCommon {

    static int getFDs() {
        if (!OS.isLinux())
            return -1;
        return new File("/proc/self/fd").list().length;
    }

    @Test
    public void resourceLeak() throws IOException {
        File tempDir = IOTools.createTempFile("resourceLeak");
        tempDir.mkdir();
        int repeat = Jvm.isArm() ? 6 : 50;
        for (int j = 0; j < repeat; j++) {
            int files = getFDs();
            if (files > 0) {
//                System.out.println("File descriptors " + files);
                assertEquals("j: " + j, 200, files, 200);
            }
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 200; i++) {
                RandomAccessFile file = new CleaningRandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
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
}
