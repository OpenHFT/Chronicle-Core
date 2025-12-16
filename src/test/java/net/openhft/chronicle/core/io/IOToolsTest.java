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

public class IOToolsTest extends CoreTestCommon {

    @Test
    public void testIsClosedException() {
        Exception closedConnectionException = new IOException("Connection reset by peer");
        assertTrue(IOTools.isClosedException(closedConnectionException), "testIsClosedException: L34");

        Exception otherException = new IOException("Some other IO error");
        assertFalse(IOTools.isClosedException(otherException), "testIsClosedException: L37");
    }

    @Test
    public void testWriteFile() throws IOException {
        String testFilename = "testFile.tmp";
        String testData = "Test Data";

        byte[] encoded = testData.getBytes(UTF_8);
        IOTools.writeFile(testFilename, encoded);

        Path path = Paths.get(testFilename);
        assertTrue(Files.exists(path), "testWriteFile: L49");
        assertArrayEquals(encoded, Files.readAllBytes(path), "testWriteFile: L50");

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(path);
    }

    @Test
    public void testTempName() {
        String filename = "test.txt";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename);
        assertTrue(tempFilename.startsWith("test"), "testTempName: L62");
        assertTrue(tempFilename.endsWith(".txt"), "testTempName: L63");
    }

    @Test
    public void testClean() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);

        IOTools.clean(bb);
        assertTrue(true, "testClean: L71"); // If we reach here, the test passes
    }

    @Test
    public void testCreateDirectories() throws IOException {
        Path tempDir = Paths.get("tempDir");
        IOTools.createDirectories(tempDir);

        assertTrue(Files.isDirectory(tempDir), "testCreateDirectories: L79");

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testIsDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertTrue(IOTools.isDirectBuffer(directBuffer), "testIsDirectBuffer: L90");
        assertFalse(IOTools.isDirectBuffer(nonDirectBuffer), "testIsDirectBuffer: L91");
    }

    @Test
    public void testAddressFor() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        long address = IOTools.addressFor(directBuffer);

        assertNotEquals(0, address);
    }

    @Test
    public void testDeleteDirWithFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        File tempFile = Files.createTempFile(tempDir, "test", ".tmp").toFile();

        assertTrue(tempFile.exists(), "testDeleteDirWithFiles: L107");
        assertTrue(IOTools.deleteDirWithFiles(tempDir.toFile()), "testDeleteDirWithFiles: L108");

        assertFalse(tempFile.exists(), "testDeleteDirWithFiles: L110");
        assertFalse(tempDir.toFile().exists(), "testDeleteDirWithFiles: L111");
    }

    @Test
    public void testReadAsBytes() throws IOException {
        String testData = "Test Data";
        byte[] encoded = testData.getBytes(UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(encoded);

        byte[] bytes = IOTools.readAsBytes(bais);

        assertArrayEquals(encoded, bytes, "testReadAsBytes: L122");
    }

    @Test
    public void readFileManyTimesByPath() {
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

        assertEquals(iterations, accumulator.get(), "readFileManyTimesByPath: L141");
    }

    @Test
    public void readFileManyTimesByFile() throws IOException {
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

        assertEquals(iterations, accumulator.get(), "readFileManyTimesByFile: L165");
    }

    @Test
    public void shouldCleanDirectBuffer() {
        CleanerTestUtil.ReservedMemorySnapshot snapshot = CleanerTestUtil.test(IOTools::clean);
        assertTrue(snapshot.before <= snapshot.after, "shouldCleanDirectBuffer: reservedMemory before=" + snapshot.before + ", after=" + snapshot.after);
    }

    @Test
    public void createDirectoriesWithBrokenLink() throws IOException, IllegalStateException {
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
            fail("createDirectoriesWithBrokenLink: L192");
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(), "createDirectoriesWithBrokenLink: L194");
            assertTrue(ioe.getMessage().startsWith("Symbolic link from "), "createDirectoriesWithBrokenLink: L195");
            assertTrue(ioe.getMessage().endsWith("nowhere is broken"), "createDirectoriesWithBrokenLink: L196");
        } finally {
            Files.delete(link);
        }
    }

    @Test
    public void createDirectoriesReadOnly() throws IOException, IllegalStateException {
        Assumptions.assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only" + Time.uniqueId());
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        assertFalse(ro.toFile().canWrite(), "createDirectoriesReadOnly: L211");
        try {
            IOTools.createDirectories(Paths.get(ro.toString(), "subdir" + Time.uniqueId()));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(), "createDirectoriesReadOnly: L215");
            assertTrue(ioe.getMessage().startsWith("Cannot write to "), "createDirectoriesReadOnly: L216");
        } finally {
            if (!ro.toFile().setWritable(true))
                throw new IllegalStateException("Cannot make read-write");
            Files.delete(ro);

        }
    }

    @Test
    public void cannotTurnAfileIntoADirectory() throws IOException {
        Assumptions.assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path file = Paths.get(path, "test-file" + Time.uniqueId());
        File asFile = file.toFile();
        if (asFile.exists() && !asFile.delete())
            throw new IOException("Cannot delete pre-existing file " + asFile);
        asFile.deleteOnExit();
        assertTrue(asFile.createNewFile(), "cannotTurnAfileIntoADirectory: L235");
        try {
            IOTools.createDirectories(Paths.get(file.toString(), "subdir" + Time.uniqueId()));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass(), "cannotTurnAfileIntoADirectory: L239");
            assertTrue(ioe.getMessage().startsWith("Cannot create a directory with the same name as a file "), "cannotTurnAfileIntoADirectory: L240");
        }
    }

    @Test
    public void isDirectBuffer() {
        assertTrue(IOTools.isDirectBuffer(ByteBuffer.allocateDirect(1)), "isDirectBuffer: L246");
        assertFalse(IOTools.isDirectBuffer(ByteBuffer.allocate(1)), "isDirectBuffer: L247");
    }

    @Test
    public void addressFor() {
        assertNotEquals(0L, IOTools.addressFor(ByteBuffer.allocateDirect(1)));
    }

    @Test
    public void addressFor2() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        assertThrows(ClassCastException.class, () -> IOTools.addressFor(bb));
    }

    @Test
    public void normaliseIOStatus() {
        final int actual = IOTools.IOSTATUS_INTERRUPTED;
        assertEquals(-3, actual, "normaliseIOStatus: L264");

        assertEquals(-3, IOTools.normaliseIOStatus(-3), "normaliseIOStatus: L266");
    }

    @Test
    public void connectionClosed() throws IOException {
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
            fail("connectionClosed: L289");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            os.close();
        }
        try {
            s2.getOutputStream().write(bytes);
            fail("connectionClosed: L297");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        }
    }

    @Test
    public void connectionClosed2() throws IOException {
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
            fail("connectionClosed2: L320");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        }
        ss.close();
        try {
            for (int i = 0; i < 100; i++) {
                bytes.clear();
                sc.write(bytes);
            }
            fail("connectionClosed2: L330");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            sc.close();
        }
    }

    @Test
    public void connectionClosed3() throws IOException {
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
                assertTrue(write > 0, "connectionClosed3: L364");
            }
            fail("connectionClosed3: L366");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            s2.close();
            sc.close();
        }
    }

    @Test
    public void connectionClosed4() throws IOException {
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
                assertTrue(write > 0, "connectionClosed4: L405");
            }
            fail("connectionClosed4: L407");
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            s2.close();
            sc.close();
        }
    }
}
