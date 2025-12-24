/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Linux-oriented coverage for pid_max parsing, fallback behaviour, and directory spaceUsed reporting.
 */
@SuppressWarnings("deprecation")
class OSLinuxTest extends CoreTestCommon {

    @Test
    void pidMaxParsesValueAndRoundsUpPowerOfTwo() throws IOException {
        File tmp = File.createTempFile("pid_max", ".txt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "12345\n".getBytes(StandardCharsets.UTF_8));

        long pidMax = OS.testGetPidMax(tmp.getAbsolutePath());
        assertEquals(16384L, pidMax, "next power of two above 12345 is 16384");
    }

    @Test
    void pidMaxFallsBackWhenUnreadable() {
        File missing = new File("no-such-pid-max-" + System.nanoTime());
        long pidMax = OS.testGetPidMax(missing.getAbsolutePath());
        assertEquals(1L << 16, pidMax, "fallback for unreadable pid_max");
    }

    @Test
    void spaceUsedOnDirectoryIsNonNegative() throws IOException {
        File dir = Files.createTempDirectory("os-space-dir").toFile();
        dir.deleteOnExit();
        long spaceUsed = OS.spaceUsed(dir.getAbsolutePath());
        assertTrue(spaceUsed >= 0L, "spaceUsed should be non-negative for directories, was " + spaceUsed);
    }
}
