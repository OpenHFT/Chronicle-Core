/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.watcher;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.util.Time;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

public class FileSystemWatcherTest {
    static String base = OS.getTarget() + "/FileSystemWatcherTest-" + Time.uniqueId();

    @Before
    public void setup() throws IOException {
        tearDown();
        Files.createDirectories(Paths.get(base));
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(base);
    }

    @Test
    public void bootstrapAndUpdate() throws IOException {
        assumeFalse(Jvm.isArm());

        /*
        This test fails on Windows and may also fail on MmcOS.

        The issue appears to be that the NIO file-watching service is very platform dependent.

        Eg. Linux does not produce events when a directory-entry is updated, WIndows does.
        Eg. Windows does not produce events when a file is opened for write - Linux does.

        This makes the results of this particular test heavily dependent on the platform.

        A practical solution would be to modify the test and framework so that only major events are generated (file created, modified etc) and filter out the unhelpful events (if possible ;-)

        In the meantime, this test has been ignored for Windows.
        */
        Assume.assumeTrue(OS.isLinux());

        SortedMap<String, String> events = new ConcurrentSkipListMap<>();

        WatcherListener listener = new WatcherListener() {
            @Override
            public void onExists(String base, String filename, Boolean modified) {
                events.put(filename, "modified: " + modified);
            }

            @Override
            public void onRemoved(String base, String filename) {
                events.put(filename, "removed: true");
            }
        };
        assertTrue(new File(base + "/dir1").mkdir());
        assertTrue(new File(base + "/dir2").mkdir());
        assertTrue(new File(base + "/dir1/file11").createNewFile());
        assertTrue(new File(base + "/dir1/file12").createNewFile());
        assertTrue(new File(base + "/dir2/file20").createNewFile());

        FileSystemWatcher watcher = new FileSystemWatcher();
        watcher.addPath(base);
        watcher.start();
        watcher.addListener(listener);
        retryAssertEquals("dir1=modified: null\n" +
                "dir1/file11=modified: null\n" +
                "dir1/file12=modified: null\n" +
                "dir2=modified: null\n" +
                "dir2/file20=modified: null", events);
        try (FileWriter fw = new FileWriter(base + "/dir1/file11")) {
        }
        assertTrue(new File(base + "/dir2/file20").delete());
        assertTrue(new File(base + "/dir2/file21").createNewFile());
        assertTrue(new File(base + "/dir3/dir30").mkdirs());
        assertTrue(new File(base + "/dir3/dir30/file301").createNewFile());

        retryAssertEquals(
                "dir1=modified: null\n" +
                        "dir1/file11=modified: true\n" +
                        "dir1/file12=modified: null\n" +
                        "dir2=modified: null\n" +
                        "dir2/file20=removed: true\n" +
                        "dir2/file21=modified: false\n" +
                        "dir3=modified: false\n" +
                        "dir3/dir30=modified: null\n" +
                        "dir3/dir30/file301=modified: null", events);

        IOTools.deleteDirWithFiles(base + "/dir2", 2);

        retryAssertEquals(
                "dir1=modified: null\n" +
                        "dir1/file11=modified: true\n" +
                        "dir1/file12=modified: null\n" +
                        "dir2=removed: true\n" +
                        "dir2/file20=removed: true\n" +
                        "dir2/file21=removed: true\n" +
                        "dir3=modified: false\n" +
                        "dir3/dir30=modified: null\n" +
                        "dir3/dir30/file301=modified: null", events);

        watcher.stop();
    }

    private void retryAssertEquals(String expected, SortedMap<String, String> events) {
        for (int i = Jvm.isDebug() ? 500 : 100; ; i--) {
            try {
                Jvm.pause(20);
                assertEquals(expected,
                        events.entrySet().stream()
                                .map(Map.Entry::toString)
                                .collect(Collectors.joining("\n")));
                break;
            } catch (AssertionError ae) {
                if (i <= 0)
                    throw ae;
            }
        }
    }
}