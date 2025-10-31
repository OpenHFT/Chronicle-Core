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
        tempDir.mkdir();
        for (int j = 0; j < 100; j++) {
            int files = new File("/proc/self/fd").list().length;
            System.out.println("File descriptors " + files);
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 100; i++) {
//                RandomAccessFile file = new CleaningRandomAccessFile(tempDir + "/file" + i, "rw");
                RandomAccessFile file = new RandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                file.getChannel().write(bb);
            }
            System.gc();
        }
    }
}
