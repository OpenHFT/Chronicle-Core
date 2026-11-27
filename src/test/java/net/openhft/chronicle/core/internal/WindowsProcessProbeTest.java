/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.ThreadLocalisedExceptionHandler;
import net.openhft.chronicle.core.test.RecordingExceptionHandlerStub;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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
        assertEquals(UNKNOWN, WindowsProcessProbe.parse(42, ROWS + "truncated"));
    }

    @Test
    void blankRowsAndWhitespaceDoNotHideAProcess() {
        String output = " \t\r\n" + ROWS.replace("\r\n", "  \r\n\t") + "\n";
        assertEquals(ALIVE, WindowsProcessProbe.parse(42, output));
        assertEquals(DEAD, WindowsProcessProbe.parse(2, output));
        assertEquals(UNKNOWN, WindowsProcessProbe.parse(2, " \t\r\n\n"));
    }

    @Test
    void overflowingPidCannotEstablishDeath() {
        assertEquals(UNKNOWN, WindowsProcessProbe.parse(2,
                ROWS.replace("\"142\"", "\"9223372036854775808\"")));
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
        assertCleaned(process);
    }

    @Test
    void successfulEnumerationEstablishesDeathAndProbeIsCleaned() {
        StubProcess process = new StubProcess(ROWS, 0, false);
        assertEquals(DEAD, run(process, 2));
        assertCleaned(process);
    }

    @Test
    void stalledProbeIsBoundedAndDestroyed() {
        StubProcess process = new StubProcess("", 0, true);
        long start = System.nanoTime();
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.MILLISECONDS.toNanos(20), () -> process));
        assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(2));
        assertCleaned(process);
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
            assertCleaned(process);
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
            assertCleaned(process);
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
            assertCleaned(process);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void oversizedOutputIsUnknown() {
        char[] chars = new char[1024 * 1024 + 1];
        StubProcess process = new StubProcess(new String(chars), 0, false);
        assertEquals(UNKNOWN, run(process, 42));
        assertCleaned(process);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void completeOutputHonoursTheExactLimit(int excess) {
        String suffix = "\",\"42\",\"Console\",\"1\",\"1 K\"\r\n";
        char[] image = new char[1024 * 1024 - 1 - suffix.length() + excess];
        Arrays.fill(image, 'x');
        StubProcess process = new StubProcess("\"" + new String(image) + suffix, 0, false);
        assertEquals(excess == 0 ? ALIVE : UNKNOWN, run(process, 42));
        assertCleaned(process);
    }

    @Test
    void endOfStreamAfterAvailabilityDoesNotCorruptCollectedOutput() {
        InputStream input = new ByteArrayInputStream(ROWS.getBytes(StandardCharsets.US_ASCII)) {
            private boolean reportedEnd;

            @Override
            public synchronized int available() {
                int available = super.available();
                if (available == 0 && !reportedEnd) {
                    reportedEnd = true;
                    return 1;
                }
                return available;
            }
        };
        StubProcess process = new StubProcess(input, 0, false);
        assertEquals(ALIVE, run(process, 42));
        assertCleaned(process);
    }

    @Test
    void waitsForOutputWithoutBusySpinningOrLongUninterruptiblePolls() {
        boolean[] waited = {false};
        InputStream input = new ByteArrayInputStream(ROWS.getBytes(StandardCharsets.US_ASCII)) {
            @Override
            public synchronized int available() {
                return waited[0] ? super.available() : 0;
            }
        };
        StubProcess process = new StubProcess(input, 0, true) {
            @Override
            public boolean waitFor(long timeout, TimeUnit unit) {
                assertEquals(TimeUnit.NANOSECONDS, unit);
                assertTrue(timeout > 0 && timeout <= TimeUnit.MILLISECONDS.toNanos(10));
                waited[0] = true;
                return true;
            }

            @Override
            public boolean isAlive() {
                return !waited[0];
            }
        };
        assertEquals(ALIVE, run(process, 42));
        assertTrue(waited[0]);
        assertCleaned(process);
    }

    @Test
    void expiryDuringAvailabilityCheckDoesNotStartAnotherWait() {
        InputStream input = new ByteArrayInputStream(new byte[0]) {
            @Override
            public synchronized int available() {
                long started = System.nanoTime();
                do {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
                } while (System.nanoTime() - started < TimeUnit.MILLISECONDS.toNanos(50));
                return 0;
            }
        };
        StubProcess process = new StubProcess(input, 0, true) {
            @Override
            public boolean waitFor(long timeout, TimeUnit unit) {
                fail("The collection deadline expired before this wait");
                return false;
            }
        };
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.MILLISECONDS.toNanos(20), () -> process));
        assertCleaned(process);
    }

    @Test
    void realWindowsQueryFindsThisJvm() {
        assumeTrue(OS.isWindows());
        assertTrue(Jvm.isProcessAlive(OS.getProcessId()));
        assertEquals(ALIVE, WindowsProcessProbe.query(OS.getProcessId()));
        assertEquals(DEAD, WindowsProcessProbe.query(Long.MAX_VALUE));
    }

    @Test
    void realWindowsQueryClosesOutputPipe() {
        assumeTrue(OS.isWindows());
        Process[] child = new Process[1];
        try {
            assertEquals(DEAD, WindowsProcessProbe.query(Long.MAX_VALUE, TimeUnit.SECONDS.toNanos(5), () -> {
                child[0] = WindowsProcessProbe.tasklistCommand(System.getenv("SystemRoot")).start();
                return child[0];
            }));
            assertThrows(IOException.class, () -> child[0].getInputStream().available(),
                    "The native stdout pipe remains open after successful enumeration");
        } finally {
            if (child[0] != null) {
                try {
                    child[0].destroyForcibly();
                } finally {
                    Closeable.closeQuietly(child[0].getInputStream(), child[0].getErrorStream(), child[0].getOutputStream());
                }
            }
        }
    }

    @Test
    void csvDecodingDoesNotDependOnTheDefaultCharset() throws Exception {
        Process process = JavaProcessBuilder.create(NonAsciiCharsetProbe.class)
                .withJvmArguments("-Dfile.encoding=UTF-16").inheritingIO().start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "charset probe did not terminate");
            assertEquals(0, process.exitValue(), "ASCII tasklist output used the JVM default charset");
        } finally {
            try {
                process.destroyForcibly();
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
            }
        }
    }

    public static final class NonAsciiCharsetProbe {
        public static void main(String[] args) {
            assertEquals(StandardCharsets.UTF_16, Charset.defaultCharset());
            assertEquals(ALIVE, run(new StubProcess(ROWS, 0, false), 42));
            assertEquals(DEAD, run(new StubProcess(ROWS, 0, false), 2));
        }
    }

    @Test
    void failedReadIsUnknownAndProbeIsCleaned() {
        InputStream input = new InputStream() {
            @Override public int available() { return 1; }
            @Override public int read() throws IOException { throw new IOException("read failed"); }
        };
        StubProcess process = new StubProcess(input, 0, false);
        assertEquals(UNKNOWN, run(process, 2));
        assertCleaned(process);
    }

    @Test
    void streamCloseFailureDoesNotMaskResultOrSkipOtherStreams() {
        InputStream input = new ByteArrayInputStream(ROWS.getBytes(StandardCharsets.US_ASCII)) {
            @Override
            public void close() throws IOException {
                throw new IOException("stdout close failed");
            }
        };
        StubProcess process = new StubProcess(input, 0, false);
        assertEquals(DEAD, run(process, 2));
        assertCleaned(process);
    }

    @Test
    void drainsOutputPublishedAtTheExitCheck() {
        boolean[] exited = {false};
        InputStream input = new ByteArrayInputStream(ROWS.getBytes(StandardCharsets.US_ASCII)) {
            @Override
            public synchronized int available() {
                return exited[0] ? super.available() : 0;
            }
        };
        StubProcess process = new StubProcess(input, 0, false) {
            @Override
            public boolean isAlive() {
                exited[0] = true;
                return false;
            }
        };
        assertEquals(ALIVE, run(process, 42));
        assertCleaned(process);
    }

    @Test
    @Timeout(5)
    void tricklingOutputStillHonoursTheDeadline() {
        InputStream input = new InputStream() {
            @Override
            public int available() {
                return 1;
            }

            @Override
            public int read() {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
                return 'x';
            }
        };
        StubProcess process = new StubProcess(input, 0, true);
        long started = System.nanoTime();
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.MILLISECONDS.toNanos(20), () -> process));
        assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2));
        assertCleaned(process);
    }

    @Test
    void destructionFailureStillClosesStreams() {
        RuntimeException expected = new IllegalStateException("destroy failed");
        StubProcess process = new StubProcess(ROWS, 0, false) {
            @Override
            public Process destroyForcibly() {
                super.destroyForcibly();
                throw expected;
            }
        };
        assertSame(expected, assertThrows(IllegalStateException.class, () -> run(process, 42)));
        assertCleaned(process);
    }

    @Test
    void tasklistUsesOnlyTheAbsoluteSystemDirectory(@TempDir Path directory) throws IOException {
        Path windows = Files.createDirectory(directory.resolve("Windows with spaces"));
        Path system32 = Files.createDirectory(windows.resolve("System32"));
        Path executable = Files.createFile(system32.resolve("tasklist.exe"));
        ProcessBuilder command = WindowsProcessProbe.tasklistCommand(windows.toString());
        assertEquals(Arrays.asList(executable.toString(), "/FO", "CSV", "/NH"), command.command());
        assertTrue(command.redirectErrorStream());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Windows"})
    void missingOrRelativeSystemRootIsRejected(String systemRoot) {
        assertThrows(IOException.class, () -> WindowsProcessProbe.tasklistCommand(systemRoot));
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                () -> WindowsProcessProbe.tasklistCommand(systemRoot).start()));
    }

    @Test
    void missingSystemTasklistStaysUnknown(@TempDir Path directory) {
        IOException failure = assertThrows(IOException.class,
                () -> WindowsProcessProbe.tasklistCommand(directory.toString()).start());
        assertTrue(failure.getMessage().contains(directory.toString()));
        assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                () -> WindowsProcessProbe.tasklistCommand(directory.toString()).start()));
    }

    @Test
    void relativeSystemRootIsRejectedEvenWhenTasklistExists() throws IOException {
        // The system temp directory can be on a different Windows drive from the checkout.
        Path directory = Files.createTempDirectory(Paths.get("").toAbsolutePath(), "relative-system-root-");
        Path system32 = directory.resolve("System32");
        Path executable = system32.resolve("tasklist.exe");
        try {
            Files.createFile(Files.createDirectory(system32).resolve("tasklist.exe"));
            Path relative = directory.getFileName();
            assertFalse(relative.isAbsolute());
            assertThrows(IOException.class, () -> WindowsProcessProbe.tasklistCommand(relative.toString()));
        } finally {
            Files.deleteIfExists(executable);
            Files.deleteIfExists(system32);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void disabledDiagnosticsDoNotInvokeTheHandler() {
        RecordingExceptionHandlerStub recording = new RecordingExceptionHandlerStub();
        recording.enabled(false);
        withDebugHandler(recording, () -> assertEquals(UNKNOWN, WindowsProcessProbe.parse(42, "invalid")));
        assertEquals(0, recording.eventCount());
    }

    @Test
    void launchDiagnosticPreservesTheOriginalFailure() {
        IOException failure = new IOException("tasklist launch failed");
        RecordingExceptionHandlerStub recording = new RecordingExceptionHandlerStub();
        withDebugHandler(recording, () -> assertEquals(UNKNOWN, WindowsProcessProbe.query(42,
                TimeUnit.SECONDS.toNanos(1), () -> { throw failure; })));
        assertEquals(1, recording.eventCount());
        assertSame(failure, recording.event(0).thrown());
    }

    @Test
    void unknownReasonsAreReportedAtDebug() {
        RecordingExceptionHandlerStub recording = new RecordingExceptionHandlerStub();
        withDebugHandler(recording, () -> {
            assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                    () -> { throw new IOException("missing tasklist"); }));
            assertEquals(UNKNOWN, WindowsProcessProbe.query(42, TimeUnit.SECONDS.toNanos(1),
                    () -> { throw new SecurityException("denied"); }));
            assertEquals(UNKNOWN, run(new StubProcess(ROWS, 1, false), 42));
            assertEquals(UNKNOWN, WindowsProcessProbe.parse(42, "malformed"));
            assertEquals(UNKNOWN, run(new StubProcess(new String(new char[1024 * 1024 + 1]), 0, false), 42));
            assertEquals(UNKNOWN, WindowsProcessProbe.query(42, 0, () -> new StubProcess("", 0, true)));
            Thread.currentThread().interrupt();
            try {
                assertEquals(UNKNOWN, run(new StubProcess(ROWS, 0, false), 42));
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        });
        for (String reason : new String[]{"IO_FAILURE", "ACCESS_DENIED", "NONZERO_EXIT", "INVALID_OUTPUT",
                "OUTPUT_LIMIT", "DEADLINE", "INTERRUPTED"}) {
            assertTrue(recording.events().stream().anyMatch(event -> event.clazz() == WindowsProcessProbe.class
                    && event.message().contains("Process 42 liveness UNKNOWN: " + reason)), reason);
        }
    }

    @Test
    void diagnosticFailureDoesNotChangeUnknownOrCleanup() {
        RecordingExceptionHandlerStub recording = new RecordingExceptionHandlerStub();
        recording.throwFromClassHandler(new IllegalStateException("logger failed"));
        StubProcess process = new StubProcess(ROWS, 1, false);
        withDebugHandler(recording, () -> assertEquals(UNKNOWN, run(process, 42)));
        assertCleaned(process);
    }

    private static void withDebugHandler(ExceptionHandler handler, Runnable body) {
        ThreadLocalisedExceptionHandler debug = (ThreadLocalisedExceptionHandler) Jvm.debug();
        ExceptionHandler previous = ThreadLocalisedExceptionHandler.unwrap(debug);
        debug.threadLocalHandler(handler);
        try {
            body.run();
        } finally {
            debug.threadLocalHandler(previous);
        }
    }

    private static void assertCleaned(StubProcess process) {
        assertTrue(process.destroyed, "child was not destroyed");
        assertTrue(process.inputClosedAfterDestroy, "stdout was not closed after destruction");
        assertTrue(process.errorClosedAfterDestroy, "stderr was not closed after destruction");
        assertTrue(process.outputClosed, "stdin was not closed");
    }

    private static WindowsProcessProbe.Result run(StubProcess process, long pid) {
        return WindowsProcessProbe.query(pid, TimeUnit.SECONDS.toNanos(1), () -> process);
    }

    private static class StubProcess extends Process {
        private final InputStream input;
        private boolean inputClosedAfterDestroy;
        private boolean errorClosedAfterDestroy;
        private boolean outputClosed;
        private final InputStream error = new ByteArrayInputStream(new byte[0]) {
            @Override
            public void close() throws IOException {
                errorClosedAfterDestroy = destroyed;
                super.close();
            }
        };
        private final OutputStream output = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                outputClosed = true;
                super.close();
            }
        };
        private final int status;
        private final boolean running;
        private boolean destroyed;

        StubProcess(String output, int status, boolean running) {
            this(new ByteArrayInputStream(output.getBytes(StandardCharsets.US_ASCII)), status, running);
        }

        StubProcess(InputStream input, int status, boolean running) {
            this.input = new FilterInputStream(input) {
                @Override
                public void close() throws IOException {
                    inputClosedAfterDestroy = destroyed;
                    super.close();
                }
            };
            this.status = status;
            this.running = running;
        }

        @Override public OutputStream getOutputStream() { return output; }
        @Override public InputStream getInputStream() { return input; }
        @Override public InputStream getErrorStream() { return error; }
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
