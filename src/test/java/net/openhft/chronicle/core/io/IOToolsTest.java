package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.cleaner.impl.CleanerTestUtil;
import net.openhft.chronicle.core.util.Time;
import org.junit.Assume;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class IOToolsTest extends CoreTestCommon {

    @Test
    public void readFileManyTimesByPath() {
        final int iterations = 10_000;
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
        final int iterations = 10_000;
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
        ServerSocket ss = new ServerSocket(0);
        Socket s = new Socket("localhost", ss.getLocalPort());
        final OutputStream os = s.getOutputStream();
        Socket s2 = ss.accept();
        s2.close();
        ss.close();
        final byte[] bytes = new byte[512];
        try {
            for (int i = 0; i < 100; i++) {
                System.out.println(i);
                os.write(bytes);
            }
            fail();
        } catch (IOException ioe) {
            assertTrue(ioe.toString(), IOTools.isClosedException(ioe));
        } finally {
            os.close();
        }
    }

    @Test
    public void connectionClosed2() throws IOException {
        ServerSocket ss = new ServerSocket(0);
        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        Socket s2 = ss.accept();
        s2.close();
        ss.close();
        ByteBuffer bytes = ByteBuffer.allocateDirect(1024);
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
        ServerSocket ss = new ServerSocket(0);
        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
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
        });
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
}