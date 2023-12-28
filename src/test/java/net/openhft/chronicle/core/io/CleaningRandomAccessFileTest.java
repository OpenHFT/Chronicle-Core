package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class CleaningRandomAccessFileTest {

    @Test
    public void testOpenAndClose() throws IOException {
        File tempFile = File.createTempFile("test", "raf");
        CleaningRandomAccessFile raf = new CleaningRandomAccessFile(tempFile, "rw");

        // Write and read to verify file is open
        raf.writeUTF("test");
        raf.seek(0);
        assertEquals("test", raf.readUTF());

        raf.close();

        assertThrows(IOException.class, () -> raf.writeUTF("should fail"));

        assertTrue(tempFile.delete());
    }

    @Test
    public void testFinalizeAndCleanup() throws IOException {
        File tempFile = File.createTempFile("test", "raf");

        new CleaningRandomAccessFile(tempFile, "rw");

        System.gc();
        System.runFinalization();

        assertTrue(tempFile.delete());
    }
}
