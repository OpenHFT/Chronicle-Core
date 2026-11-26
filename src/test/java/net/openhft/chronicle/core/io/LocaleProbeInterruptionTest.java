/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LocaleProbeInterruptionTest {
    public static class Child {
        public static void main(String[] args) {
            boolean interrupted = args[0].equals("interrupted");
            if (interrupted) Thread.currentThread().interrupt();
            boolean closed =
                    IOTools.isClosedException(
                            args[0].equals("closed")
                                    ? new java.nio.channels.ClosedChannelException()
                                    : new IOException("ordinary failure"));
            if (Thread.currentThread().isInterrupted() != interrupted)
                throw new AssertionError("Locale initialisation changed caller interrupt status");
            if (closed != args[0].equals("closed"))
                throw new AssertionError("Incorrect closed classification");
            System.out.println("OK");
        }
    }

    private void child(String language, String mode) throws Exception {
        java.nio.file.Path log = java.nio.file.Files.createTempFile("locale-probe-", ".log");
        List<String> args = new ArrayList<>();
        args.add(new File(System.getProperty("java.home"), "bin/java").getPath());
        for (String arg :
                java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments())
            if (arg.startsWith("--add-opens") || arg.startsWith("--add-exports")
                    || arg.startsWith("-javaagent:") && arg.contains("jacoco"))
                args.add(arg); // Include the isolated locale probe in coverage when the parent is instrumented.
        args.add("-ea");
        args.add("-Duser.language=" + language);
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));
        args.add(Child.class.getName());
        args.add(mode);
        Process process =
                new ProcessBuilder(args)
                        .redirectErrorStream(true)
                        .redirectOutput(log.toFile())
                        .start();
        try {
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            String output =
                    new String(
                            java.nio.file.Files.readAllBytes(log),
                            java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(finished, "Child timed out: " + output);
            assertEquals(0, process.exitValue(), output);
        } finally {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            java.nio.file.Files.deleteIfExists(log);
        }
    }

    @Test
    void englishDoesNotInterruptCaller() throws Exception {
        child("en", "ordinary");
    }

    @Test
    void germanDoesNotInterruptCaller() throws Exception {
        child("de", "ordinary");
    }

    @Test
    void frenchDoesNotInterruptCaller() throws Exception {
        child("fr", "ordinary");
    }

    @Test
    void japaneseDoesNotInterruptCaller() throws Exception {
        child("ja", "ordinary");
    }

    @Test
    void preexistingInterruptIsPreserved() throws Exception {
        child("de", "interrupted");
    }

    @Test
    void closedChannelIsRecognisedAfterLocaleInit() throws Exception {
        child("de", "closed");
    }
}
