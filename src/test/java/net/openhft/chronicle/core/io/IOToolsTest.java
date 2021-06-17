package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import org.junit.Assume;
import org.junit.Test;
import sun.nio.ch.IOStatus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class IOToolsTest extends CoreTestCommon {

    @Test
    public void readFileManyTimesByPath() {
        IntStream.range(0, 10000)
                .parallel()
                .forEach(i -> {
                    try {
                        IOTools.readFile("readFileManyTimes.txt");
                    } catch (IOException ioe) {
                        Jvm.rethrow(ioe);
                    }
                });
    }

    @Test
    public void readFileManyTimesByFile() throws IOException {
        String file = OS.getTarget() + "/readFileManyTimes.txt";
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("Delete me\n".getBytes(StandardCharsets.UTF_8));
        }
        IntStream.range(0, 10000)
                .parallel()
                .forEach(i -> {
                    try {
                        IOTools.readFile(file);
                    } catch (IOException ioe) {
                        Jvm.rethrow(ioe);
                    }
                });
    }

    @Test
    public void shouldCleanDirectBuffer() {
        IOTools.clean(ByteBuffer.allocateDirect(64));
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
        assertTrue(IOTools.addressFor(ByteBuffer.allocateDirect(1)) != 0L);
        try {
            IOTools.addressFor(ByteBuffer.allocate(1));
            fail();
        } catch (ClassCastException cce) {
            // expected
        }
    }

    @Test
    public void normaliseIOStatus() {
        assertEquals(-3, IOTools.IOSTATUS_INTERRUPTED);

        assertEquals(-3, IOStatus.normalize(-3));
    }
}