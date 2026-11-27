/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.cleaner.ReflectionBasedByteBufferCleanerService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Launches a JVM without Chronicle's module flags (unlike JavaProcessBuilder, which copies them)
 * and checks that each access failure names the flags to add rather than stale advice.
 */
class ModuleAccessDiagnosticsTest extends CoreTestCommon {

    // Every flag the access messages name: the mapping flags plus the cleaner's jdk.internal.ref export
    private static final String ADVISED_FLAGS = "--add-opens java.base/java.lang.reflect=ALL-UNNAMED " +
            "--add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-exports java.base/jdk.internal.ref=ALL-UNNAMED";

    public static void main(String[] args) throws Exception {
        final File file = File.createTempFile("module-access", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(OS.pageSize());
            final long address = OS.map(raf.getChannel(), FileChannel.MapMode.READ_WRITE, 0, OS.pageSize());
            System.out.println("MAPPED");
            OS.unmap(address, OS.pageSize());
            System.out.println("UNMAPPED");
        } catch (Throwable t) {
            for (Throwable cause = t; cause != null; cause = cause.getCause())
                System.out.println("FAILURE " + cause);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
        ReflectionBasedByteBufferCleanerService cleaner = new ReflectionBasedByteBufferCleanerService();
        System.out.println("CLEANER " + cleaner.impact());
        if (!"UNAVAILABLE".equals(cleaner.impact().toString())) {
            cleaner.clean(ByteBuffer.allocateDirect(8));
            System.out.println("CLEANED");
        }
    }

    private static String runChild(String flags) throws Exception {
        final File log = File.createTempFile("module-access", ".log");
        try {
            final List<String> command = new ArrayList<>();
            command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
            if (!flags.isEmpty())
                command.addAll(Arrays.asList(flags.split(" ")));
            command.addAll(Arrays.asList("-Dchronicle.analytics.disable=true",
                    "-cp", System.getProperty("java.class.path"),
                    ModuleAccessDiagnosticsTest.class.getName()));
            final ProcessBuilder builder = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(log);
            builder.environment().remove("JAVA_TOOL_OPTIONS");
            builder.environment().remove("JDK_JAVA_OPTIONS");
            builder.environment().remove("_JAVA_OPTIONS");
            final Process process = builder.start();
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
                throw new AssertionError("Child JVM did not exit within 60 seconds");
            }
            final String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            Files.deleteIfExists(log.toPath());
        }
    }

    @Test
    void jvmWithoutModuleFlagsIsToldWhichFlagsToAdd() throws Exception {
        assumeTrue(Jvm.majorVersion() >= 9);
        // Java 9-15 allow illegal reflective access by default; request strong encapsulation.
        final String output = runChild(Jvm.majorVersion() < 17 ? "--illegal-access=deny" : "");

        assertFalse(Arrays.asList(output.split("\\R")).contains("MAPPED"), output);
        // the original access failure is kept as the cause
        assertTrue(output.contains("FAILURE java.lang.IllegalAccessException"), output);
        assertTrue(output.contains("setAccessible0"), output);
        for (String flag : ADVISED_FLAGS.split(" (?=--)"))
            assertTrue(output.contains(flag), flag + " missing from " + output);
        // removed in Java 17; the JVM ignores it
        assertFalse(output.contains("--illegal-access=permit"), output);
    }

    @Test
    void advisedFlagsAreEnoughToMapAndClean() throws Exception {
        assumeTrue(Jvm.majorVersion() >= 9);
        final String output = runChild((Jvm.majorVersion() < 17 ? "--illegal-access=deny " : "") + ADVISED_FLAGS);

        assertTrue(Arrays.asList(output.split("\\R")).contains("MAPPED"), output);
        assertTrue(Arrays.asList(output.split("\\R")).contains("UNMAPPED"), output);
        assertFalse(output.contains("FAILURE"), output);
        assertTrue(Arrays.asList(output.split("\\R")).contains("CLEANED"), output);
        assertFalse(output.contains("setAccessible0"), output);
    }

    @Test
    void java8NeedsNoModuleFlags() throws Exception {
        assumeTrue(Jvm.majorVersion() == 8);
        final String output = runChild("");
        assertTrue(Arrays.asList(output.split("\\R")).contains("MAPPED"), output);
        assertTrue(Arrays.asList(output.split("\\R")).contains("UNMAPPED"), output);
        assertTrue(Arrays.asList(output.split("\\R")).contains("CLEANED"), output);
        assertFalse(output.contains("FAILURE"), output);
        assertFalse(output.contains("--add-opens"), output);
        assertFalse(output.contains("--add-exports"), output);
    }

    @Test
    void mappingAdviceDoesNotSuggestModuleFlagsOnJava8() {
        assertFalse(OS.mappingAccessAdvice(false).contains("--add-"));
        assertTrue(OS.mappingAccessAdvice(false).contains("Java 8"));
        assertTrue(OS.mappingAccessAdvice(true).contains("--add-opens"));
        assertTrue(OS.mappingAccessAdvice(true).contains("--add-exports"));
    }
}
