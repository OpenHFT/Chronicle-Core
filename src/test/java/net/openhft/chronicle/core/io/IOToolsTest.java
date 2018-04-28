package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class IOToolsTest {
    @Test
    public void shouldCleanDirectBuffer() throws Exception {
        IOTools.clean(ByteBuffer.allocateDirect(64));
    }

    @Test
    public void createDirectoriesWithBrokenLink() throws IOException {
        if (!OS.isLinux()) return;

        String path = OS.getTarget();
        Path link = Paths.get(path, "link2nowhere");
        Path nowhere = Paths.get(path, "nowhere");
        if (Files.isSymbolicLink(link)) {
            Files.delete(link);
            if (Files.isSymbolicLink(link)) {
                throw new IllegalStateException("Still exists");
            }
        }
        Files.createSymbolicLink(link, nowhere);

        try {
            IOTools.createDirectories(Paths.get(link.toString(), "subdir"));
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
    public void createDirectoriesReadOnly() throws IOException {
        if (!OS.isLinux()) return;

        String path = OS.getTarget();
        Path ro = Paths.get(path, "read-only");
        IOTools.createDirectories(ro);
        if (!ro.toFile().setWritable(false))
            throw new IllegalStateException("Cannot make read-only");
        assertFalse(ro.toFile().canWrite());
        try {
            IOTools.createDirectories(Paths.get(ro.toString(), "subdir"));
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
        if (!OS.isLinux()) return;

        String path = OS.getTarget();
        Path file = Paths.get(path, "test-file");
        file.toFile().delete();
        file.toFile().deleteOnExit();
        assertTrue(file.toFile().createNewFile());
        try {
            IOTools.createDirectories(Paths.get(file.toString(), "subdir"));
        } catch (IOException ioe) {
            assertSame(IOException.class, ioe.getClass());
            assertTrue(ioe.getMessage().startsWith("Cannot create a directory with the same name as a file "));
        }
    }

}