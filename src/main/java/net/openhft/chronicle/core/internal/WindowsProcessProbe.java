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
    private static final int MAX_OUTPUT = 1024 * 1024;
    private static final Pattern ROW = Pattern.compile(
            "\"(?:[^\"]|\"\")*\",\"([0-9]+)\",\"(?:[^\"]|\"\")*\",\"[0-9]+\",\"(?:[^\"]|\"\")*\"");

    public enum Result { ALIVE, DEAD, UNKNOWN }

    private WindowsProcessProbe() {
    }

    public static Result query(long pid) {
        // Enumerate all rows: a filtered no-match result is localised prose, not
        // a machine-readable indication of absence. Avoid a cmd.exe intermediary.
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
            return Result.UNKNOWN;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Result.UNKNOWN;
        } finally {
            if (process != null) {
                // Do not read to EOF or wait indefinitely for an unresponsive child.
                process.destroyForcibly();
            }
        }
    }

    static Result parse(long pid, String output) {
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
        return !any ? Result.UNKNOWN : found ? Result.ALIVE : Result.DEAD;
    }

    @FunctionalInterface
    interface Starter {
        Process start() throws IOException;
    }
}
