/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Linux-oriented coverage: pid_max parsing and spaceUsed fallbacks.
 */
public class OSLinuxTest extends CoreTestCommon {

    @Test
    public void pidMaxParsesValueAndRoundsUpPowerOfTwo() throws IOException {
        File tmp = File.createTempFile("pid_max", ".txt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "12345\n".getBytes(StandardCharsets.UTF_8));

        long pidMax = OS.testGetPidMax(tmp.getAbsolutePath());
        assertEquals("next power of two above 12345 is 16384", 16384L, pidMax);
    }

    @Test
    public void pidMaxFallsBackWhenUnreadable() {
        File missing = new File("no-such-pid-max-" + System.nanoTime());
        long pidMax = OS.testGetPidMax(missing.getAbsolutePath());
        assertEquals("fallback for unreadable pid_max", 1L << 16, pidMax);
    }

    @Test
    public void spaceUsedOnDirectoryIsNonNegative() throws IOException {
        File dir = Files.createTempDirectory("os-space-dir").toFile();
        dir.deleteOnExit();
        assertTrue(OS.spaceUsed(dir.getAbsolutePath()) >= 0L);
    }
}
