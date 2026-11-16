/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JvmArgsTest {
    private static final String EXPORT = "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED";
    private static final int JAVA = Integer.parseInt(System.getProperty("java.specification.version").replace("1.", ""));

    public static class Startup {
        public static void main(String[] args) {
            System.out.println("READY " + Jvm.majorVersion());
        }
    }

    @Test
    void defaultStartupDoesNotLoadCompilerProbe() throws Exception {
        Result result = child(Startup.class.getName(), null, "-verbose:class");
        assertEquals(0, result.exit, result.output);
        assertTrue(result.output.contains("READY "), result.output);
        assertFalse(result.output.contains("CompilerAccessProbe"), result.output);
    }

    @Test
    void cliAcceptsWorkingAccessIncludingJava8() throws Exception {
        Result result = JAVA == 8 ? child(JvmArgs.class.getName(), null)
                : child(JvmArgs.class.getName(), null, "--add-exports=" + EXPORT);
        assertEquals(0, result.exit, result.output);
        assertTrue(result.output.contains("SUPPORTED:"), result.output);
    }

    @Test
    void equivalentAndInheritedExportsWork() throws Exception {
        assumeTrue(JAVA >= 9);
        Result split = child(JvmArgs.class.getName(), null, "--add-exports", EXPORT);
        assertEquals(0, split.exit, split.output);
        Result inherited = child(JvmArgs.class.getName(), "--add-exports=" + EXPORT);
        assertEquals(0, inherited.exit, inherited.output);
        assertTrue(inherited.output.contains("SUPPORTED:"), inherited.output);
    }

    @Test
    void deniedAccessIsNotReportedAsSuccess() throws Exception {
        assumeTrue(JAVA >= 9);
        Result result = child(JvmArgs.class.getName(), null, strict());
        assertEquals(1, result.exit, result.output);
        assertTrue(result.output.contains("DENIED:"), result.output);
        assertTrue(result.output.contains("--add-exports " + EXPORT), result.output);
    }

    @Test
    void missingCompilerIsUnavailableNotDenied() throws Exception {
        assumeTrue(JAVA >= 9);
        for (String modules : new String[]{"java.base", "java.base,java.compiler"}) {
            Result result = child(JvmArgs.class.getName(), null, "--limit-modules=" + modules);
            assertEquals(1, result.exit, result.output);
            assertTrue(result.output.contains("UNAVAILABLE:"), result.output);
            assertFalse(result.output.contains("--add-exports"), result.output);
        }
    }

    @Test
    void warnContinuesAndFailStopsStartup() throws Exception {
        assumeTrue(JAVA >= 9);
        Result warn = child(Startup.class.getName(), null, strictWith("-Dchronicle.jvm.args.check=warn"));
        assertEquals(0, warn.exit, warn.output);
        assertTrue(warn.output.contains("DENIED:"), warn.output);
        assertTrue(warn.output.contains("READY "), warn.output);
        Result fail = child(Startup.class.getName(), null, strictWith("-Dchronicle.jvm.args.check=fail"));
        assertNotEquals(0, fail.exit, fail.output);
        assertTrue(fail.output.contains("DENIED:"), fail.output);
        assertFalse(fail.output.contains("READY "), fail.output);
    }

    @Test
    void failModeContinuesWhenCompilerWorks() throws Exception {
        Result result = JAVA == 8 ? child(Startup.class.getName(), null, "-Dchronicle.jvm.args.check=fail")
                : child(Startup.class.getName(), null, "-Dchronicle.jvm.args.check=fail", "--add-exports=" + EXPORT);
        assertEquals(0, result.exit, result.output);
        assertTrue(result.output.contains("READY "), result.output);
    }

    @Test
    void invalidModeDoesNotSilentlyDisableChecking() throws Exception {
        Result result = child(Startup.class.getName(), null, "-Dchronicle.jvm.args.check=typo");
        assertNotEquals(0, result.exit, result.output);
        assertTrue(result.output.contains("must be off, warn or fail"), result.output);
    }

    @Test
    void namedModuleIsExplicitlyUnavailable() throws Exception {
        assumeTrue(JAVA >= 9);
        Result result = child("NAMED", null);
        assertEquals(1, result.exit, result.output);
        assertTrue(result.output.contains("classpath deployment only"), result.output);
    }

    private static String[] strict() {
        return JAVA < 17 ? new String[]{"--illegal-access=deny"} : new String[0];
    }

    private static String[] strictWith(String mode) {
        List<String> flags = new ArrayList<>(Arrays.asList(strict()));
        flags.add(mode);
        return flags.toArray(new String[0]);
    }

    private static Result child(String main, String inheritedOptions, String... flags) throws Exception {
        Path directory = Files.createTempDirectory("compiler probe with spaces ");
        Path log = directory.resolve("result.log");
        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(Arrays.asList(flags));
        // Exclude slf4j-simple: diagnostics must work with no provider.
        StringBuilder cp = new StringBuilder();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (entry.contains("slf4j-simple")) continue;
            if (cp.length() > 0) cp.append(File.pathSeparator);
            cp.append(Paths.get(entry).toAbsolutePath());
        }
        // Put the CLI class in a jar whose path contains spaces; exercise launcher quoting.
        Path jar = directory.resolve("probe classes.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Automatic-Module-Name", "core.probe");
        String resource = "net/openhft/chronicle/core/JvmArgs.class";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar), manifest);
             InputStream in = JvmArgs.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in);
            out.putNextEntry(new JarEntry(resource));
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            out.closeEntry();
        }
        if ("NAMED".equals(main))
            command.addAll(Arrays.asList("--module-path", jar.toString(), "--module",
                    "core.probe/" + JvmArgs.class.getName(), "--check"));
        else
            command.addAll(Arrays.asList("-Dchronicle.analytics.disable=true", "-cp",
                    jar + File.pathSeparator + cp, main));
        if (main.equals(JvmArgs.class.getName())) command.add("--check");
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        for (String name : new String[]{"JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS"})
            builder.environment().remove(name);
        if (inheritedOptions != null) builder.environment().put("JDK_JAVA_OPTIONS", inheritedOptions);
        Process process = null;
        try {
            process = builder.start();
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "Compiler probe child timed out");
            return new Result(process.exitValue(), new String(Files.readAllBytes(log), StandardCharsets.UTF_8));
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(log);
            Files.deleteIfExists(jar);
            Files.deleteIfExists(directory);
        }
    }

    private static final class Result {
        final int exit;
        final String output;
        Result(int exit, String output) {
            this.exit = exit;
            this.output = output;
        }
    }
}
