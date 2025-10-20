package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CleaningRandomAccessFileTest extends CoreTestCommon {

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

    @SuppressWarnings("removal")
    @Test
    public void testFinalizeAndCleanup() throws IOException {
        File tempFile = File.createTempFile("test", "raf");

        //noinspection resource
        new CleaningRandomAccessFile(tempFile, "rw");

        System.gc();
        System.runFinalization();

        if (!tempFile.delete()) {
            Jvm.pause(100);
            Files.delete(tempFile.toPath());
        }
    }

    @Test
    public void resourceLeak() throws IOException {
        File tempDir = IOTools.createTempFile("resourceLeak");
        //noinspection ResultOfMethodCallIgnored
        tempDir.mkdir();
        int repeat = Jvm.isArm() ? 6 : OS.isWindows() ? 25 : 50;
        for (int j = 0; j < repeat; j++) {
            int files = getFDs();
            if (files > 0) {
//                System.out.println("File descriptors " + files);
                assertEquals("j: " + j, 200, files, 200);
            }
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            for (int i = 0; i < 200; i++) {
                @SuppressWarnings("resource")
                RandomAccessFile file = new CleaningRandomAccessFile(tempDir + "/file" + i, "rw");
                bb.clear();
                //noinspection ResultOfMethodCallIgnored
                file.getChannel().write(bb);
            }
            long start = System.currentTimeMillis();
            System.gc();
            for (int i = 0; i < 40; i++) {
                Jvm.pause(20);
                if (getFDs() < 200) {
                    double time = (System.currentTimeMillis() - start) / 1e3;
                    if (time > 0.1)
                        System.out.println("resourceLeak() - Took " + time + " seconds.");
                    break;
                }
            }
        }
        IOTools.deleteDirWithFiles(tempDir);
    }

    static int getFDs() {
        if (!OS.isLinux())
            return -1;
        //noinspection DataFlowIssue
        return new File("/proc/self/fd").list().length;
    }
}
