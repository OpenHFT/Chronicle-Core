/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import java.io.ByteArrayOutputStream;
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

    private WindowsProcessProbe() {
    }

    public static Result query(long pid) {
        //! A filtered no-match response is localised prose, so enumerate CSV rows directly.
        //! realWindowsQueryFindsThisJvm covers native enumeration; malformedOrEmptyEnumerationIsUnknown
        //! rejects prose as proof of death, and recognisesExactPidAndConfirmedAbsence checks the PID column.
        return query(pid, TimeUnit.SECONDS.toNanos(5), () -> new ProcessBuilder(
                "tasklist.exe", "/FO", "CSV", "/NH").redirectErrorStream(true).start());
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
                    return Result.UNKNOWN;
                //! Avoid an unbounded pipe read or wait while checking another process's lock.
                //! stalledProbeIsBoundedAndDestroyed exercises the collection deadline.
                if (System.nanoTime() - started >= timeoutNanos)
                    return Result.UNKNOWN;
                int available = input.available();
                if (available > 0) {
                    int read = input.read(buffer, 0, Math.min(available, buffer.length));
                    if (read > 0) {
                        if (output.size() + read > MAX_OUTPUT)
                            return Result.UNKNOWN;
                        output.write(buffer, 0, read);
                    }
                } else if (!process.isAlive()) {
                    // The child may have written its last row between available()
                    // and the exit check. Drain that row before judging absence.
                    if (input.available() > 0)
                        continue;
                    if (process.exitValue() != 0)
                        return Result.UNKNOWN;
                    return parse(pid, new String(output.toByteArray(), StandardCharsets.US_ASCII));
                } else {
                    long remaining = timeoutNanos - (System.nanoTime() - started);
                    if (remaining <= 0)
                        return Result.UNKNOWN;
                    process.waitFor(Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(10)), TimeUnit.NANOSECONDS);
                }
            }
        } catch (IOException | SecurityException failure) {
            //! An unavailable query cannot authorise lock recovery. failedStartAndDenialAreUnknown
            //! covers failed launch/access checks; nonzeroExitCannotEstablishDeathAndProbeIsCleaned
            //! covers an unsuccessful child. Interruption likewise preserves UNKNOWN and the flag.
            return Result.UNKNOWN;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Result.UNKNOWN;
        } finally {
            if (process != null) {
                //! Always destroy the child without an unbounded EOF read or termination wait.
                //! successfulEnumerationEstablishesDeathAndProbeIsCleaned and
                //! interruptedProbePreservesInterruptAndDestroysChild assert cleanup on both outcomes.
                process.destroyForcibly();
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
                return Result.UNKNOWN;
            any = true;
            try {
                found |= Long.parseLong(row.group(1)) == pid;
            } catch (NumberFormatException malformedPid) {
                return Result.UNKNOWN;
            }
        }
        if (!any)
            return Result.UNKNOWN;
        return found ? Result.ALIVE : Result.DEAD;
    }

    @FunctionalInterface
    interface Starter {
        Process start() throws IOException;
    }
}
