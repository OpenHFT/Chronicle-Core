/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.UnaryOperator;
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

    private static String readModelFromCommand(String fallback, String[] command, UnaryOperator<String> mapping) throws IOException {
        return readModelFromCommand(fallback, command, mapping, 5, TimeUnit.SECONDS);
    }

    static String readModelFromCommand(String fallback, String[] command, UnaryOperator<String> mapping,
                                       long timeout, TimeUnit unit) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        FutureTask<byte[]> output = new FutureTask<>(new CommandOutput(process.getInputStream()));
        Thread reader = new Thread(output, "chronicle-cpu-output");
        reader.setDaemon(true);
        reader.start();
        try {
            // Drain stdout concurrently so a full pipe cannot prevent process completion.
            // Stderr is inherited and cannot be mistaken for a CPU model.
            if (!process.waitFor(timeout, unit) || process.exitValue() != 0)
                return fallback;
            byte[] bytes = output.get(1, TimeUnit.SECONDS);
            String model = bytes == null ? null : modelFromOutput(bytes, mapping);
            return model == null ? fallback : model;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return fallback;
        } catch (TimeoutException timedOut) {
            return fallback;
        } catch (ExecutionException failed) {
            throw new IOException("Unable to read CPU model command output", failed.getCause());
        } finally {
            process.destroyForcibly();
            output.cancel(true);
            try {
                process.waitFor(1, TimeUnit.SECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String modelFromOutput(byte[] bytes, UnaryOperator<String> mapping) {
        for (String line : new String(bytes, StandardCharsets.UTF_8).split("\\R")) {
            String model = mapping.apply(line.trim());
            if (model != null && !model.isEmpty())
                return model;
        }
        return null;
    }

    // This task must not access CpuClass while its static initialiser waits for stdout.
    private static final class CommandOutput implements Callable<byte[]> {
        private final InputStream input;

        private CommandOutput(InputStream input) {
            this.input = input;
        }

        @Override
        public byte[] call() throws IOException {
            try (InputStream stream = input) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int length;
                while ((length = stream.read(buffer)) != -1) {
                    if (bytes.size() + length > 1024 * 1024)
                        return null;
                    bytes.write(buffer, 0, length);
                }
                return bytes.toByteArray();
            }
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
    static UnaryOperator<String> removingTag() {
        return line -> line.replaceFirst("[^:]*+: ", "");
    }
}
