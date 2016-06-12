/*
 * Copyright 2016 higherfrequencytrading.com
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

import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class OSTest {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }
    @Test
    public void testIs64Bit() {
        System.out.println("is64 = " + OS.is64Bit());
    }

    @Test
    public void testGetProcessId() {
        System.out.println("pid = " + OS.getProcessId());
    }

    @Test
    @Ignore("Failing on TC (linux agent) for unknown reason, anyway the goal of this test is to " +
            "test mapping granularity on windows")
    public void testMapGranularity() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // tests that windows supports page mapping granularity
        long length = OS.pageSize();
        String name = Paths.get(OS.TARGET, "deleteme" + UUID.randomUUID().toString()).toString();
        File file = new File(name);
        file.deleteOnExit();
        FileChannel fc = new RandomAccessFile(name, "rw").getChannel();
        long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);
        OS.memory().writeLong(address, 0);
        OS.unmap(address, length);
        assertEquals(length, file.length());
    }

    @Test
    @Ignore("Should always pass, or crash the JVM based on length")
    public void testMap() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!OS.isWindows()) return;

        // crashes the JVM.
//        long length = (4L << 30L) + (64 << 10);
        // doesn't crash the JVM.
        long length = (4L << 30L);

        String name = OS.TARGET + "/deleteme";
        new File(name).deleteOnExit();
        FileChannel fc = new RandomAccessFile(name, "rw").getChannel();
        long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);
        for (long offset = 0; offset < length; offset += OS.pageSize())
            OS.memory().writeLong(address + offset, offset);
        OS.unmap(address, length);
    }
}
