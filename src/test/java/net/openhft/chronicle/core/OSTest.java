/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OSTest {

    @Rule
    public final TestName testName = new TestName();
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

    /**
     * tests that windows supports page mapping granularity
     */
    @Test
    //@Ignore("Failing on TC (linux agent) for unknown reason, anyway the goal of this test is to " +
    //        "test mapping granularity on windows")
    public void testMapGranularity() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        File file = new File(OS.TARGET, getClass().getName() + "." + testName.getMethodName() + ".deleteme");
        file.deleteOnExit();

        try (RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            FileChannel fc = rw.getChannel();

            long length = OS.pageSize();
            MappedByteBuffer anchor = fc.map(MapMode.READ_WRITE, 0, length);
            anchor.order(ByteOrder.nativeOrder());

            long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);

            OS.memory().writeLong(address, 0);
            OS.unmap(address, length);

            assertEquals(length, file.length());
        }
    }

    @Test
    //@Ignore("Should always pass, or crash the JVM based on length")
    public void testMap() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        File file = new File(OS.TARGET, getClass().getName() + "." + testName.getMethodName() + ".deleteme");
        file.deleteOnExit();

        try (RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            FileChannel fc = rw.getChannel();

            // crashes the JVM.
            // long length = (4L << 30L) + (64 << 10);
            // doesn't crash the JVM, but takes 3s
            // long length = (4L << 30);
            // doesn't crash the JVM and runs fast
            long length = (4L << 25);

            long anchorSize = 0x4000_0000L;
            int anchorCount = (int) ((length + anchorSize - 1) / anchorSize);
            List<MappedByteBuffer> anchors = new ArrayList<>();
            long anchorTotalRemain = length;
            for (int i = 0; i < anchorCount; i++) {
                MappedByteBuffer anchor = fc.map(MapMode.READ_WRITE, i * anchorSize, Math.min(anchorTotalRemain, anchorSize));
                anchor.order(ByteOrder.nativeOrder());
                anchors.add(anchor);
                anchorTotalRemain -= anchorSize;
            }

            long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);
            for (long offset = 0; offset < length; offset += OS.pageSize()) {
                OS.memory().writeLong(address + offset, offset);
            }
            for (long offset = 0; offset < length; offset += OS.pageSize()) {
                assertEquals(offset, OS.memory().readLong(address + offset));
            }

            OS.unmap(address, length);
        }
    }

    @Test
    public void testMapFast() throws Exception {
        File file = new File(OS.TARGET, getClass().getName() + "." + testName.getMethodName() + ".deleteme");
        file.deleteOnExit();

        try (RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            FileChannel fc = rw.getChannel();

            long length = Long.BYTES;
            MappedByteBuffer anchor = fc.map(MapMode.READ_WRITE, 0, length);
            anchor.order(ByteOrder.nativeOrder());

            long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);

            long value = System.currentTimeMillis();
            value ^= (value << 32);

            OS.memory().writeLong(address, value);

            assertEquals(value, OS.memory().readLong(address));
            assertEquals(value, anchor.getLong(0));

            OS.unmap(address, length);
        }
    }

}
