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
import org.junit.Assume;
import org.junit.Test;

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
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class IOToolsTest extends CoreTestCommon {

    @Test
    public void testIsClosedException() {
        Exception closedConnectionException = new IOException("Connection reset by peer");
        assertTrue(IOTools.isClosedException(closedConnectionException));

        Exception otherException = new IOException("Some other IO error");
        assertFalse(IOTools.isClosedException(otherException));
    }

    @Test
    public void testWriteFile() throws IOException {
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
    public void testTempName() {
        String filename = "test.txt";
        String tempFilename = IOTools.tempName(filename);

        assertNotEquals(filename, tempFilename);
        assertTrue(tempFilename.startsWith("test"));
        assertTrue(tempFilename.endsWith(".txt"));
    }

    @Test
    public void testClean() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);

        IOTools.clean(bb);
        assertTrue(true); // If we reach here, the test passes
    }

    @Test
    public void testCreateDirectories() throws IOException {
        Path tempDir = Paths.get("tempDir");
        IOTools.createDirectories(tempDir);

        assertTrue(Files.isDirectory(tempDir));

        BackgroundResourceReleaser.releasePendingResources();
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testIsDirectBuffer() {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(1024);

        assertTrue(IOTools.isDirectBuffer(directBuffer));
        assertFalse(IOTools.isDirectBuffer(nonDirectBuffer));
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

        assertTrue(tempFile.exists());
        assertTrue(IOTools.deleteDirWithFiles(tempDir.toFile()));

        assertFalse(tempFile.exists());
        assertFalse(tempDir.toFile().exists());
    }

    @Test
    public void testReadAsBytes() throws IOException {
        String testData = "Test Data";
        ByteArrayInputStream bais = new ByteArrayInputStream(testData.getBytes());

        byte[] bytes = IOTools.readAsBytes(bais);

        assertArrayEquals(testData.getBytes(), bytes);
    }

    @Test
    public void destroyProcessWaitsThenDestroysWhenTimeoutElapses() {
        StubProcess process = new StubProcess(StubProcess.Mode.TIMED_OUT);

        IOTools.destroyProcess(process);

        assertEventsAre(process, "waitFor(1, SECONDS)", "destroy");
    }

    @Test
    public void destroyProcessStillDestroysAfterNaturalExit() {
        // destroy() is a no-op on an already-exited Process per JDK semantics;
        // this test pins the current "always call destroy()" behaviour so any
        // future refactor that skips destroy() after a natural exit is deliberate.
        StubProcess process = new StubProcess(StubProcess.Mode.EXITED);

        IOTools.destroyProcess(process);

        assertEventsAre(process, "waitFor(1, SECONDS)", "destroy");
    }

    @Test
    public void destroyProcessRestoresInterruptStatusAndStillDestroys() {
        StubProcess process = new StubProcess(StubProcess.Mode.INTERRUPTED);

        assertFalse("pre-condition: current thread is not already interrupted",
                Thread.currentThread().isInterrupted());
        try {
            IOTools.destroyProcess(process);

            assertEventsAre(process, "waitFor(1, SECONDS)", "destroy");
            assertTrue("interrupt flag must be restored on return",
                    Thread.currentThread().isInterrupted());
        } finally {
            // Clear the interrupt flag so it does not leak into later tests on
            // this thread.
            Thread.interrupted();
        }
    }

    @Test
    public void destroyProcessIsSafeToCallRepeatedly() {
        StubProcess process = new StubProcess(StubProcess.Mode.EXITED);

        IOTools.destroyProcess(process);
        IOTools.destroyProcess(process);

        assertEventsAre(process,
                "waitFor(1, SECONDS)", "destroy",
                "waitFor(1, SECONDS)", "destroy");
    }

    private static void assertEventsAre(StubProcess process, String... expected) {
        assertEquals(Arrays.asList(expected), process.events);
    }

    @Test
    public void destroyProcessTerminatesRealRunningChild() throws InterruptedException {
        final Process child = JavaProcessBuilder.create(SleepForever.class).start();
        try {
            assertTrue("sanity: child should still be alive before destroyProcess",
                    child.isAlive());

            IOTools.destroyProcess(child);

            // destroyProcess sends SIGTERM and returns without waiting further;
            // give the child a generous window to actually exit.
            assertTrue("child must exit within 5s of destroyProcess",
                    child.waitFor(5, TimeUnit.SECONDS));
        } finally {
            if (child.isAlive())
                child.destroyForcibly();
        }
    }

    /**
     * Entry point spawned by {@link #destroyProcessTerminatesRealRunningChild}.
     * Blocks on stdin so the child only exits when terminated by its parent.
     */
    public static final class SleepForever {
        public static void main(String[] args) throws IOException {
            int ignored = System.in.read();
        }
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

        assertEquals(iterations, accumulator.get());
    }

    @Test
    public void readFileManyTimesByFile() throws IOException {
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
                        Jvm.rethrow(ioe);
                    }
                });

        assertEquals(iterations, accumulator.get());
    }

    @Test
    public void shouldCleanDirectBuffer() {
        CleanerTestUtil.test(IOTools::clean);
    }

    @Test
    public void createDirectoriesWithBrokenLink() throws IOException, IllegalStateException {
        Assume.assumeTrue(OS.isLinux());

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
    public void createDirectoriesReadOnly() throws IOException, IllegalStateException {
        Assume.assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only" + Time.uniqueId());
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        assertFalse(ro.toFile().canWrite());
        try {
            IOTools.createDirectories(Paths.get(ro.toString(), "subdir" + Time.uniqueId()));
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
    public void cannotTurnAfileIntoADirectory() throws IOException {
        Assume.assumeTrue(OS.isLinux());

        String path = OS.getTarget();
        Path file = Paths.get(path, "test-file" + Time.uniqueId());
        file.toFile().delete();
        file.toFile().deleteOnExit();
        assertTrue(file.toFile().createNewFile());
        try {
            IOTools.createDirectories(Paths.get(file.toString(), "subdir" + Time.uniqueId()));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass());
            assertTrue(ioe.getMessage().startsWith("Cannot create a directory with the same name as a file "));
        }
    }

    @Test
    public void isDirectBuffer() {
        assertTrue(IOTools.isDirectBuffer(ByteBuffer.allocateDirect(1)));
        assertFalse(IOTools.isDirectBuffer(ByteBuffer.allocate(1)));
    }

    @Test
    public void addressFor() {
        assertNotEquals(0L, IOTools.addressFor(ByteBuffer.allocateDirect(1)));
    }

    @Test
    public void addressFor2() {
        final ByteBuffer bb = ByteBuffer.allocate(1);
        try {
            IOTools.addressFor(bb);
            fail();
        } catch (ClassCastException cce) {
            // expected
        }
    }

    @Test
    public void normaliseIOStatus() {
        final int actual = IOTools.IOSTATUS_INTERRUPTED;
        assertEquals(-3, actual);

        assertEquals(-3, IOTools.normaliseIOStatus(-3));
    }

    @Test
    public void connectionClosed() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            // Some CI environments disallow socket operations; skip in that case.
            Assume.assumeTrue("Network not permitted in this environment", false);
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
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
        } finally {
            os.close();
        }
        try {
            s2.getOutputStream().write(bytes);
            fail();
        } catch (IOException ioe) {
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
        }
    }

    @Test
    public void connectionClosed2() throws IOException {
        ServerSocket ss;
        try {
            ss = new ServerSocket(0);
        } catch (IOException ioe) {
            Assume.assumeTrue("Network not permitted in this environment", false);
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
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
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
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
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
            Assume.assumeTrue("Network not permitted in this environment", false);
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
        },  "close~thread");
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
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
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
            Assume.assumeTrue("Network not permitted in this environment", false);
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
//                System.out.println(i);
                bytes.clear();
                final int write = sc.write(bytes);
                assertTrue(write > 0);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
        } finally {
            s2.close();
            sc.close();
        }
    }

    /**
     * Recording fixture for {@link IOTools#destroyProcess(Process)} tests.
     *
     * <p>Captures every {@code waitFor}/{@code destroy} call (with arguments)
     * into an ordered {@link #events} list so tests can assert both which
     * methods ran and in what order. The three {@link Mode}s cover the three
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
            /** {@code waitFor} throws {@link InterruptedException}. */
            INTERRUPTED
        }

        private final Mode mode;
        private final List<String> events = new ArrayList<>();

        private StubProcess(Mode mode) {
            this.mode = mode;
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public int waitFor() {
            throw new UnsupportedOperationException(
                    "destroyProcess must use the timed waitFor overload");
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            events.add("waitFor(" + timeout + ", " + unit.name() + ")");
            switch (mode) {
                case EXITED:
                    return true;
                case INTERRUPTED:
                    throw new InterruptedException("interrupted for test");
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
    }
}
