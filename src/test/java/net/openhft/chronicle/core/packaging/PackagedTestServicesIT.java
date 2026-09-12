/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.packaging;

import net.openhft.chronicle.core.ChronicleInitRunnable;
import net.openhft.chronicle.core.cleaner.CleanerServiceLocator;
import net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs after packaging, using the exported fixtures ahead of the main JAR. */
public class PackagedTestServicesIT {
    @TempDir
    Path tempDir;

    @Test
    void exportedFixturesDoNotSelectTestCleaner() throws Exception {
        runConsumer("cleaner");
    }

    @Test
    void exportedFixturesDoNotRegisterTestInitialiser() throws Exception {
        runConsumer("initialiser");
    }

    private void runConsumer(String mode) throws Exception {
        final List<String> command = new ArrayList<>();
        final String java = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        command.add(Paths.get(System.getProperty("java.home"), "bin", java).toString());
        // Preserve the parent test JVM's module access without its test-class directories.
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("--add-opens=") || argument.startsWith("--add-exports="))
                command.add(argument);
        }
        command.add("-cp");
        command.add(consumerClasspath());
        command.add(PackagedTestServicesIT.class.getName());
        command.add(mode);
        final Path output = tempDir.resolve(mode + ".log");
        final Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(child.waitFor(20, TimeUnit.SECONDS), "Packaged fixture consumer did not finish");
            assertEquals(0, child.exitValue(), new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                assertTrue(child.waitFor(5, TimeUnit.SECONDS), "Packaged fixture consumer did not terminate");
            }
        }
    }

    private static String consumerClasspath() {
        final List<String> entries = new ArrayList<>();
        entries.add(System.getProperty("packagedCoreTestJar"));
        entries.add(System.getProperty("packagedCoreJar"));
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (entry.endsWith(".jar"))
                entries.add(entry);
        }
        return String.join(File.pathSeparator, entries);
    }

    public static void main(String[] args) {
        if ("initialiser".equals(args[0])) {
            for (ChronicleInitRunnable initialiser : ServiceLoader.load(ChronicleInitRunnable.class))
                throw new AssertionError("Exported fixture registered an initialiser: " + initialiser.getClass().getName());
            return;
        }

        final ByteBufferCleanerService cleaner = CleanerServiceLocator.cleanerService();
        System.out.println("Selected " + cleaner.getClass().getName());
        if (cleaner.getClass().getName().contains(".testimpl."))
            throw new AssertionError("Exported fixture selected a test cleaner");
        final BufferPoolMXBean direct = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class).stream()
                .filter(pool -> "direct".equals(pool.getName())).findFirst().orElseThrow(AssertionError::new);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
        final long allocated = direct.getMemoryUsed();
        cleaner.clean(buffer);
        if (direct.getMemoryUsed() >= allocated)
            throw new AssertionError("Selected cleaner did not free the consumer's direct buffer");
    }
}
