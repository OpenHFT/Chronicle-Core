/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeMappingClassLoaderTest extends CoreTestCommon {
    @Test
    void ordinaryMappingAndUnmappingStillWork() throws Exception {
        runChild("ordinary");
    }

    @Test
    void firstMappingWorksWithoutContextClassLoader() throws Exception {
        runChild("map");
    }

    @Test
    void firstUnmapWorksWithoutContextClassLoader() throws Exception {
        runChild("unmap");
    }

    private void runChild(String phase) throws Exception {
        Path directory = Files.createTempDirectory("native-mapping-loader-");
        Path data = directory.resolve("mapped.dat");
        Path log = directory.resolve("child.log");
        Process child = null;
        try {
            List<String> command = new ArrayList<>();
            command.add(new File(System.getProperty("java.home"), "bin/java").getPath());
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments())
                if (argument.startsWith("--add-opens") || argument.startsWith("--add-exports"))
                    command.add(argument);
            Collections.addAll(command, "-ea", "-Dchronicle.analytics.disable=true", "-cp",
                    System.getProperty("java.class.path"), Child.class.getName(), phase, data.toString());
            child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            boolean exited = child.waitFor(30, TimeUnit.SECONDS);
            if (!exited) {
                child.destroyForcibly();
                child.waitFor(10, TimeUnit.SECONDS);
            }
            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            assertTrue(exited, output);
            assertEquals(0, child.exitValue(), output);
            assertTrue(output.contains("MAPPED AND UNMAPPED"), output);
        } finally {
            if (child != null) {
                child.destroyForcibly();
                child.waitFor(10, TimeUnit.SECONDS);
                child.getInputStream().close();
                child.getErrorStream().close();
                child.getOutputStream().close();
            }
            // The failed baseline may retain a mapping until its child JVM exits.
            Files.deleteIfExists(data);
            Files.deleteIfExists(log);
            Files.deleteIfExists(directory);
        }
    }

    public static class Child {
        public static void main(String[] args) throws Exception {
            Thread thread = Thread.currentThread();
            ClassLoader original = thread.getContextClassLoader();
            try (RandomAccessFile file = new RandomAccessFile(args[1], "rw")) {
                if ("map".equals(args[0]))
                    thread.setContextClassLoader(null);
                int size = OS.pageSize();
                file.setLength(size);
                long address = OS.map(file.getChannel(), FileChannel.MapMode.READ_WRITE, 0, size);
                try {
                    OS.memory().writeLong(address, 42L);
                    if (OS.memory().readLong(address) != 42L)
                        throw new AssertionError("Mapped memory round trip failed");
                    if ("unmap".equals(args[0]))
                        thread.setContextClassLoader(null);
                } finally {
                    OS.unmap(address, size);
                }
                System.out.println("MAPPED AND UNMAPPED");
            } finally {
                thread.setContextClassLoader(original);
            }
        }
    }
}
