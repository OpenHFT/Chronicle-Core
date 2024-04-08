/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OSTest extends CoreTestCommon {
    @Rule
    public final TestName testName = new TestName();
    private ThreadDump threadDump;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsSparseFileSupported() {
        // This test is environment-dependent and may need to be adjusted based on the target system
        boolean expected = System.getProperty("os.name").toLowerCase().contains("linux") && OS.is64Bit();
        assertEquals(expected, OS.isSparseFileSupported());
    }

    @Test
    public void testFindTmp() {
        String tmp = OS.findTmp();
        assertNotNull(tmp);
    }

    @Test
    public void testIPAddressHolder() {
        String ipAddress = OS.IPAddressHolder.IP_ADDRESS;
        assertNotNull(ipAddress);
    }

    @Test
    public void testHostnameHolder() {
        String hostname = OS.HostnameHolder.HOST_NAME;
        assertNotNull(hostname);
    }

    @Test
    public void testFindFile() {
        assertEquals(new File("./last").getAbsolutePath(), OS.findFile("first", "last").getAbsolutePath());
    }

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
        final boolean expected =
                Stream.of("com.ibm.vm.bitmode", "sun.arch.data.model")
                        .map(System::getProperty)
                        .filter(Objects::nonNull)
                        .anyMatch(p -> p.contains("64")) ||
                        Stream.of("java.vm.version")
                                .map(System::getProperty)
                                .filter(Objects::nonNull)
                                .anyMatch(p -> p.contains("_64"));

        assertEquals(expected, OS.is64Bit());
    }

    @Test
    public void testGetProcessId() {
        final int processId = OS.getProcessId();
        assertTrue(processId > 0);
    }

    /**
     * tests that Windows supports page mapping granularity
     */
    @Test
    //@Ignore("Failing on TC (linux agent) for unknown reason, anyway the goal of this test is to " +
    //        "test mapping granularity on windows")
    public void testMapGranularity() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        File file = IOTools.createTempFile(getClass().getName() + "." + testName.getMethodName());

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
        File file = IOTools.createTempFile(getClass().getName() + "." + testName.getMethodName());

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
        File file = IOTools.createTempFile(getClass().getName() + "." + testName.getMethodName());

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

    @Test
    public void getHostname() throws IOException {
        System.out.println("exec hostname: " + OS.HostnameHolder.execHostname());
        final String hostName = OS.getHostName();
        System.out.println("hostname: " + hostName);
        assertNotNull(hostName);
        assertNotEquals("", hostName);

        assumeTrue(OS.isWindows() || OS.isLinux() || OS.isMacOSX());
        assertNotEquals("localhost", hostName);
    }

    @Test
    public void getIPAddress() {
        System.out.println("getIpAddressByLocalHost: " + OS.IPAddressHolder.getIpAddressByLocalHost());
        System.out.println("getIpAddressByDatagram " + OS.IPAddressHolder.getIpAddressByDatagram());
        System.out.println("getIpAddressBySocket: " + OS.IPAddressHolder.getIpAddressBySocket());

        final String ipAddress = OS.getIPAddress();
        System.out.println("ipAddress: " + ipAddress);
        assertNotNull(ipAddress);
        assertNotEquals("", ipAddress);

        assumeTrue(OS.isWindows() || OS.isLinux() || OS.isMacOSX());
        assertNotEquals("0.0.0.0", ipAddress);
    }

    @Test
    public void getTarget() {
        String target = OS.getTarget();
        if (!target.endsWith("/target"))
            assertEquals("target", target);
    }

    @Test
    public void getTmp() {
        String tmp = OS.getTmp();
        assertNotNull(tmp);
    }

    @Test
    public void mapAlign() {
        // Testing for 64 bytes alignment
        assertEquals(0, OS.mapAlign(0, 64)); // Perfectly aligned already
        assertEquals(64, OS.mapAlign(1, 64)); // Not aligned, should round up to 64
        assertEquals(128, OS.mapAlign(96, 64)); // Not aligned, should round up to 128

        // Testing for 1024 bytes alignment
        assertEquals(0, OS.mapAlign(0, 1024)); // Perfectly aligned already
        assertEquals(1024, OS.mapAlign(1024, 1024)); // Perfectly aligned already
        assertEquals(2048, OS.mapAlign(1025, 1024)); // Not aligned, should round up to 2048

        // Testing for 4096 bytes alignment
        assertEquals(0, OS.mapAlign(0, 4096)); // Perfectly aligned already
        assertEquals(4096, OS.mapAlign(1, 4096)); // Not aligned, should round up to 4096
        assertEquals(4096, OS.mapAlign(4096, 4096)); // Perfectly aligned already
        assertEquals(8192, OS.mapAlign(4097, 4096)); // Not aligned, should round up to 8192

        // Testing for 2M bytes alignment (hugetlbfs)
        int customPageSize = 2 * 1024 * 1024;
        assertEquals(0, OS.mapAlign(0, customPageSize)); // Perfectly aligned already
        assertEquals(customPageSize, OS.mapAlign(1, customPageSize)); // Not aligned, should round up to higher closest
        assertEquals(customPageSize, OS.mapAlign(customPageSize, customPageSize)); // Perfectly aligned already
        assertEquals(2 * customPageSize, OS.mapAlign(customPageSize + 1, customPageSize)); // Not aligned, should round up to higher closest
        assertEquals(2 * customPageSize, OS.mapAlign(2 * customPageSize - 1, customPageSize)); // Not aligned, should round up to higher closest

        // Testing with page alignment equal to 1 (should not change the offset)
        assertEquals(42, OS.mapAlign(42, 1)); // Alignment of 1, no change

        // Edge cases: large numbers
        assertEquals(1_073_741_824L, OS.mapAlign(1_073_741_823L, 4096)); // 1 GiB - 1 rounded up to next page

        // Testing negative cases (should throw an exception)
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1, 64));
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(10, -64));
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(10, 0));
    }

    @Test
    public void pageAlign() {
        // Testing for 64 bytes alignment
        assertEquals(0, OS.pageAlign(0, 64)); // Perfectly aligned already
        assertEquals(64, OS.pageAlign(1, 64)); // Not aligned, should round up to 64
        assertEquals(128, OS.pageAlign(96, 64)); // Not aligned, should round up to 128

        // Testing for 1024 bytes alignment
        assertEquals(0, OS.pageAlign(0, 1024)); // Perfectly aligned already
        assertEquals(1024, OS.pageAlign(1024, 1024)); // Perfectly aligned already
        assertEquals(2048, OS.pageAlign(1025, 1024)); // Not aligned, should round up to 2048

        // Testing for 4096 bytes alignment
        assertEquals(0, OS.pageAlign(0, 4096)); // Perfectly aligned already
        assertEquals(4096, OS.pageAlign(1, 4096)); // Not aligned, should round up to 4096
        assertEquals(4096, OS.pageAlign(4096, 4096)); // Perfectly aligned already
        assertEquals(8192, OS.pageAlign(4097, 4096)); // Not aligned, should round up to 8192

        // Testing for 2M bytes alignment (hugetlbfs)
        int customPageSize = 2 * 1024 * 1024;
        assertEquals(0, OS.pageAlign(0, customPageSize)); // Perfectly aligned already
        assertEquals(customPageSize, OS.pageAlign(1, customPageSize)); // Not aligned, should round up to higher closest
        assertEquals(customPageSize, OS.pageAlign(customPageSize, customPageSize)); // Perfectly aligned already
        assertEquals(2 * customPageSize, OS.pageAlign(customPageSize + 1, customPageSize)); // Not aligned, should round up to higher closest
        assertEquals(2 * customPageSize, OS.pageAlign(2 * customPageSize - 1, customPageSize)); // Not aligned, should round up to higher closest
    }

    @Test
    public void testGetUserName() {
        String expectedUserName = System.getProperty("user.name");
        assertEquals(expectedUserName, OS.getUserName());
    }

    @Test
    public void testPageAlign() {
        long size = 12345;
        long expectedAlignedSize = (size + OS.pageSize() - 1) & ~(OS.pageSize() - 1);
        assertEquals(expectedAlignedSize, OS.pageAlign(size));
    }

    @Test
    public void testMapAlign() {
        long offset = 6000;
        long expectedAlignedOffset = (offset + OS.defaultOsPageSize() - 1) & ~(OS.defaultOsPageSize() - 1);
        assertEquals(expectedAlignedOffset, OS.mapAlign(offset));

        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1));
    }

    @Test
    public void testGetProcessId0() {
        int processId = OS.getProcessId0();
        assertTrue(processId > 0);
        // Additional checks can be added if there are known constraints on the process ID.
    }

    @Test
    public void testGetPidMax() {
        long pidMax = OS.getPidMax();
        assertTrue(pidMax > 0);
        // Specific value checks can be added for different OS types if known.
    }

    @Test
    public void testUserDir() {
        String expectedUserDir = System.getProperty("user.dir");
        assertEquals(expectedUserDir, OS.userDir());
    }

    @Test
    public void testGetHostName0() {
        String expectedHostName = null;

        if (OS.isWindows()) {
            expectedHostName = System.getenv("COMPUTERNAME");
            if (expectedHostName != null) {
                expectedHostName = expectedHostName.toLowerCase();
            }
        }

        if (expectedHostName == null) {
            try {
                expectedHostName = InetAddress.getLocalHost().getHostName();
            } catch (Throwable ignored) {
                expectedHostName = "localhost"; // Fallback to localhost if all else fails
            }
        }

        assertEquals(expectedHostName, OS.HostnameHolder.HOST_NAME);
    }
}
