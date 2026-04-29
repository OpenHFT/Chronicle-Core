/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class IOToolsTest extends CoreTestCommon {

    @Test
    void testIsClosedException() {
        Exception closedConnectionException = new IOException("Connection reset by peer");
        assertTrue(IOTools.isClosedException(closedConnectionException));

        Exception otherException = new IOException("Some other IO error");
        assertFalse(IOTools.isClosedException(otherException));
    }

    @Test
    void testWriteFile() throws IOException {
        String testFilename = "testFile.tmp";
        String testData = "Test Data";

        IOTools.writeFile(testFilename, testData.getBytes());

        Path path = Paths.get(testFilename);
        assertTrue(Files.exists(path));
        assertArrayEquals(testData.getBytes(), Files.readAllBytes(path));

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(path);
    }

    @Test
    void testTempName() {
        String filename = "test.txt";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename);
        assertTrue(tempFilename.startsWith("test"));
        assertTrue(tempFilename.endsWith(".txt"));
    }

    @Test
    void testClean() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);

        IOTools.clean(bb);
        assertTrue(true); // If we reach here, the test passes
    }

    @Test
    void testCreateDirectories() throws IOException {
        Path tempDir = Paths.get("tempDir");
        IOTools.createDirectories(tempDir);

        assertTrue(Files.isDirectory(tempDir));

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testIsDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertTrue(IOTools.isDirectBuffer(directBuffer));
        assertFalse(IOTools.isDirectBuffer(nonDirectBuffer));
    }

    @Test
    void testAddressFor() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        long address = IOTools.addressFor(directBuffer);

        assertNotEquals(0, address);
    }

    @Test
    void testDeleteDirWithFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        File tempFile = Files.createTempFile(tempDir, "test", ".tmp").toFile();

        assertTrue(tempFile.exists());
        assertTrue(IOTools.deleteDirWithFiles(tempDir.toFile()));

        assertFalse(tempFile.exists());
        assertFalse(tempDir.toFile().exists());
    }

    @Test
    void testReadAsBytes() throws IOException {
        String testData = "Test Data";
        ByteArrayInputStream bais = new ByteArrayInputStream(testData.getBytes());

        byte[] bytes = IOTools.readAsBytes(bais);

        assertArrayEquals(testData.getBytes(), bytes);
    }

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
                        throw Jvm.rethrow(ioe);
                    }
                });

        assertEquals(iterations, accumulator.get());
    }

    @Test
    void readFileManyTimesByFile() throws IOException {
        final int iterations = 3_000;
        final LongAccumulator accumulator = new LongAccumulator(Long::sum, 0);

        String file = OS.getTarget() + "/readFileManyTimes.txt";
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("Delete me\n".getBytes(StandardCharsets.UTF_8));
        }

        IntStream.range(0, iterations)
                .parallel()
                .forEach(i -> {
                    try {
                        IOTools.readFile(IOToolsTest.class, file);
                        accumulator.accumulate(1);
                    } catch (IOException ioe) {
                        throw Jvm.rethrow(ioe);
                    }
                });

        assertEquals(iterations, accumulator.get());
    }

    @Test
    void shouldCleanDirectBuffer() {
        CleanerTestUtil.test(IOTools::clean);
    }

    @Test
    void createDirectoriesWithBrokenLink() throws IOException, IllegalStateException {
        assumeTrue(OS.isLinux());

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
            fail();
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass());
            assertTrue(ioe.getMessage().startsWith("Symbolic link from "));
            assertTrue(ioe.getMessage().endsWith("nowhere is broken"));
        } finally {
            Files.delete(link);
        }
    }

    @Test
    void createDirectoriesReadOnly() throws IOException, IllegalStateException {
        assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only" + Time.uniqueId());
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        assertFalse(ro.toFile().canWrite());
        try {
            IOTools.createDirectories(Paths.get(ro.toString(), "subdir" + Time.uniqueId()));
            fail("Expected an IOException when trying to create a directory inside a read-only directory");
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass());
            assertTrue(ioe.getMessage().startsWith("Cannot write to "));
        } finally {
            if (!ro.toFile().setWritable(true))
                throw new IllegalStateException("Cannot make read-write");
            Files.delete(ro);
        }
    }

    @Test
    void cannotTurnAfileIntoADirectory() throws IOException {
        assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path file = Paths.get(path, "test-file" + Time.uniqueId());
        file.toFile().delete();
        file.toFile().deleteOnExit();
        assertTrue(file.toFile().createNewFile());
        try {
            IOTools.createDirectories(Paths.get(file.toString(), "subdir" + Time.uniqueId()));
            fail("Expected an IOException when trying to create a directory with the same name as a file");
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass());
            assertTrue(ioe.getMessage().startsWith("Cannot create a directory with the same name as a file "));
        }
    }

    @Test
    void isDirectBuffer() {
        assertTrue(IOTools.isDirectBuffer(ByteBuffer.allocateDirect(1)));
        assertFalse(IOTools.isDirectBuffer(ByteBuffer.allocate(1)));
    }

    @Test
    void addressFor() {
        assertNotEquals(0L, IOTools.addressFor(ByteBuffer.allocateDirect(1)));
    }

    @Test
    void addressFor2() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        try {
            IOTools.addressFor(bb);
            fail("Expected a ClassCastException when trying to get the address of a non-direct ByteBuffer");
        } catch (ClassCastException cce) {
            // expected
        }
    }

    @Test
    void normaliseIOStatus() {
        final int actual = IOTools.IOSTATUS_INTERRUPTED;
        assertEquals(-3, actual);

        assertEquals(-3, IOTools.normaliseIOStatus(-3));
    }

    @Test
    void connectionClosed() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            // Some CI environments disallow socket operations; skip in that case.
            assumeTrue(false, "Network not permitted in this environment");
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
//                System.out.println(i);
                os.write(bytes);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            os.close();
        }
        try {
            s2.getOutputStream().write(bytes);
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        }
    }

    @Test
    void connectionClosed2() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            assumeTrue(false, "Network not permitted in this environment");
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
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        }
        ss.close();
        try {
            for (int i = 0; i < 100; i++) {
//                System.out.println(i);
                bytes.clear();
                sc.write(bytes);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            sc.close();
        }
    }

    @Test
    void connectionClosed3() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            assumeTrue(false, "Network not permitted in this environment");
            return;
        }
        SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", ss.getLocalPort()));
        Socket s2 = ss.accept();
        ss.close();
        ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
        Thread t = new Thread(() -> {
            Jvm.pause(100);
            Closeable.closeQuietly(sc);
        }, "close~thread");
        t.start();
        try {
            for (int i = 0; i < 10000; i++) {
//                System.out.println(i);
                bytes.clear();
                final int write = sc.write(bytes);
                assertTrue(write > 0);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            s2.close();
            sc.close();
        }
    }

    @Test
    void connectionClosed4() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            assumeTrue(false, "Network not permitted in this environment");
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
            Closeable.closeQuietly(sc);
        }, "close~thread");
        t.setDaemon(true);
        t.start();
        try {
            for (int i = 0; i < 10000; i++) {
//                System.out.println(i);
                bytes.clear();
                final int write = sc.write(bytes);
                assertTrue(write > 0);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(IOTools.isClosedException(ioe), ioe.toString());
        } finally {
            s2.close();
            sc.close();
        }
    }

    // ---------------------------------------------------------------
    // IOTools.destroyProcess tests
    // ---------------------------------------------------------------

    @Test
    void destroyProcessReturnsEarlyWhenChildExitsInsideWaitWindow() {
        StubProcess process = new StubProcess(StubProcess.Mode.EXITED);

        IOTools.destroyProcess(process);

        // Child exited inside the first waitFor — no destroy needed.
        assertEquals(Arrays.asList("waitFor(1, SECONDS)"), process.events);
    }

    @Test
    void destroyProcessEscalatesToForciblyWhenChildIgnoresSigterm() {
        StubProcess process = new StubProcess(StubProcess.Mode.TIMED_OUT);

        IOTools.destroyProcess(process);

        // Child never exits: wait, destroy, wait again, then destroyForcibly.
        assertEquals(Arrays.asList(
                "waitFor(1, SECONDS)",
                "destroy",
                "waitFor(1, SECONDS)",
                "destroyForcibly"), process.events);
    }

    @Test
    void destroyProcessRestoresInterruptStatusAndStillDestroys() {
        StubProcess process = new StubProcess(StubProcess.Mode.INTERRUPTED);

        assertFalse(Thread.currentThread().isInterrupted(),
                "pre-condition: current thread is not already interrupted");
        try {
            IOTools.destroyProcess(process);

            // First waitFor was interrupted → finally closes pipes → destroy →
            // second waitFor (interrupt flag still set, throws immediately) →
            // escalate to destroyForcibly.
            assertEquals(Arrays.asList(
                    "waitFor(1, SECONDS)",
                    "destroy",
                    "waitFor(1, SECONDS)",
                    "destroyForcibly"), process.events);
            assertTrue(Thread.currentThread().isInterrupted(),
                    "interrupt flag must be restored on return");
        } finally {
            // Clear the interrupt flag so it does not leak into later tests on
            // this thread.
            Thread.interrupted();
        }
    }

    @Test
    void destroyProcessIsSafeToCallRepeatedly() {
        StubProcess process = new StubProcess(StubProcess.Mode.EXITED);

        IOTools.destroyProcess(process);
        IOTools.destroyProcess(process);

        assertEquals(Arrays.asList(
                "waitFor(1, SECONDS)",
                "waitFor(1, SECONDS)"), process.events);
    }

    @Test
    void destroyProcessClosesPipeStreams() {
        StubProcess process = new StubProcess(StubProcess.Mode.TIMED_OUT);

        IOTools.destroyProcess(process);

        assertTrue(process.outClosed.get(), "child stdin pipe must be closed");
        assertTrue(process.inClosed.get(),  "child stdout pipe must be closed");
        assertTrue(process.errClosed.get(), "child stderr pipe must be closed");
    }

    @Test
    void destroyProcessTerminatesRealRunningChild() throws InterruptedException {
        final Process child = JavaProcessBuilder.create(SleepForever.class).start();
        try {
            // Give the freshly-spawned JVM a brief window to reach main() so
            // the test does not race against class init on a busy CI box.
            assertFalse(child.waitFor(50, TimeUnit.MILLISECONDS),
                    "sanity: child should still be alive before destroyProcess");

            IOTools.destroyProcess(child);

            // destroyProcess waits up to 1s for SIGTERM and another 1s after
            // destroy(); allow extra slack on slow CI.
            assertTrue(child.waitFor(5, TimeUnit.SECONDS),
                    "child must exit within 5s of destroyProcess");
        } finally {
            if (child.isAlive())
                child.destroyForcibly();
        }
    }

    /**
     * Entry point spawned by {@link #destroyProcessTerminatesRealRunningChild}.
     * Blocks indefinitely so the child only exits when terminated by its
     * parent. {@link Jvm#pause(long)} with {@link Long#MAX_VALUE} avoids any
     * dependency on the parent's stdin handling (closing/inheriting/piping)
     * which would otherwise let the child exit prematurely on EOF.
     */
    public static final class SleepForever {
        public static void main(String[] args) {
            Jvm.pause(Long.MAX_VALUE);
        }
    }

    /**
     * Recording fixture for {@link IOTools#destroyProcess(Process)} tests.
     *
     * <p>Captures every {@code waitFor}/{@code destroy}/{@code destroyForcibly}
     * call (with arguments) into an ordered {@link #events} list and tracks
     * whether each pipe stream was closed. The three {@link Mode}s cover the
     * paths the helper must handle: a child that refuses to exit inside the
     * wait window, a child that has already exited, and a wait that is
     * interrupted before it can complete.</p>
     */
    private static final class StubProcess extends Process {

        enum Mode {
            /** {@code waitFor} returns {@code false} (wait window elapsed). */
            TIMED_OUT,
            /** {@code waitFor} returns {@code true} (child exited naturally). */
            EXITED,
            /** {@code waitFor} throws {@link InterruptedException} on first call. */
            INTERRUPTED
        }

        private final Mode mode;
        private final List<String> events = new ArrayList<>();
        final AtomicBoolean outClosed = new AtomicBoolean();
        final AtomicBoolean inClosed  = new AtomicBoolean();
        final AtomicBoolean errClosed = new AtomicBoolean();
        private int waitForCalls;

        private StubProcess(Mode mode) {
            this.mode = mode;
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() {
                    outClosed.set(true);
                }
            };
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[0]) {
                @Override
                public void close() {
                    inClosed.set(true);
                }
            };
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]) {
                @Override
                public void close() {
                    errClosed.set(true);
                }
            };
        }

        @Override
        public int waitFor() {
            // Recorded rather than thrown: future refactors may use either
            // overload, and locking down the exact API would cause
            // unnecessary test churn.
            events.add("waitFor()");
            return 0;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            events.add("waitFor(" + timeout + ", " + unit.name() + ")");
            waitForCalls++;
            switch (mode) {
                case EXITED:
                    return true;
                case INTERRUPTED:
                    // Only the first call throws; later calls behave as
                    // TIMED_OUT so the helper can progress to destroyForcibly.
                    if (waitForCalls == 1)
                        throw new InterruptedException("interrupted for test");
                    return false;
                case TIMED_OUT:
                default:
                    return false;
            }
        }

        @Override
        public int exitValue() {
            throw new IllegalThreadStateException("process still running");
        }

        @Override
        public void destroy() {
            events.add("destroy");
        }

        @Override
        public Process destroyForcibly() {
            events.add("destroyForcibly");
            return this;
        }
    }
}
