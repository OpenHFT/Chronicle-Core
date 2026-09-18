/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static net.openhft.chronicle.core.internal.WindowsProcessProbe.Result.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WindowsProcessProbeTest {
    private static final String ROWS = "\"a 42 b.exe\",\"142\",\"Console\",\"1\",\"1,000 K\"\r\n"
            + "\"java.exe\",\"42\",\"Console\",\"1\",\"2,000 K\"\r\n";

    @Test
    void recognisesExactPidAndConfirmedAbsence() {
        assertEquals(ALIVE, WindowsProcessProbe.parse(42, ROWS));
        assertEquals(DEAD, WindowsProcessProbe.parse(2, ROWS));
        assertEquals(DEAD, WindowsProcessProbe.parse(1000, ROWS));
    }

    @Test
    void malformedOrEmptyEnumerationIsUnknown() {
        for (String output : new String[]{"", "Access is denied", "INFO: No tasks are running", ROWS + "truncated"})
            assertEquals(UNKNOWN, WindowsProcessProbe.parse(2, output), output);
    }

    @Test
    void failedStartAndDenialAreUnknown() {
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                () -> { throw new IOException("could not start"); }));
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                () -> { throw new SecurityException("denied"); }));
    }

    @Test
    void nonzeroExitCannotEstablishDeathAndProbeIsCleaned() {
        StubProcess process = new StubProcess(ROWS, 1, false);
        assertEquals(UNKNOWN, run(process, 2));
        assertTrue(process.destroyed);
    }

    @Test
    void successfulEnumerationEstablishesDeathAndProbeIsCleaned() {
        StubProcess process = new StubProcess(ROWS, 0, false);
        assertEquals(DEAD, run(process, 2));
        assertTrue(process.destroyed);
    }

    @Test
    void stalledProbeIsBoundedAndDestroyed() {
        StubProcess process = new StubProcess("", 0, true);
        long start = System.nanoTime();
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.MILLISECONDS.toNanos(20), () -> process));
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(2));
        assertTrue(process.destroyed);
    }

    @Test
    void interruptedProbePreservesInterruptAndDestroysChild() {
        StubProcess process = new StubProcess("", 0, true);
        Thread.currentThread().interrupt();
        try {
            assertEquals(UNKNOWN, run(process, 42));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(process.destroyed);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void oversizedOutputIsUnknown() {
        char[] chars = new char[1024 * 1024 + 1];
        StubProcess process = new StubProcess(new String(chars), 0, false);
        assertEquals(UNKNOWN, run(process, 42));
        assertTrue(process.destroyed);
    }

    @Test
    void realWindowsQueryFindsThisJvm() {
        assumeTrue(OS.isWindows());
        assertTrue(Jvm.isProcessAlive(OS.getProcessId()));
        assertEquals(ALIVE, WindowsProcessProbe.query(OS.getProcessId()));
        assertEquals(DEAD, WindowsProcessProbe.query(Long.MAX_VALUE));
    }

    private static WindowsProcessProbe.Result run(StubProcess process, long pid) {
        return WindowsProcessProbe.query(pid, TimeUnit.SECONDS.toNanos(1), () -> process);
    }

    private static final class StubProcess extends Process {
        private final InputStream input;
        private final int status;
        private final boolean running;
        private boolean destroyed;

        StubProcess(String output, int status, boolean running) {
            this.input = new ByteArrayInputStream(output.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            this.status = status;
            this.running = running;
        }

        @Override public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
        @Override public InputStream getInputStream() { return input; }
        @Override public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
        @Override public int waitFor() { throw new AssertionError("Unbounded wait"); }
        @Override public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            unit.sleep(timeout);
            return !running;
        }
        @Override public int exitValue() { return status; }
        @Override public boolean isAlive() { return running && !destroyed; }
        @Override public void destroy() { destroyed = true; }
        @Override public Process destroyForcibly() { destroyed = true; return this; }
    }
}
