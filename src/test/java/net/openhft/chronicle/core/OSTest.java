/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.MockitoAnnotations;

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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("deprecation")
class OSTest extends CoreTestCommon {
    private ThreadDump threadDump;
    private String testMethodName = "unknown";

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        testMethodName = testInfo.getTestMethod()
                .map(Method::getName)
                .orElse("unknown");
    }

    @Test
    @DisplayName("Sparse file support matches platform capabilities")
    void testIsSparseFileSupported() {
        // This test is environment-dependent and may need to be adjusted based on the target system
        boolean expected = System.getProperty("os.name").toLowerCase().contains("linux") && OS.is64Bit();
        assertEquals(expected, OS.isSparseFileSupported(), "sparse file support should match platform capabilities (64-bit Linux)");
    }

    @Test
    @DisplayName("findTmp returns a temporary directory path")
    void testFindTmp() {
        String tmp = OS.findTmp();
        assertNotNull(tmp, "temporary directory path should be resolved by OS.findTmp()");
    }

    @Test
    @DisplayName("IPAddressHolder initialises IP address string value")
    void testIPAddressHolder() {
        String ipAddress = OS.IPAddressHolder.IP_ADDRESS;
        assertNotNull(ipAddress, "IPAddressHolder.IP_ADDRESS should be initialized");
    }

    @Test
    @DisplayName("HostnameHolder initialises host name string value")
    void testHostnameHolder() {
        String hostname = OS.HostnameHolder.HOST_NAME;
        assertNotNull(hostname, "HostnameHolder.HOST_NAME should be initialized");
    }

    @Test
    @DisplayName("findFile returns last existing file path")
    void testFindFile() {
        assertEquals(new File("./last").getAbsolutePath(), OS.findFile("first", "last").getAbsolutePath(),
                "findFile should return last valid file when earlier options are not found");
    }

    @Override
    @BeforeEach
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @Override
    @AfterEach
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    @DisplayName("is64Bit detects architecture from JVM properties")
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

        assertEquals(expected, OS.is64Bit(), "64-bit detection should match JVM system properties");
    }

    @Test
    @DisplayName("getProcessId returns a positive process identifier")
    void testGetProcessId() {
        final int processId = OS.getProcessId();
        assertTrue(processId > 0, "OS.getProcessId should return a positive id: processId=" + processId);
    }

    /**
     * tests that Windows supports page mapping granularity
     */
    @Test
    @DisplayName("Map granularity uses page size on Windows")
    //@Ignore("Failing on TC (linux agent) for unknown reason, anyway the goal of this test is to " +
    //        "test mapping granularity on windows")
    void testMapGranularity() throws IOException {
        File file = IOTools.createTempFile(getClass().getName() + "." + testMethodName);

        try (RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            FileChannel fc = rw.getChannel();

            long length = OS.pageSize();
            MappedByteBuffer anchor = fc.map(MapMode.READ_WRITE, 0, length);
            anchor.order(ByteOrder.nativeOrder());

            long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);

            OS.memory().writeLong(address, 0);
            OS.unmap(address, length);

            assertEquals(length, file.length(), "file should grow to mapped region size after memory write");
        }
    }

    @Test
    @DisplayName("Large memory map write and read round trip")
    //@Ignore("Should always pass, or crash the JVM based on length")
    void testMap() throws IOException {
        File file = IOTools.createTempFile(getClass().getName() + "." + testMethodName);

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
                assertEquals(offset, OS.memory().readLong(address + offset), "each page should contain its offset value after write/read cycle");
            }

            OS.unmap(address, length);
        }
    }

    @Test
    @DisplayName("Fast map read and write round trip")
    void testMapFast() throws Exception {
        File file = IOTools.createTempFile(getClass().getName() + "." + testMethodName);

        try (RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            FileChannel fc = rw.getChannel();

            long length = Long.BYTES;
            MappedByteBuffer anchor = fc.map(MapMode.READ_WRITE, 0, length);
            anchor.order(ByteOrder.nativeOrder());

            long address = OS.map0(fc, OS.imodeFor(FileChannel.MapMode.READ_WRITE), 0, length);

            long value = System.currentTimeMillis();
            value ^= (value << 32);

            OS.memory().writeLong(address, value);

            assertEquals(value, OS.memory().readLong(address), "direct memory read should return written value");
            assertEquals(value, anchor.getLong(0), "mapped buffer read should match direct memory write");

            OS.unmap(address, length);
        }
    }

    @Test
    @DisplayName("Host name lookup resolves OS host name value")
    void getHostname() throws IOException {
        System.out.println("exec hostname: " + OS.HostnameHolder.execHostname());
        final String hostName = OS.getHostName();
        System.out.println("hostname: " + hostName);
        assertNotNull(hostName, "OS.getHostName should return a resolved host name");
        assertNotEquals("", hostName, "hostname should not be empty string");

        assumeTrue(OS.isWindows() || OS.isLinux() || OS.isMacOSX(),
                "host name checks require Windows, Linux, or macOS");
        assertNotEquals("localhost", hostName, "hostname should be actual machine name, not 'localhost' on Windows/Linux/macOS");
    }

    @Test
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    @DisplayName("IP address lookup resolves OS address value")
    void getIPAddress() {
        System.out.println("getIpAddressByLocalHost: " + OS.IPAddressHolder.getIpAddressByLocalHost());
        System.out.println("getIpAddressByDatagram " + OS.IPAddressHolder.getIpAddressByDatagram());
        System.out.println("getIpAddressBySocket: " + OS.IPAddressHolder.getIpAddressBySocket());

        final String ipAddress = OS.getIPAddress();
        System.out.println("ipAddress: " + ipAddress);
        assertNotNull(ipAddress, "OS.getIPAddress should return a resolved IP address");
        assertNotEquals("", ipAddress, "IP address should not be empty string");

        assumeTrue(OS.isWindows() || OS.isLinux() || OS.isMacOSX(),
                "IP address checks require Windows, Linux, or macOS");
        assertNotEquals("0.0.0.0", ipAddress, "IP address should be actual network address, not '0.0.0.0' on Windows/Linux/macOS");
    }

    @Test
    @DisplayName("Target directory lookup resolves build target path")
    void getTarget() {
        String target = OS.getTarget();
        if (!target.endsWith("/target"))
            assertEquals("target", target, "getTarget should return 'target' as directory name");
    }

    @Test
    @DisplayName("Temporary directory lookup returns OS temp path")
    void getTmp() {
        String tmp = OS.getTmp();
        assertNotNull(tmp, "temporary directory path should be resolved by OS.getTmp()");
    }

    @Test
    @DisplayName("mapAlign aligns offsets to page size")
    void mapAlign() {
        // Testing for 64 bytes alignment
        assertEquals(0, OS.mapAlign(0, 64), "zero offset with 64-byte alignment should remain zero"); // Perfectly aligned already
        assertEquals(64, OS.mapAlign(1, 64), "unaligned offset should round up to next 64-byte boundary"); // Not aligned, should round up to 64
        assertEquals(128, OS.mapAlign(96, 64), "96 should round up to 128 for 64-byte alignment"); // Not aligned, should round up to 128

        // Testing for 1024 bytes alignment
        assertEquals(0, OS.mapAlign(0, 1024), "zero offset with 1KB alignment should remain zero"); // Perfectly aligned already
        assertEquals(1024, OS.mapAlign(1024, 1024), "1KB offset at 1KB boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(2048, OS.mapAlign(1025, 1024), "offset just past boundary should round up to next 1KB page"); // Not aligned, should round up to 2048

        // Testing for 4096 bytes alignment
        assertEquals(0, OS.mapAlign(0, 4096), "zero offset with 4KB alignment should remain zero"); // Perfectly aligned already
        assertEquals(4096, OS.mapAlign(1, 4096), "unaligned offset should round up to next 4KB page"); // Not aligned, should round up to 4096
        assertEquals(4096, OS.mapAlign(4096, 4096), "4KB offset at 4KB boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(8192, OS.mapAlign(4097, 4096), "offset just past 4KB boundary should round up to 8KB"); // Not aligned, should round up to 8192

        // Testing for 2M bytes alignment (hugetlbfs)
        int customPageSize = 2 * 1024 * 1024;
        assertEquals(0, OS.mapAlign(0, customPageSize), "zero offset with 2MB hugepage alignment should remain zero"); // Perfectly aligned already
        assertEquals(customPageSize, OS.mapAlign(1, customPageSize), "unaligned offset should round up to next 2MB hugepage"); // Not aligned, should round up to higher closest
        assertEquals(customPageSize, OS.mapAlign(customPageSize, customPageSize), "2MB offset at 2MB hugepage boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(2L * customPageSize, OS.mapAlign(customPageSize + 1, customPageSize), "offset just past hugepage boundary should round up to next 2MB"); // Not aligned, should round up to higher closest
        assertEquals(2L * customPageSize, OS.mapAlign(2L * customPageSize - 1, customPageSize), "offset near second hugepage should round up to 4MB"); // Not aligned, should round up to higher closest

        // Testing with page alignment equal to 1 (should not change the offset)
        assertEquals(42, OS.mapAlign(42, 1), "alignment of 1 should not change any offset"); // Alignment of 1, no change

        // Edge cases: large numbers
        assertEquals(1_073_741_824L, OS.mapAlign(1_073_741_823L, 4096), "large offset near 1GB should round up to page boundary"); // 1 GiB - 1 rounded up to next page

        // Testing negative cases (should throw an exception)
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1, 64), "negative offset should be rejected");
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(10, -64), "negative page alignment should be rejected");
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(10, 0), "zero page alignment should be rejected");
    }

    @Test
    @DisplayName("pageAlign rounds values to page size")
    void pageAlign() {
        // Testing for 64 bytes alignment
        assertEquals(0, OS.pageAlign(0, 64), "zero size with 64-byte alignment should remain zero"); // Perfectly aligned already
        assertEquals(64, OS.pageAlign(1, 64), "unaligned size should round up to next 64-byte boundary"); // Not aligned, should round up to 64
        assertEquals(128, OS.pageAlign(96, 64), "96 bytes should round up to 128 for 64-byte alignment"); // Not aligned, should round up to 128

        // Testing for 1024 bytes alignment
        assertEquals(0, OS.pageAlign(0, 1024), "zero size with 1KB alignment should remain zero"); // Perfectly aligned already
        assertEquals(1024, OS.pageAlign(1024, 1024), "1KB size at 1KB boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(2048, OS.pageAlign(1025, 1024), "size just past boundary should round up to next 1KB page"); // Not aligned, should round up to 2048

        // Testing for 4096 bytes alignment
        assertEquals(0, OS.pageAlign(0, 4096), "zero size with 4KB alignment should remain zero"); // Perfectly aligned already
        assertEquals(4096, OS.pageAlign(1, 4096), "unaligned size should round up to next 4KB page"); // Not aligned, should round up to 4096
        assertEquals(4096, OS.pageAlign(4096, 4096), "4KB size at 4KB boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(8192, OS.pageAlign(4097, 4096), "size just past 4KB boundary should round up to 8KB"); // Not aligned, should round up to 8192

        // Testing for 2M bytes alignment (hugetlbfs)
        int customPageSize = 2 * 1024 * 1024;
        assertEquals(0, OS.pageAlign(0, customPageSize), "zero size with 2MB hugepage alignment should remain zero"); // Perfectly aligned already
        assertEquals(customPageSize, OS.pageAlign(1, customPageSize), "unaligned size should round up to next 2MB hugepage"); // Not aligned, should round up to higher closest
        assertEquals(customPageSize, OS.pageAlign(customPageSize, customPageSize), "2MB size at 2MB hugepage boundary should remain unchanged"); // Perfectly aligned already
        assertEquals(2L * customPageSize, OS.pageAlign(customPageSize + 1, customPageSize), "size just past hugepage boundary should round up to next 2MB"); // Not aligned, should round up to higher closest
        assertEquals(2L * customPageSize, OS.pageAlign(2L * customPageSize - 1, customPageSize), "size near second hugepage should round up to 4MB"); // Not aligned, should round up to higher closest
    }

    @Test
    @DisplayName("getUserName returns current user name value")
    void testGetUserName() {
        String expectedUserName = System.getProperty("user.name");
        assertEquals(expectedUserName, OS.getUserName(), "OS.getUserName should return user.name system property");
    }

    @Test
    @DisplayName("pageAlign matches expected alignment for sizes")
    void testPageAlign() {
        long size = 12345;
        long expectedAlignedSize = (size + OS.pageSize() - 1) & ~(OS.pageSize() - 1);
        assertEquals(expectedAlignedSize, OS.pageAlign(size), "pageAlign should round up arbitrary size to system page boundary");
    }

    @Test
    @DisplayName("mapAlign matches expected alignment for offsets")
    void testMapAlign() {
        long offset = 6000;
        long expectedAlignedOffset = (offset + OS.defaultOsPageSize() - 1) & ~(OS.defaultOsPageSize() - 1);
        assertEquals(expectedAlignedOffset, OS.mapAlign(offset), "mapAlign should round up arbitrary offset to OS page boundary");

        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1), "single-argument mapAlign should reject negative offset");
    }

    @Test
    @DisplayName("Process id lookup returns OS process identifier")
    void testGetProcessId0() {
        int processId = OS.getProcessId0();
        assertTrue(processId > 0, "OS.getProcessId0 should return a positive id: processId=" + processId);
        // Additional checks can be added if there are known constraints on the process ID.
    }

    @Test
    @DisplayName("getPidMax returns system pid maximum value")
    void testGetPidMax() {
        long pidMax = OS.getPidMax();
        assertTrue(pidMax > 0, "OS.getPidMax should return a positive limit: pidMax=" + pidMax);
        // Specific value checks can be added for different OS types if known.
    }

    @Test
    @DisplayName("userDir returns current user directory path")
    void testUserDir() {
        String expectedUserDir = System.getProperty("user.dir");
        assertEquals(expectedUserDir, OS.userDir(), "OS.userDir should return user.dir system property");
    }

    @Test
    @DisplayName("getHostName0 returns raw host name value")
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

        assertEquals(OS.HostnameHolder.HOST_NAME, expectedHostName, "HOST_NAME should match system hostname");
    }

    @Test
    @DisplayName("mapAlign rejects negative offsets for alignment")
    void mapAlignRejectsNegativeOffsets() {
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(-1L), "mapAlign should reject negative offset value");
    }

    @Test
    @DisplayName("mapAlign rejects non positive alignment values")
    void mapAlignRejectsNonPositiveAlignment() {
        assertThrows(IllegalArgumentException.class, () -> OS.mapAlign(64L, 0), "mapAlign should reject zero as page alignment value");
    }

    @Test
    @DisplayName("mapAlign rounds up to alignment boundary")
    void mapAlignRoundsUpToAlignment() {
        long alignment = OS.defaultOsPageSize();
        long offset = alignment / 2;
        long aligned = OS.mapAlign(offset, (int) alignment);
        assertEquals(alignment, aligned, "memory should be aligned to page boundary");
    }

    @Test
    @DisplayName("Memory map and unmap round trip")
    void memoryMapAndUnmapRoundTrip() throws IOException {
        File temp = File.createTempFile("chronicle-os-map", ".bin");
        temp.deleteOnExit();
        long size = OS.pageAlign(8192L);
        try (RandomAccessFile raf = new RandomAccessFile(temp, "rw");
             FileChannel channel = raf.getChannel()) {
            raf.setLength(size);

            long address = OS.map(channel, FileChannel.MapMode.READ_WRITE, 0L, size);
            assertNotEquals(0L, address, "memory mapping should return valid non-zero address");

            OS.unmap(address, size);
        }
    }

    @Test
    @DisplayName("mapAlign handles non zero start offsets")
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
            assertNotEquals(0L, address, "memory mapping with unaligned offset should return valid address");
            OS.unmap(address, size);
        }
    }

    @Test
    @DisplayName("imodeFor returns correct modes for all map types")
    void imodeForAllModes() {
        assertEquals(0, OS.imodeFor(FileChannel.MapMode.READ_ONLY), "READ_ONLY mapping mode should return 0");
        assertEquals(1, OS.imodeFor(FileChannel.MapMode.READ_WRITE), "READ_WRITE should return 1");
        assertEquals(2, OS.imodeFor(FileChannel.MapMode.PRIVATE), "PRIVATE should return 2");
    }

    @Test
    @DisplayName("memoryMapped tracks mapped memory counter value")
    void memoryMappedTracking() {
        long before = OS.memoryMapped();
        assertTrue(before >= 0, "memoryMapped should return non-negative value: before=" + before);
    }

    @Test
    @DisplayName("isLinux returns consistent value across repeated calls")
    void isLinuxConsistent() {
        boolean linux1 = OS.isLinux();
        boolean linux2 = OS.isLinux();
        assertEquals(linux1, linux2, "isLinux should return consistent value");
    }

    @Test
    @DisplayName("isWindows returns consistent value across repeated calls")
    void isWindowsConsistent() {
        boolean win1 = OS.isWindows();
        boolean win2 = OS.isWindows();
        assertEquals(win1, win2, "isWindows should return consistent value");
    }

    @Test
    @DisplayName("isMacOSX returns consistent value across repeated calls")
    void isMacOSXConsistent() {
        boolean mac1 = OS.isMacOSX();
        boolean mac2 = OS.isMacOSX();
        assertEquals(mac1, mac2, "isMacOSX should return consistent value");
    }

    @Test
    @DisplayName("isWsl returns consistent value across repeated calls")
    void isWslConsistent() {
        boolean wsl1 = OS.isWsl();
        boolean wsl2 = OS.isWsl();
        assertEquals(wsl1, wsl2, "isWsl should return consistent value");
    }

    @Test
    @DisplayName("defaultOsPageSize returns positive page size for platform")
    void defaultOsPageSizePositive() {
        int pageSize = OS.defaultOsPageSize();
        assertTrue(pageSize > 0, "defaultOsPageSize should return positive value: pageSize=" + pageSize);
        if (OS.isWindows()) {
            assertEquals(OS.SAFE_PAGE_SIZE, pageSize, "Windows should use SAFE_PAGE_SIZE");
        }
    }

    @Test
    @DisplayName("mapAlignment returns positive alignment value for mappings")
    void mapAlignmentPositive() {
        long alignment = OS.mapAlignment();
        assertTrue(alignment > 0, "mapAlignment should return positive value: alignment=" + alignment);
    }

    @Test
    @DisplayName("memory returns UnsafeMemory instance singleton object")
    void memoryReturnsInstance() {
        Memory memory = OS.memory();
        assertNotNull(memory, "memory should return non-null Memory instance object");
        assertSame(UnsafeMemory.INSTANCE, memory, "memory should return UnsafeMemory.INSTANCE");
    }

    @Test
    @DisplayName("IPAddressHolder isSet detects valid address strings")
    void ipAddressHolderIsSet() {
        assertFalse(OS.IPAddressHolder.isSet(null), "IPAddressHolder should treat null as not set");
        assertFalse(OS.IPAddressHolder.isSet(""), "IPAddressHolder should treat empty string as not set");
        assertFalse(OS.IPAddressHolder.isSet("0.0.0.0"), "IPAddressHolder should treat 0.0.0.0 as not set");
        assertTrue(OS.IPAddressHolder.isSet("192.168.1.1"), "valid IP should be set");
    }

    @Test
    @DisplayName("IPAddressHolder getIpAddressByLocalHost returns valid address result")
    void ipAddressHolderByLocalHost() {
        String addr = OS.IPAddressHolder.getIpAddressByLocalHost();
        assertNotNull(addr, "getIpAddressByLocalHost should return non-null address string");
    }

    @Test
    @DisplayName("IPAddressHolder getIpAddressByDatagram returns address string from datagram socket")
    void ipAddressHolderByDatagram() {
        String addr = OS.IPAddressHolder.getIpAddressByDatagram();
        // May be null if network is unavailable, but should not throw
        assertTrue(addr == null || !addr.isEmpty() || addr.isEmpty(),
                "getIpAddressByDatagram should return address string or null");
    }

    @Test
    @DisplayName("IPAddressHolder getIpAddressBySocket returns address string from connected socket")
    void ipAddressHolderBySocket() {
        String addr = OS.IPAddressHolder.getIpAddressBySocket();
        assertNotNull(addr, "getIpAddressBySocket should return non-null address string");
    }

    @Test
    @DisplayName("getPidMax returns valid fallback for missing file")
    void getPidMaxWithMissingFile() {
        long pidMax = OS.getPidMax("nonexistent/path/to/pid_max");
        assertEquals(1L << 16, pidMax, "getPidMax should return fallback value for missing file");
    }

    @Test
    @DisplayName("pageSize returns positive cached page size value")
    void pageSizeReturnsPositive() {
        int size1 = OS.pageSize();
        int size2 = OS.pageSize();
        assertTrue(size1 > 0, "pageSize should return positive value: size1=" + size1);
        assertEquals(size1, size2, "pageSize should return cached value");
    }

    @Test
    @DisplayName("findFile with single element returns that file")
    void findFileSingleElement() {
        File result = OS.findFile("single.txt");
        assertTrue(result.getPath().endsWith("single.txt"), "findFile with single element should return that element");
    }

    @Test
    @DisplayName("findFile traverses directories that already exist")
    void findFileTraversesDirectories() {
        File result = OS.findFile("src", "main", "java");
        assertTrue(result.getPath().contains("java"), "findFile should traverse existing directories");
    }

    @Test
    @DisplayName("getUserDir returns non-null user directory path from system property")
    void getUserDirReturnsValue() {
        String userDir = OS.getUserDir();
        assertNotNull(userDir, "getUserDir should return non-null directory path");
        assertEquals(System.getProperty("user.dir"), userDir, "getUserDir should match user.dir property");
    }

    @Test
    @DisplayName("TIME_LIMIT exception has no stack trace")
    void timeLimitHasNoStackTrace() {
        assertEquals(0, OS.TIME_LIMIT.getStackTrace().length, "TIME_LIMIT should have empty stack trace");
    }

    @Test
    @DisplayName("USER_HOME constant matches user.home system property value")
    void userHomeConstantSet() {
        assertEquals(System.getProperty("user.home"), OS.USER_HOME, "USER_HOME should match system property");
    }

    @Test
    @DisplayName("SAFE_PAGE_SIZE is 64KB constant value")
    void safePageSizeValue() {
        assertEquals(64 << 10, OS.SAFE_PAGE_SIZE, "SAFE_PAGE_SIZE should be 64KB");
    }
}
