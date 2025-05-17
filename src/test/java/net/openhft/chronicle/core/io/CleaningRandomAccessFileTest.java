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

    @Test
    public void testFileDescriptorClosedAfterGC() throws Exception {
        File tempFile = File.createTempFile("testFD", "raf");

        CleaningRandomAccessFile raf = new CleaningRandomAccessFile(tempFile, "rw");

        // access the protected getFD method via reflection
        java.lang.reflect.Method getFD = java.io.RandomAccessFile.class.getDeclaredMethod("getFD");
        getFD.setAccessible(true);
        FileDescriptor fd = (FileDescriptor) getFD.invoke(raf);

        assertTrue(fd.valid());

        // drop the reference without closing the file
        raf = null;

        // force GC and finalization
        for (int i = 0; i < 10 && fd.valid(); i++) {
            System.gc();
            System.runFinalization();
            Thread.sleep(50);
        }

        assertFalse(fd.valid(), "File descriptor should be closed after GC");

        assertTrue(tempFile.delete());
    }
}
