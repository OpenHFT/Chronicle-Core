/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class CpuClass {
    static final String CPU_MODEL;

    private static final Logger LOGGER = LoggerFactory.getLogger(CpuClass.class);

    static {
        String model = System.getProperty("os.arch", "unknown");

        try {
            final Path path = Paths.get("/proc/cpuinfo");
            if (Files.isReadable(path)) {
                try (Stream<String> lines = Files.lines(path)) {
                    model = lines
                            .filter(line -> line.startsWith("model name"))
                            .map(removingTag())
                            .findFirst().orElse(model);
                }
            } else if (Bootstrap.IS_WIN) {
                model = readModelFromCommand(model, new String[]{
                        "powershell.exe", "-NoProfile", "-NonInteractive", "-Command",
                        "$ErrorActionPreference = 'Stop'; Get-CimInstance Win32_Processor | Select-Object -First 1 -ExpandProperty Name"
                }, line -> !line.isEmpty() ? line : null);
            } else if (Bootstrap.IS_MAC) {
                model = readModelFromCommand(model, new String[]{"sysctl", "-a"}, line ->
                        line.startsWith("machdep.cpu.brand_string") ? removingTag().apply(line) : null);
            }

        } catch (IOException e) {
            LOGGER.debug("Unable to read cpuinfo", e);
        }
        CPU_MODEL = model;
    }

    private static String readModelFromCommand(String fallback, String[] command, Function<String, String> mapping) throws IOException {
        return readModelFromCommand(fallback, command, mapping, 5, TimeUnit.SECONDS);
    }

    static String readModelFromCommand(String fallback, String[] command, Function<String, String> mapping,
                                       long timeout, TimeUnit unit) throws IOException {
        Path output = Files.createTempFile("chronicle-cpu-", ".out");
        Process process = null;
        try {
            // Redirect both streams so output cannot fill a pipe while waiting for completion.
            // Only stdout is eligible as a model name; PowerShell diagnostics are not CPU names.
            process = new ProcessBuilder(command).redirectOutput(output.toFile())
                    .redirectError(ProcessBuilder.Redirect.INHERIT).start();
            if (!process.waitFor(timeout, unit) || process.exitValue() != 0)
                return fallback;
            if (Files.size(output) > 1024 * 1024)
                return fallback;
            try (Stream<String> lines = Files.lines(output)) {
                return lines.map(String::trim).map(mapping)
                        .filter(s -> s != null && !s.isEmpty()).findFirst().orElse(fallback);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return fallback;
        } finally {
            if (process != null) {
                process.destroyForcibly();
                try {
                    process.waitFor(1, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            Files.deleteIfExists(output);
        }
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private CpuClass() {
    }

    public static String getCpuModel() {
        return CPU_MODEL;
    }

    @SuppressWarnings("java:S5852") // Possessive quantifiers (*+) are used preventing catastrophic backtracking
    @NotNull
    static Function<String, String> removingTag() {
        return line -> line.replaceFirst("[^:]*+: ", "");
    }
}
