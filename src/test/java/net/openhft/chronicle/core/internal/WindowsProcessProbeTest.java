/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 4})
    void longCsvFieldsRemainParseable(int field) {
        String[] fields = {"java.exe", "42", "Console", "1", "1,000 K"};
        char[] chars = new char[64 * 1024];
        Arrays.fill(chars, 'x');
        fields[field] = new String(chars);
        String row = "\"" + String.join("\",\"", fields) + "\"\r\n";
        assertEquals(ALIVE, WindowsProcessProbe.parse(42, row));
        assertEquals(DEAD, WindowsProcessProbe.parse(2, row));
    }

    @Test
    void malformedLongCsvFieldIsUnknown() {
        char[] chars = new char[64 * 1024];
        Arrays.fill(chars, 'x');
        assertEquals(UNKNOWN, WindowsProcessProbe.parse(2, "\"" + new String(chars)));
    }

    @Test
    void escapedQuotesPreservePidMatching() {
        String row = "\"image \"\"142\"\".exe\",\"42\",\"a,\"\"session\"\"\",\"1\",\"1,000 K\"\r\n";
        assertEquals(ALIVE, WindowsProcessProbe.parse(42, row));
        assertEquals(DEAD, WindowsProcessProbe.parse(142, row));
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
        StubProcess process = new StubProcess("", 0, true) {
            @Override
            public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
                Thread.currentThread().interrupt();
                return super.waitFor(timeout, unit);
            }
        };
        try {
            assertFalse(Thread.currentThread().isInterrupted());
            assertEquals(UNKNOWN, run(process, 42));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(process.destroyed);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void interruptedCompletedProbeCannotEstablishDeath() {
        StubProcess process = new StubProcess(ROWS, 0, false);
        Thread.currentThread().interrupt();
        try {
            assertEquals(UNKNOWN, run(process, 2));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(process.destroyed);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void interruptionDuringReadCannotEstablishDeath() {
        InputStream input = new ByteArrayInputStream(ROWS.getBytes(StandardCharsets.US_ASCII)) {
            @Override
            public synchronized int read(byte[] buffer, int offset, int length) {
                int read = super.read(buffer, offset, length);
                Thread.currentThread().interrupt();
                return read;
            }
        };
        StubProcess process = new StubProcess(input, 0, false);
        try {
            assertEquals(UNKNOWN, run(process, 2));
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

    private static class StubProcess extends Process {
        private final InputStream input;
        private final int status;
        private final boolean running;
        private boolean destroyed;

        StubProcess(String output, int status, boolean running) {
            this(new ByteArrayInputStream(output.getBytes(StandardCharsets.US_ASCII)), status, running);
        }

        StubProcess(InputStream input, int status, boolean running) {
            this.input = input;
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
