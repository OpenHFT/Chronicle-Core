/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class OSTest extends CoreTestCommon {
    private ThreadDump threadDump;
    private String testName;

    @BeforeEach
    void beforeEachOSTest(TestInfo testInfo) {
        testName = testInfo.getTestMethod().map(Method::getName).orElse("unknown");
        threadDump = new ThreadDump();
    }

    @Test
    void testIsSparseFileSupported() {
        // This test is environment-dependent and may need to be adjusted based on the target system
        boolean expected = System.getProperty("os.name").toLowerCase().contains("linux") && OS.is64Bit();
        assertEquals(expected, OS.isSparseFileSupported());
    }

    @Test
    void testFindTmp() {
        String tmp = OS.findTmp();
        assertNotNull(tmp);
    }

    @Test
    void testIPAddressHolder() {
        String ipAddress = OS.IPAddressHolder.IP_ADDRESS;
        assertNotNull(ipAddress);
    }

    @Test
    void testHostnameHolder() {
        String hostname = OS.HostnameHolder.HOST_NAME;
        assertNotNull(hostname);
    }

    @Test
    void testFindFile() {
        assertEquals(new File("./last").getAbsolutePath(), OS.findFile("first", "last").getAbsolutePath());
    }

    @Override
    @AfterEach
    void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    void testIs64Bit() {
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
    void testGetProcessId() {
        final int processId = OS.getProcessId();
        assertTrue(processId > 0);
    }

    /**
     * tests that Windows supports page mapping granularity
     */
    @Test
    void testMapGranularity() throws IOException {
        File file = IOTools.createTempFile(getClass().getName() + "." + testName);

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
    void testMap() throws IOException {
        File file = IOTools.createTempFile(getClass().getName() + "." + testName);

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
    void testMapFast() throws Exception {
        File file = IOTools.createTempFile(getClass().getName() + "." + testName);

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
    void getHostname() throws IOException {
        final String hostName = OS.getHostName();
        System.out.println("hostname: " + hostName);
        assertNotNull(hostName);
        assertNotEquals("", hostName);

        assumeTrue(OS.isWindows() || OS.isLinux() || OS.isMacOSX());
        assertNotEquals("localhost", hostName);
    }

    @Test
    void getIPAddress() {
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
    void getTarget() {
        String target = OS.getTarget();
        if (!target.endsWith("/target"))
            assertEquals("target", target);
    }

    @Test
    void getTmp() {
        String tmp = OS.getTmp();
        assertNotNull(tmp);
    }

    @Test
    void mapAlign() {
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
    void pageAlign() {
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
    void testGetUserName() {
        String expectedUserName = System.getProperty("user.name");
        assertEquals(expectedUserName, OS.getUserName());
    }

    @Test
    void testPageAlign() {
        long size = 12345;
        long expectedAlignedSize = (size + OS.pageSize() - 1) & -OS.pageSize();
        assertEquals(expectedAlignedSize, OS.pageAlign(size));
    }

    @Test
    void testMapAlign() {
        long offset = 6000;
        long expectedAlignedOffset = (offset + OS.defaultOsPageSize() - 1) & -OS.defaultOsPageSize();
        assertEquals(expectedAlignedOffset, OS.mapAlign(offset));

        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1));
    }

    @Test
    void testGetProcessId0() {
        int processId = OS.getProcessId0();
        assertTrue(processId > 0);
        // Additional checks can be added if there are known constraints on the process ID.
    }

    @Test
    void testGetPidMax() {
        long pidMax = OS.getPidMax();
        assertTrue(pidMax > 0);
        // Specific value checks can be added for different OS types if known.
    }

    @Test
    void testUserDir() {
        String expectedUserDir = System.getProperty("user.dir");
        assertEquals(expectedUserDir, OS.userDir());
    }

    @Test
    void testGetHostName0() {
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

    @Test
    void mapAlignRejectsNegativeOffsets() {
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1L));
    }

    @Test
    void mapAlignRejectsNonPositiveAlignment() {
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(64L, 0));
    }

    @Test
    void mapAlignRoundsUpToAlignment() {
        long alignment = OS.defaultOsPageSize();
        long offset = alignment / 2;
        long aligned = OS.mapAlign(offset, (int) alignment);
        assertEquals(alignment, aligned);
    }

    @Test
    void memoryMapAndUnmapRoundTrip() throws IOException {
        File temp = File.createTempFile("chronicle-os-map", ".bin");
        temp.deleteOnExit();
        long size = OS.pageAlign(8192L);
        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(size);

            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, 0L, size);
            assertNotEquals(0L, address, "Expected non-zero mapping address");

            OS.unmap(address, size);
        }
    }

    @Test
    void mapAlignHandlesNonZeroStartOffsets() throws IOException {
        File temp = File.createTempFile("chronicle-os-map-offset", ".bin");
        temp.deleteOnExit();
        long pageSize = OS.pageSize();
        long start = pageSize / 2; // intentionally unaligned
        long size = OS.pageAlign(4096L);

        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(start + size);

            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, start, size);
            assertNotEquals(0L, address);
            OS.unmap(address, size);
        }
    }
}
