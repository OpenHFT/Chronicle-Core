/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bounded Windows process enumeration. Only a complete, successful enumeration
 * can establish absence: access denial, malformed output and timeout are unknown.
 */
public final class WindowsProcessProbe {
    //! A verbose or corrupt tasklist response must not allocate unbounded storage.
    //! WindowsProcessProbeTest.oversizedOutputIsUnknown covers the cap and child cleanup.
    private static final int MAX_OUTPUT = 1024 * 1024;
    //! Greedy repeated alternatives overflow the regex stack on fields below MAX_OUTPUT.
    //! Possessive repetition keeps matching iterative; longCsvFieldsRemainParseable and
    //! malformedLongCsvFieldIsUnknown fail without it. escapedQuotesPreservePidMatching
    //! preserves the CSV quoting contract while changing the matching strategy.
    private static final Pattern ROW = Pattern.compile(
            "\"(?:[^\"]|\"\")*+\",\"([0-9]+)\",\"(?:[^\"]|\"\")*+\",\"[0-9]+\",\"(?:[^\"]|\"\")*+\"");

    public enum Result { ALIVE, DEAD, UNKNOWN }

    private enum UnknownReason {
        INTERRUPTED, DEADLINE, OUTPUT_LIMIT, NONZERO_EXIT, IO_FAILURE, ACCESS_DENIED, INVALID_OUTPUT
    }

    private WindowsProcessProbe() {
    }

    public static Result query(long pid) {
        //! A filtered no-match response is localised prose, so enumerate CSV rows directly.
        //! realWindowsQueryFindsThisJvm covers native enumeration; malformedOrEmptyEnumerationIsUnknown
        //! rejects prose as proof of death, and recognisesExactPidAndConfirmedAbsence checks the PID column.
        return query(pid, TimeUnit.SECONDS.toNanos(5), () -> tasklistCommand(System.getenv("SystemRoot")).start());
    }

    static ProcessBuilder tasklistCommand(String systemRoot) throws IOException {
        //! A bare executable name lets the working directory or PATH select the liveness authority.
        //! Only use the configured system directory; unavailable configuration stays UNKNOWN.
        //! tasklistUsesOnlyTheAbsoluteSystemDirectory and missingOrRelativeSystemRootIsRejected cover this choice.
        //! missingSystemTasklistStaysUnknown prevents falling back to a searched executable when the system file is absent.
        if (systemRoot == null || systemRoot.isEmpty() || !new File(systemRoot).isAbsolute())
            throw new IOException("SystemRoot must name an absolute Windows directory");
        File executable = new File(new File(systemRoot, "System32"), "tasklist.exe");
        if (!executable.isFile())
            throw new IOException("System tasklist executable is missing: " + executable);
        return new ProcessBuilder(executable.getPath(), "/FO", "CSV", "/NH").redirectErrorStream(true);
    }

