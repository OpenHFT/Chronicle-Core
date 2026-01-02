/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.util.Time;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class IOToolsTest extends CoreTestCommon {

    @Test
    @DisplayName("isClosedException recognises closed socket errors properly")
    void testIsClosedException() {
        Exception closedConnectionException = new IOException("Connection reset by peer");
        assertTrue(IOTools.isClosedException(closedConnectionException), "connection reset exception should be recognized as closed connection");

        Exception otherException = new IOException("Some other IO error");
        assertFalse(IOTools.isClosedException(otherException), "generic IO error should not be recognized as closed connection");
    }

    @Test
    @DisplayName("writeFile writes bytes to filesystem safely")
    void testWriteFile() throws IOException {
        String testFilename = "testFile.tmp";
        String testData = "Test Data";

        byte[] encoded = testData.getBytes(UTF_8);
        IOTools.writeFile(testFilename, encoded);

        Path path = Paths.get(testFilename);
        assertTrue(Files.exists(path), "file should exist after writing");
        assertArrayEquals(encoded, Files.readAllBytes(path), "file content should match written data");

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("tempName preserves base name and extension")
    void testTempName() {
        String filename = "test.txt";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename, "temp filename should differ from source name: " + tempFilename);
        assertTrue(tempFilename.startsWith("test"), "temp filename should start with \"test\": " + tempFilename);
        assertTrue(tempFilename.endsWith(".txt"), "temp filename should end with \".txt\": " + tempFilename);
    }

    @Test
    @DisplayName("clean releases direct buffer without error")
    void testClean() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);

        IOTools.clean(bb);
        assertTrue(true, "execution should reach this point without exception"); // If we reach here, the test passes
    }

    @Test
    @DisplayName("createDirectories creates target directory path successfully")
    void testCreateDirectories() throws IOException {
        Path tempDir = Paths.get("tempDir");
        IOTools.createDirectories(tempDir);

        assertTrue(Files.isDirectory(tempDir), "directory should exist after creation");

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("isDirectBuffer recognises direct and heap buffers")
    void testIsDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertTrue(IOTools.isDirectBuffer(directBuffer), "direct buffer should be identified as direct by IOTools");
        assertFalse(IOTools.isDirectBuffer(nonDirectBuffer), "heap buffer should not be identified as direct by IOTools");
    }

    @Test
    @DisplayName("addressFor returns non-zero for direct buffer allocation")
    void testAddressFor() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        long address = IOTools.addressFor(directBuffer);

        assertNotEquals(0, address, "addressFor should return non-zero address for direct buffer instance: " + address);
    }

    @Test
    @DisplayName("deleteDirWithFiles removes directory and contents fully")
    void testDeleteDirWithFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        File tempFile = Files.createTempFile(tempDir, "test", ".tmp").toFile();

        assertTrue(tempFile.exists(), "temp file should exist before deletion");
        assertTrue(IOTools.deleteDirWithFiles(tempDir.toFile()), "directory deletion should succeed");

        assertFalse(tempFile.exists(), "temp file should not exist after directory deletion");
        assertFalse(tempDir.toFile().exists(), "directory should not exist after deletion");
    }

    @Test
    @DisplayName("readAsBytes preserves stream content bytes exactly")
    void testReadAsBytes() throws IOException {
        String testData = "Test Data";
        byte[] encoded = testData.getBytes(UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);

        byte[] bytes = IOTools.readAsBytes(bais);

        assertArrayEquals(encoded, bytes, "encoding/decoding should preserve data");
    }

    @Test
    @DisplayName("readFile by path succeeds in parallel")
    void readFileManyTimesByPath() {
        final int iterations = 3_000;
        final LongAccumulator accumulator = new LongAccumulator(Long::sum, 0);

        IntStream.range(0, iterations)
                .parallel()
                .forEach(i -> {
                    try {
                        IOTools.readFile(IOToolsTest.class, "readFileManyTimes.txt");
                        accumulator.accumulate(1);
                    } catch (IOException ioe) {
                        Jvm.rethrow(ioe);
                    }
                });

        assertEquals(iterations, accumulator.get(), "all parallel read operations should complete successfully");
    }

    @Test
    @DisplayName("readFile by file succeeds in parallel")
    void readFileManyTimesByFile() throws IOException {
        final int iterations = 3_000;
        final LongAccumulator accumulator = new LongAccumulator(Long::sum, 0);

        String file = OS.getTarget() + "/readFileManyTimes.txt";
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("Delete me\n".getBytes(UTF_8));
        }

        IntStream.range(0, iterations)
                .parallel()
                .forEach(i -> {
                    try {
                        IOTools.readFile(IOToolsTest.class, file);
                        accumulator.accumulate(1);
                    } catch (IOException ioe) {
                        Jvm.rethrow(ioe);
                    }
                });

        assertEquals(iterations, accumulator.get(), "all parallel file read operations should complete successfully");
    }

    @Test
    @DisplayName("cleaner releases direct buffer memory safely")
    void shouldCleanDirectBuffer() {
        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.captureReservedMemorySnapshot(IOTools::clean);
        assertTrue(snapshot.before <= snapshot.after, "reserved memory should not increase after cleaning direct buffer: before=" + snapshot.before + ", after=" + snapshot.after);
    }

    @Test
    @DisplayName("createDirectories fails on broken symbolic link")
    void createDirectoriesWithBrokenLink() throws IOException, IllegalStateException {
        Assumptions.assumeTrue(OS.isLinux(), "requires Linux symlink behaviour for broken link check");

        String path = OS.getTarget();
        Path link = Paths.get(path, "link2nowhere" + Time.uniqueId());
        Path nowhere = Paths.get(path, "nowhere");
        if (Files.isSymbolicLink(link)) {
            Files.delete(link);
            if (Files.isSymbolicLink(link)) {
                throw new IllegalStateException("Still exists");
            }
        }
        Files.createSymbolicLink(link, nowhere);

        try {
            IOTools.createDirectories(Paths.get(link.toString(), "subdir" + Time.uniqueId()));
            fail("createDirectories should throw IOException for broken symbolic link");
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(),
                    "broken link createDirectories should throw IOException, not subclass");
            assertTrue(ioe.getMessage().startsWith("Symbolic link from "),
                    "error message should start with \"Symbolic link from \": " + ioe.getMessage());
            assertTrue(ioe.getMessage().endsWith("nowhere is broken"),
                    "error message should end with \"nowhere is broken\": " + ioe.getMessage());
        } finally {
            Files.delete(link);
        }
    }

    @Test
    @DisplayName("createDirectories fails for read-only directory permissions")
    void createDirectoriesReadOnly() throws IOException, IllegalStateException {
        Assumptions.assumeTrue(OS.isLinux(), "requires Linux permissions for read-only directory check");

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only" + Time.uniqueId());
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        boolean writable = ro.toFile().canWrite();
        if (OS.isWsl() && writable) {
            Assumptions.assumeTrue(false, "WSL does not enforce read-only permissions on this filesystem");
        }
        assertFalse(writable, "read-only directory should not be writable: " + ro);
        boolean restored;
        IOException deleteFailure = null;
        try {
            IOTools.createDirectories(Paths.get(ro.toString(), "subdir" + Time.uniqueId()));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(),
                    "read-only createDirectories should throw IOException, not subclass");
            assertTrue(ioe.getMessage().startsWith("Cannot write to "),
                    "error message should start with \"Cannot write to \": " + ioe.getMessage());
        } finally {
            restored = ro.toFile().setWritable(true);
            if (restored) {
                try {
                    Files.delete(ro);
                } catch (IOException e) {
                    deleteFailure = e;
                }
            }
        }
        if (!restored)
            throw new IllegalStateException("Cannot make read-write");
        if (deleteFailure != null)
            throw deleteFailure;
    }

    @Test
    @DisplayName("createDirectories rejects file in place of directory")
    void cannotTurnAfileIntoADirectory() throws IOException {
        Assumptions.assumeTrue(OS.isLinux(), "requires Linux file and directory conflict handling");

        String path = OS.getTarget();
        Path file = Paths.get(path, "test-file" + Time.uniqueId());
        File asFile = file.toFile();
        if (asFile.exists() && !asFile.delete())
            throw new IOException("Cannot delete pre-existing file " + asFile);
        asFile.deleteOnExit();
        assertTrue(asFile.createNewFile(), "test file should be created before directory attempt: " + asFile);
        try {
            IOTools.createDirectories(Paths.get(file.toString(), "subdir" + Time.uniqueId()));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(),
                    "file-backed createDirectories should throw IOException, not subclass");
            assertTrue(ioe.getMessage().startsWith("Cannot create a directory with the same name as a file "),
                    "error message should start with file/directory conflict text: " + ioe.getMessage());
        }
    }

    @Test
    @DisplayName("isDirectBuffer handles direct and heap buffers")
    void isDirectBuffer() {
        assertTrue(IOTools.isDirectBuffer(ByteBuffer.allocateDirect(1)), "direct buffer should be identified as direct in small buffer check");
        assertFalse(IOTools.isDirectBuffer(ByteBuffer.allocate(1)), "heap buffer should not be identified as direct in small buffer check");
    }

    @Test
    @DisplayName("addressFor returns non-zero for newly allocated direct buffer")
    void addressFor() {
        long address = IOTools.addressFor(ByteBuffer.allocateDirect(1));
        assertNotEquals(0L, address, "addressFor should return non-zero address for freshly allocated direct buffer: " + address);
    }

    @Test
    @DisplayName("addressFor rejects heap ByteBuffer instances safely")
    void addressFor2() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        assertThrows(ClassCastException.class, () -> IOTools.addressFor(bb),
                "addressFor should throw when provided a heap ByteBuffer");
    }

    @Test
    @DisplayName("normaliseIOStatus returns expected IO status values")
    void normaliseIOStatus() {
        final int actual = IOTools.IOSTATUS_INTERRUPTED;
        assertEquals(-3, actual, "IOSTATUS_INTERRUPTED should be -3 but was " + actual);

        assertEquals(-3, IOTools.normaliseIOStatus(-3), "normalized IO status should match original interrupted status value");
    }

    @Test
    @DisplayName("writing to closed sockets throws IOException")
    void connectionClosed() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            // Some CI environments disallow socket operations; skip in that case.
            Assumptions.assumeTrue(false, "socket bind blocked; loopback required for closed-socket test");
            return;
        }
        Socket s = new Socket("localhost", ss.getLocalPort());
        final OutputStream os = s.getOutputStream();
        Socket s2 = ss.accept();
        s2.close();
        ss.close();
        final byte[] bytes = new byte[512];
        try {
            for (int i = 0; i < 100; i++) {
                os.write(bytes);
            }
            fail("writing to closed connection should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "closed connection should be recognised as closed: " + ioe);
        } finally {
            os.close();
        }
        try {
            s2.getOutputStream().write(bytes);
            fail("writing to closed socket output stream should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "closed socket output stream should be recognised as closed: " + ioe);
        }
    }

    @Test
    @DisplayName("writing to closed channel throws IOException")
    void connectionClosed2() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "socket channel test needs ServerSocket; network access blocked");
            return;
        }
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", ss.getLocalPort()));
        Socket s2 = ss.accept();
        s2.close();
        ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
        try {
            OutputStream os = s2.getOutputStream();
            os.close();
            os.write(1);
            fail("writing to already closed output stream should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "already closed output stream should be recognised as closed: " + ioe);
        }
        ss.close();
        try {
            for (int i = 0; i < 100; i++) {
                bytes.clear();
                sc.write(bytes);
            }
            fail("writing to socket channel after server socket closed should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "closed socket channel should be recognised as closed: " + ioe);
        } finally {
            sc.close();
        }
    }

    @Test
    @DisplayName("socket channel closes during write loop")
    void connectionClosed3() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "socket close race test needs ServerSocket; network access blocked");
            return;
        }
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", ss.getLocalPort()));
        Socket s2 = ss.accept();
        ss.close();
        ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
        Thread t = new Thread(() -> {
            Jvm.pause(100);
            try {
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "close~thread");
        t.start();
        try {
            for (int i = 0; i < 10000; i++) {
                bytes.clear();
                final int write = sc.write(bytes);
                assertTrue(write > 0, "connectionClosed3: each write should transfer bytes at index " + i);
            }
            fail("writing to socket channel closed by background thread should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "background close should be recognised as closed: " + ioe);
        } finally {
            s2.close();
            sc.close();
        }
    }

    @Test
    @DisplayName("interrupted channel write throws IOException as expected")
    void connectionClosed4() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "interrupt close test needs ServerSocket; network access blocked");
            return;
        }
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", ss.getLocalPort()));
        Socket s2 = ss.accept();
        ss.close();
        ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
        Thread main = Thread.currentThread();
        Thread t = new Thread(() -> {
            Jvm.pause(100);
            main.interrupt();
            Jvm.pause(10);
            try {
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "close~thread");
        t.setDaemon(true);
        t.start();
        try {
            for (int i = 0; i < 10000; i++) {
                bytes.clear();
                final int write = sc.write(bytes);
                assertTrue(write > 0, "connectionClosed4: each write should transfer bytes at index " + i);
            }
            fail("writing to interrupted and closed socket channel should throw IOException");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), "interrupted closed channel should be recognised as closed: " + ioe);
        } finally {
            s2.close();
            sc.close();
        }
    }

    @Test
    @DisplayName("isClosedException detects ClosedChannelException by class name")
    void isClosedExceptionByClassName() {
        // Test the branch that checks class name contains "Close"
        java.nio.channels.ClosedChannelException cce = new java.nio.channels.ClosedChannelException();
        assertTrue(IOTools.isClosedException(cce),
                "ClosedChannelException should be recognised as closed by class name");
    }

    @Test
    @DisplayName("isClosedException returns false for non-IOException")
    void isClosedExceptionNonIOException() {
        Exception e = new RuntimeException("Connection reset by peer");
        assertFalse(IOTools.isClosedException(e),
                "RuntimeException should not be recognised as closed exception");
    }

    @Test
    @DisplayName("tempName handles filename without extension")
    void tempNameNoExtension() {
        String filename = "testfile";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename, "temp filename should differ from original");
        assertTrue(tempFilename.startsWith("testfile"), "temp filename should start with original name");
        assertFalse(tempFilename.contains("."), "temp filename without extension should not contain dot");
    }

    @Test
    @DisplayName("tempName handles filename with short extension")
    void tempNameShortExtension() {
        // Extension longer than 4 chars should not be treated as extension
        String filename = "test.longext";
        String tempFilename = IOTools.tempName(filename);

        // Since .longext is > 4 chars, it won't be treated as extension
        assertTrue(tempFilename.startsWith("test.longext"), "long extension should be treated as part of name");
    }

    @Test
    @DisplayName("writeFile handles gzip compression")
    void writeFileGzip() throws IOException {
        String testFilename = OS.getTarget() + "/testFile-" + Time.uniqueId() + ".gz";
        String testData = "Test Data for gzip";
        byte[] encoded = testData.getBytes(UTF_8);

        IOTools.writeFile(testFilename, encoded);

        Path path = Paths.get(testFilename);
        assertTrue(Files.exists(path), "gzip file should exist after writing");

        // Read back and verify it's actually gzipped
        try (java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(Files.newInputStream(path))) {
            byte[] read = IOTools.readAsBytes(gis);
            assertArrayEquals(encoded, read, "gzip file content should match written data after decompression");
        }

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("urlFor finds resource with leading slash")
    void urlForLeadingSlash() throws FileNotFoundException {
        // This tests the branch where name starts with /
        // Use a resource that exists - the test resource file
        java.net.URL url = IOTools.urlFor(IOToolsTest.class, "/readFileManyTimes.txt");
        assertNotNull(url, "urlFor should find resource with leading slash");
    }

    @Test
    @DisplayName("urlFor throws FileNotFoundException for missing resource")
    void urlForMissingResource() {
        assertThrows(FileNotFoundException.class,
                () -> IOTools.urlFor(IOToolsTest.class, "nonexistent-resource-xyz123.txt"),
                "urlFor should throw FileNotFoundException for missing resource");
    }

    @Test
    @DisplayName("shallowDeleteDirWithFiles by string path")
    void shallowDeleteDirWithFilesString() throws IOException {
        Path tempDir = Files.createTempDirectory("shallowTest");
        Files.createFile(tempDir.resolve("test.txt"));

        assertTrue(IOTools.shallowDeleteDirWithFiles(tempDir.toString()),
                "shallowDeleteDirWithFiles should succeed on flat directory");
        assertFalse(tempDir.toFile().exists(), "directory should be deleted");
    }

    @Test
    @DisplayName("deleteDirWithFiles by string array")
    void deleteDirWithFilesStringArray() throws IOException {
        Path tempDir1 = Files.createTempDirectory("deleteTest1");
        Path tempDir2 = Files.createTempDirectory("deleteTest2");

        assertTrue(IOTools.deleteDirWithFiles(tempDir1.toString(), tempDir2.toString()),
                "deleteDirWithFiles should succeed for multiple directories");
        assertFalse(tempDir1.toFile().exists(), "first directory should be deleted");
        assertFalse(tempDir2.toFile().exists(), "second directory should be deleted");
    }

    @Test
    @DisplayName("deleteDirWithFiles returns false for non-existent directory")
    void deleteDirWithFilesNonExistent() {
        assertFalse(IOTools.deleteDirWithFiles(new File("/nonexistent/path/xyz123")),
                "deleteDirWithFiles should return false for non-existent directory");
    }

    @Test
    @DisplayName("deleteDirWithFilesOrThrow succeeds for existing directory")
    void deleteDirWithFilesOrThrowSuccess() throws IOException {
        Path tempDir = Files.createTempDirectory("throwTest");
        Files.createFile(tempDir.resolve("test.txt"));

        assertDoesNotThrow(() -> IOTools.deleteDirWithFilesOrThrow(tempDir.toString()),
                "deleteDirWithFilesOrThrow should succeed for existing directory");
        assertFalse(tempDir.toFile().exists(), "directory should be deleted");
    }

    @Test
    @DisplayName("createTempFile creates file in target directory")
    void createTempFileTest() {
        File tempFile = IOTools.createTempFile("testPrefix");

        assertTrue(tempFile.getParentFile().exists(), "parent directory should exist");
        assertTrue(tempFile.getName().startsWith("testPrefix"), "file name should start with prefix");
        assertTrue(tempFile.getName().endsWith(".tmp"), "file name should end with .tmp");
    }

    @Test
    @DisplayName("createTempDirectory creates directory in target")
    void createTempDirectoryTest() {
        Path tempDir = IOTools.createTempDirectory("testDirPrefix");

        assertTrue(Files.isDirectory(tempDir), "temp directory should exist");
        assertTrue(tempDir.getFileName().toString().startsWith("testDirPrefix"),
                "directory name should start with prefix");

        // Cleanup
        IOTools.deleteDirWithFiles(tempDir.toFile());
    }

    @Test
    @DisplayName("unmonitor handles null and non-monitorable objects")
    void unmonitorHandlesNull() {
        assertDoesNotThrow(() -> IOTools.unmonitor(null),
                "unmonitor should handle null without throwing");
        assertDoesNotThrow(() -> IOTools.unmonitor("not a monitorable"),
                "unmonitor should handle non-monitorable without throwing");
    }

    @Test
    @DisplayName("open handles gzip input stream")
    void openGzipStream() throws IOException {
        // Create a gzip file first
        String testFilename = OS.getTarget() + "/openTest-" + Time.uniqueId() + ".gz";
        String testData = "Test data for open gzip";
        byte[] encoded = testData.getBytes(UTF_8);
        IOTools.writeFile(testFilename, encoded);

        // Test opening it via URL
        java.net.URL url = new File(testFilename).toURI().toURL();
        try (InputStream is = IOTools.open(url)) {
            byte[] read = IOTools.readAsBytes(is);
            assertArrayEquals(encoded, read, "open should decompress gzip content");
        }

        Files.deleteIfExists(Paths.get(testFilename));
    }

    @Test
    @DisplayName("readAsBytes handles FileInputStream specially")
    void readAsBytesFileInputStream() throws IOException {
        Path tempFile = Files.createTempFile("readAsBytes", ".txt");
        String testData = "FileInputStream test data";
        Files.write(tempFile, testData.getBytes(UTF_8));

        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            byte[] bytes = IOTools.readAsBytes(fis);
            assertEquals(testData, new String(bytes, UTF_8),
                    "readAsBytes should read FileInputStream content correctly");
        }

        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("deleteDirWithFilesOrWait succeeds within timeout")
    void deleteDirWithFilesOrWaitSuccess() throws IOException {
        Path tempDir = Files.createTempDirectory("waitTest");
        Files.createFile(tempDir.resolve("test.txt"));

        assertDoesNotThrow(() -> IOTools.deleteDirWithFilesOrWait(1000, tempDir.toFile()),
                "deleteDirWithFilesOrWait should succeed within timeout");
        assertFalse(tempDir.toFile().exists(), "directory should be deleted");
    }
}
