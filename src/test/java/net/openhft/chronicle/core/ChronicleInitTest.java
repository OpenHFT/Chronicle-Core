/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ServiceLoader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class ChronicleInitTest extends CoreTestCommon {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    private static JavaProcessBuilder builder(String... extraJvmArgs) {
        return JavaProcessBuilder.create(ChronicleInitTest.class)
                .withJvmArguments(extraJvmArgs);
    }

    private static JavaProcessBuilder builderWithTracingDisabled(String... extraJvmArgs) {
        String[] args = new String[extraJvmArgs.length + 1];
        args[0] = "-Djvm.resource.tracing=false";
        System.arraycopy(extraJvmArgs, 0, args, 1, extraJvmArgs.length);
        return JavaProcessBuilder.create(ChronicleInitTest.class)
                .withJvmArguments(args);
    }

    public static void main(String[] args) {
        // Service loader implementation
        if (!"dolor".equals(Jvm.getProperty("lorem.ipsum"))) {
            System.out.println("Service loader implementation did not run");
            System.exit(10);
        }
        // Normally enabled via system.properties file
        if (Jvm.isResourceTracing()) {
            System.out.println("Resource tracing is enabled");
            System.exit(11);
        }
        if (!Jvm.areOptionalSafepointsEnabled()) {
            System.out.println("Optional safepoints are not enabled");
            System.exit(12);
        }
        if (!"false".equals(Jvm.getProperty("jvm.resource.tracing"))) {
            System.out.println("Resource tracing was " + Jvm.getProperty("jvm.resource.tracing"));
            System.exit(13);
        }
    }

    @BeforeEach
    public void setUpStream() {
        try {
            System.setErr(new PrintStream(errContent, true, UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("ISO-8859-1 should always be supported", e);
        }
    }

    @AfterEach
    public void restoreStream() {
        System.setErr(originalErr);
    }

    @DisplayName("ChronicleInit class loads without exception behaviour under expected input and output conditions")
    @Test
    void initShouldNotThrowException() {
        assertDoesNotThrow(() -> Class.forName(ChronicleInit.class.getName()),
                "Loading ChronicleInit should not throw an exception");
    }

    @DisplayName("postInit completes without throwing exception behaviour under expected input and output conditions")
    @Test
    void postInitShouldNotThrowException() {
        assertDoesNotThrow(ChronicleInit::postInit, "Calling postInit should not throw an exception");
    }

    @DisplayName("ServiceLoader discovers ChronicleInitRunnable providers behaviour under expected input and output conditions")
    @Test
    void shouldLoadServiceProviders() {
        ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);
        assertTrue(runnableLoader.iterator().hasNext(), "Service providers should be loaded");
    }

    @DisplayName("process exits when init runnable disables tracing")
    @Test
    void testPositive() throws Exception {
        Process process = builderWithTracingDisabled("-Dchronicle.init.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(0, process.waitFor(), "process should exit successfully when init runnable disables resource tracing");
            String stdout = JavaProcessBuilder.getProcessStdOut(process);
            assertTrue(stdout.contains("disabling resource tracking"),
                    "stdout should contain \"disabling resource tracking\": " + stdout);
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits when postInit enables tracing")
    @Test
    void testPostInitNegative() throws Exception {
        Process process = builder("-Dchronicle.postinit.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(11, process.waitFor(), "process should exit with code 11 when resource tracing is enabled after postInit");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits 10 when postInit overrides property")
    @Test
    void testExitCode10WhenServiceLoaderPropertyOverridden() throws Exception {
        Process process = builder("-Dchronicle.postinit.runnable=" + PostInitOverridesLoremIpsum.class.getName()).start();

        try {
            assertEquals(10, process.waitFor(), "process should exit with code 10 when postInit overrides service loader property");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits 12 when safepoints disabled")
    @Test
    void testExitCode12WhenOptionalSafepointsDisabled() throws Exception {
        Process process = builder(
                "-Djvm.resource.tracing=false",
                "-Djvm.safepoint.enabled=false",
                "-Dlorem.ipsum=dolor").start();

        try {
            assertEquals(12, process.waitFor(), "process should exit with code 12 when optional safepoints are disabled");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits 13 when tracing flag differs")
    @Test
    void testExitCode13WhenResourceTracingPropertyDiffersFromFlag() throws Exception {
        Process process = builder(
                "-Djvm.resource.tracing=false",
                "-Dchronicle.postinit.runnable=" + PostInitEnablesResourceTracingProperty.class.getName(),
                "-Dlorem.ipsum=dolor").start();

        try {
            assertEquals(13, process.waitFor(), "process should exit with code 13 when resource tracing property differs from flag");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits nonzero without init class")
    @Test
    void testNoInit() throws Exception {
        Process process = builder().start();

        try {
            assertNotEquals(0, process.waitFor(), "process should exit non-zero when no init class is configured");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("process exits nonzero for invalid init class")
    @Test
    void testBadClass() throws Exception {
        Process process = builder("-Dchronicle.init.class=" + ChronicleInitTest.class.getName()).start();

        try {
            assertNotEquals(0, process.waitFor(), "process should exit non-zero for invalid init class");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @DisplayName("command line override disables resource tracing")
    @Test
    void testCommandLineOverride() throws Exception {
        Process process = builderWithTracingDisabled().start();

        try {
            assertEquals(0, process.waitFor(), "process should exit successfully when command line overrides system properties");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    public static class ResourceTracingInit implements Runnable {
        @Override
        public void run() {
            System.err.println("disabling resource tracking");
            System.setProperty("jvm.resource.tracing", "false");
        }
    }

    public static class PostInitOverridesLoremIpsum implements Runnable {
        @Override
        public void run() {
            System.setProperty("lorem.ipsum", "sit");
        }
    }

    public static class ServiceLoaderInit implements ChronicleInitRunnable {
        @Override
        public void run() {
            System.setProperty("lorem.ipsum", "dolor");
        }

        @Override
        public void postInit() {
            System.setProperty("jvm.safepoint.enabled", "false");
        }
    }

    public static class PostInitEnablesResourceTracingProperty implements Runnable {
        @Override
        public void run() {
            System.setProperty("jvm.resource.tracing", "true");
        }
    }
}
