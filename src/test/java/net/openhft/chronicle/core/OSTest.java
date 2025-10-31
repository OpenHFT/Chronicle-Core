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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static org.junit.Assert.*;

public class OSTest {

    @Test(expected = IllegalArgumentException.class)
    public void mapAlignRejectsNegativeOffsets() {
        OS.mapAlign(-1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapAlignRejectsNonPositiveAlignment() {
        OS.mapAlign(64L, 0);
    }

    @Test
    public void mapAlignRoundsUpToAlignment() {
        long alignment = OS.defaultOsPageSize();
        long offset = alignment / 2;
        long aligned = OS.mapAlign(offset, (int) alignment);
        assertEquals(alignment, aligned);
    }

    @Test
    public void memoryMapAndUnmapRoundTrip() throws IOException {
        File temp = File.createTempFile("chronicle-os-map", ".bin");
        temp.deleteOnExit();
        long size = OS.pageAlign(8192L);
        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(size);

            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, 0L, size);
            assertTrue("Expected non-zero mapping address", address != 0L);

            OS.unmap(address, size);
        }
    }

    @Test
    public void mapAlignHandlesNonZeroStartOffsets() throws IOException {
        File temp = File.createTempFile("chronicle-os-map-offset", ".bin");
        temp.deleteOnExit();
        long pageSize = OS.pageSize();
        long start = pageSize / 2; // intentionally unaligned
        long size = OS.pageAlign(4096L);

        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(start + size);

            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, start, size);
            assertTrue(address != 0L);
            OS.unmap(address, size);
        }
    }
}