    static Result query(long pid, long timeoutNanos, Starter starter) {
        Process process = null;
        long started = System.nanoTime();
        try {
            process = starter.start();
            process.getOutputStream().close();
            InputStream input = process.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (;;) {
                //! Buffered output and an exited child can avoid waitFor entirely, so its
                //! InterruptedException cannot be the only cancellation check. Both
                //! interruptedCompletedProbeCannotEstablishDeath and
                //! interruptionDuringReadCannotEstablishDeath returned DEAD without this check.
                if (Thread.currentThread().isInterrupted())
                    return unknown(pid, UnknownReason.INTERRUPTED);
                //! Avoid an unbounded pipe read or wait while checking another process's lock.
                //! stalledProbeIsBoundedAndDestroyed and tricklingOutputStillHonoursTheDeadline exercise the collection deadline.
                if (System.nanoTime() - started >= timeoutNanos)
                    return unknown(pid, UnknownReason.DEADLINE);
                int available = input.available();
                if (available > 0) {
                    int read = input.read(buffer, 0, Math.min(available, buffer.length));
                    if (read > 0) {
                        if (output.size() + read > MAX_OUTPUT)
                            return unknown(pid, UnknownReason.OUTPUT_LIMIT);
                        output.write(buffer, 0, read);
                    }
                } else if (!process.isAlive()) {
                    //! The last row can arrive between the availability and exit checks.
                    //! drainsOutputPublishedAtTheExitCheck requires that row to be read before classification.
                    if (input.available() > 0)
                        continue;
                    if (process.exitValue() != 0)
                        return unknown(pid, UnknownReason.NONZERO_EXIT);
                    return parse(pid, new String(output.toByteArray(), StandardCharsets.US_ASCII));
                } else {
                    long remaining = timeoutNanos - (System.nanoTime() - started);
                    if (remaining <= 0)
                        return unknown(pid, UnknownReason.DEADLINE);
                    process.waitFor(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(10)), TimeUnit.NANOSECONDS);
                }
            }
        } catch (IOException | SecurityException failure) {
            //! An unavailable query cannot authorise lock recovery. failedStartAndDenialAreUnknown
            //! covers failed launch/access checks; nonzeroExitCannotEstablishDeathAndProbeIsCleaned
            //! covers an unsuccessful child. failedReadIsUnknownAndProbeIsCleaned covers a pipe read failure.
            //! Interruption likewise preserves UNKNOWN and the flag.
            return unknown(pid, failure instanceof SecurityException ? UnknownReason.ACCESS_DENIED : UnknownReason.IO_FAILURE, failure);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return unknown(pid, UnknownReason.INTERRUPTED);
        } finally {
            if (process != null) {
                //! Windows destruction does not close pipe handles. Destroy first to stop writers,
                //! then close every stream without adding a termination wait, even if destruction throws.
                //! successfulEnumerationEstablishesDeathAndProbeIsCleaned and
                //! interruptedProbePreservesInterruptAndDestroysChild assert closure and ordering.
                //! destructionFailureStillClosesStreams covers the exceptional cleanup path.
                //! realWindowsQueryClosesOutputPipe verifies native closure, and
                //! streamCloseFailureDoesNotMaskResultOrSkipOtherStreams covers a failed close.
                try {
                    process.destroyForcibly();
                } finally {
                    Closeable.closeQuietly(process.getInputStream());
                    Closeable.closeQuietly(process.getErrorStream());
                    Closeable.closeQuietly(process.getOutputStream());
                }
            }
        }
    }

    static Result parse(long pid, String output) {
        //! A substring match can confuse image names, memory sizes and other PIDs with the owner.
        //! Require every nonblank row to be valid before proving absence, even if an earlier row matched.
        //! recognisesExactPidAndConfirmedAbsence and malformedOrEmptyEnumerationIsUnknown cover this policy.
        boolean found = false;
        boolean any = false;
        for (String line : output.split("\\r?\\n")) {
            if (line.trim().isEmpty())
                continue;
            Matcher row = ROW.matcher(line.trim());
            if (!row.matches())
                return unknown(pid, UnknownReason.INVALID_OUTPUT);
            any = true;
            try {
                found |= Long.parseLong(row.group(1)) == pid;
            } catch (NumberFormatException malformedPid) {
                return unknown(pid, UnknownReason.INVALID_OUTPUT);
            }
        }
        if (!any)
            return unknown(pid, UnknownReason.INVALID_OUTPUT);
        return found ? Result.ALIVE : Result.DEAD;
    }

    private static Result unknown(long pid, UnknownReason reason) {
        return unknown(pid, reason, null);
    }

    private static Result unknown(long pid, UnknownReason reason, Throwable failure) {
        //! Repeated UNKNOWN results prevent stale-lock recovery; operators need a reason without
        //! WARN spam in a polling loop. unknownReasonsAreReportedAtDebug covers reason/PID diagnostics.
        try {
            if (Jvm.isDebugEnabled(WindowsProcessProbe.class))
                Jvm.debug().on(WindowsProcessProbe.class,
                        "Process " + pid + " liveness UNKNOWN: " + reason + "; treating as alive", failure);
        } catch (RuntimeException ignored) {
            //! A user-supplied logging handler must not replace the conservative result or prevent disposal.
            //! diagnosticFailureDoesNotChangeUnknownOrCleanup covers this fallback.
        }
        return Result.UNKNOWN;
    }

    @FunctionalInterface
    interface Starter {
        Process start() throws IOException;
    }
}
