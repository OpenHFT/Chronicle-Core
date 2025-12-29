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

    @DisplayName("isClosedException recognises closed socket errors properly")
    @Test
    void testIsClosedException() {
        Exception closedConnectionException = new IOException("Connection reset by peer");
        assertTrue(IOTools.isClosedException(closedConnectionException), "connection reset exception should be recognized as closed connection");

        Exception otherException = new IOException("Some other IO error");
        assertFalse(IOTools.isClosedException(otherException), "generic IO error should not be recognized as closed connection");
    }

    @DisplayName("writeFile writes bytes to filesystem safely")
    @Test
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

    @DisplayName("tempName preserves base name and extension")
    @Test
    void testTempName() {
        String filename = "test.txt";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename, "temp filename should differ from source name: " + tempFilename);
        assertTrue(tempFilename.startsWith("test"), "temp filename should start with \"test\": " + tempFilename);
        assertTrue(tempFilename.endsWith(".txt"), "temp filename should end with \".txt\": " + tempFilename);
    }

    @DisplayName("clean releases direct buffer without error")
    @Test
    void testClean() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);

        IOTools.clean(bb);
        assertTrue(true, "execution should reach this point without exception"); // If we reach here, the test passes
    }

    @DisplayName("createDirectories creates target directory path successfully")
    @Test
    void testCreateDirectories() throws IOException {
        Path tempDir = Paths.get("tempDir");
        IOTools.createDirectories(tempDir);

        assertTrue(Files.isDirectory(tempDir), "directory should exist after creation");

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(tempDir);
    }

    @DisplayName("isDirectBuffer recognises direct and heap buffers")
    @Test
    void testIsDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertTrue(IOTools.isDirectBuffer(directBuffer), "direct buffer should be identified as direct by IOTools");
        assertFalse(IOTools.isDirectBuffer(nonDirectBuffer), "heap buffer should not be identified as direct by IOTools");
    }

    @DisplayName("addressFor returns non-zero for direct buffer allocation")
    @Test
    void testAddressFor() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        long address = IOTools.addressFor(directBuffer);

        assertNotEquals(0, address, "addressFor should return non-zero address for direct buffer instance: " + address);
    }

    @DisplayName("deleteDirWithFiles removes directory and contents fully")
    @Test
    void testDeleteDirWithFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        File tempFile = Files.createTempFile(tempDir, "test", ".tmp").toFile();

        assertTrue(tempFile.exists(), "temp file should exist before deletion");
        assertTrue(IOTools.deleteDirWithFiles(tempDir.toFile()), "directory deletion should succeed");

        assertFalse(tempFile.exists(), "temp file should not exist after directory deletion");
        assertFalse(tempDir.toFile().exists(), "directory should not exist after deletion");
    }

    @DisplayName("readAsBytes preserves stream content bytes exactly")
    @Test
    void testReadAsBytes() throws IOException {
        String testData = "Test Data";
        byte[] encoded = testData.getBytes(UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);

        byte[] bytes = IOTools.readAsBytes(bais);

        assertArrayEquals(encoded, bytes, "encoding/decoding should preserve data");
    }

    @DisplayName("readFile by path succeeds in parallel")
    @Test
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

    @DisplayName("readFile by file succeeds in parallel")
    @Test
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

    @DisplayName("cleaner releases direct buffer memory safely")
    @Test
    void shouldCleanDirectBuffer() {
        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.captureReservedMemorySnapshot(IOTools::clean);
        assertTrue(snapshot.before <= snapshot.after, "reserved memory should not increase after cleaning direct buffer: before=" + snapshot.before + ", after=" + snapshot.after);
    }

    @DisplayName("createDirectories fails on broken symbolic link")
    @Test
    void createDirectoriesWithBrokenLink() throws IOException, IllegalStateException {
        Assumptions.assumeTrue(OS.isLinux());

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

    @DisplayName("createDirectories fails for read-only directory permissions")
    @Test
    void createDirectoriesReadOnly() throws IOException, IllegalStateException {
        Assumptions.assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only" + Time.uniqueId());
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        assertFalse(ro.toFile().canWrite(), "read-only directory should not be writable: " + ro);
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

    @DisplayName("createDirectories rejects file in place of directory")
    @Test
    void cannotTurnAfileIntoADirectory() throws IOException {
        Assumptions.assumeTrue(OS.isLinux());

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

    @DisplayName("isDirectBuffer handles direct and heap buffers")
    @Test
    void isDirectBuffer() {
        assertTrue(IOTools.isDirectBuffer(ByteBuffer.allocateDirect(1)), "direct buffer should be identified as direct in small buffer check");
        assertFalse(IOTools.isDirectBuffer(ByteBuffer.allocate(1)), "heap buffer should not be identified as direct in small buffer check");
    }

    @DisplayName("addressFor returns non-zero for newly allocated direct buffer")
    @Test
    void addressFor() {
        long address = IOTools.addressFor(ByteBuffer.allocateDirect(1));
        assertNotEquals(0L, address, "addressFor should return non-zero address for freshly allocated direct buffer: " + address);
    }

    @DisplayName("addressFor rejects heap ByteBuffer instances safely")
    @Test
    void addressFor2() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        assertThrows(ClassCastException.class, () -> IOTools.addressFor(bb),
                "addressFor should throw when provided a heap ByteBuffer");
    }

    @DisplayName("normaliseIOStatus returns expected IO status values")
    @Test
    void normaliseIOStatus() {
        final int actual = IOTools.IOSTATUS_INTERRUPTED;
        assertEquals(-3, actual, "IOSTATUS_INTERRUPTED should be -3 but was " + actual);

        assertEquals(-3, IOTools.normaliseIOStatus(-3), "normalized IO status should match original interrupted status value");
    }

    @DisplayName("writing to closed sockets throws IOException")
    @Test
    void connectionClosed() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            // Some CI environments disallow socket operations; skip in that case.
            Assumptions.assumeTrue(false, "Network not permitted in this environment");
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

    @DisplayName("writing to closed channel throws IOException")
    @Test
    void connectionClosed2() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "Network not permitted in this environment");
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

    @DisplayName("socket channel closes during write loop")
    @Test
    void connectionClosed3() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "Network not permitted in this environment");
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

    @DisplayName("interrupted channel write throws IOException as expected")
    @Test
    void connectionClosed4() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assumptions.assumeTrue(false, "Network not permitted in this environment");
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
}
