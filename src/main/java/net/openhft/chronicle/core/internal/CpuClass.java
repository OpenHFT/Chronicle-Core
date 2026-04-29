/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.io.IOTools;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CpuClass {
    static final String CPU_MODEL;

    private static final String PROCESS = "process ";
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
                model = readModelFromCommand(model, "wmic cpu get name", line ->
                        !"Name".equals(line) && !line.isEmpty() ? line : null);
            } else if (Bootstrap.IS_MAC) {
                model = readModelFromCommand(model, "sysctl -a", line ->
                        line.startsWith("machdep.cpu.brand_string") ? removingTag().apply(line) : null);
            }

        } catch (IOException e) {
            LOGGER.debug("Unable to read cpuinfo", e);
        }
        CPU_MODEL = model;
    }

    private static String readModelFromCommand(String fallback, String cmd, Function<String, String> mapping) throws IOException {
        Process process = new ProcessBuilder(cmd.split(" "))
                .redirectErrorStream(true)
                .start();
        try {
            String result = fallback;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                result = reader.lines()
                        .map(String::trim)
                        .map(mapping)
                        .filter(s -> s != null && !s.isEmpty())
                        .findFirst().orElse(fallback);
            }
            try {
                int ret = process.waitFor();
                if (ret != 0)
                    LOGGER.warn(PROCESS + cmd + " returned " + ret);
            } catch (InterruptedException e) {
                LOGGER.warn(PROCESS + cmd + " waitFor threw ", e);
                Thread.currentThread().interrupt();
            }
            return result;
        } finally {
            IOTools.destroyProcess(process);
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
