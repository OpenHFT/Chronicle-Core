/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ServiceLoader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class ChronicleInitTest extends CoreTestCommon {

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

    @Test
    public void initShouldNotThrowException() {
        assertDoesNotThrow(() -> Class.forName(ChronicleInit.class.getName()),
                "Loading ChronicleInit should not throw an exception");
    }

    @Test
    public void postInitShouldNotThrowException() {
        assertDoesNotThrow(ChronicleInit::postInit, "Calling postInit should not throw an exception");
    }

    @Test
    public void shouldLoadServiceProviders() {
        ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);
        assertTrue(runnableLoader.iterator().hasNext(), "Service providers should be loaded");
    }

    @Test
    public void testPositive() throws Exception {
        Process process = builderWithTracingDisabled("-Dchronicle.init.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(0, process.waitFor());
            String stdout = JavaProcessBuilder.getProcessStdOut(process);
            assertTrue(stdout.contains("disabling resource tracking"), "Init runnable should execute");
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testPostInitNegative() throws Exception {
        Process process = builder("-Dchronicle.postinit.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(11, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testExitCode10WhenServiceLoaderPropertyOverridden() throws Exception {
        Process process = builder("-Dchronicle.postinit.runnable=" + PostInitOverridesLoremIpsum.class.getName()).start();

        try {
            assertEquals(10, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testExitCode12WhenOptionalSafepointsDisabled() throws Exception {
        Process process = builder(
                "-Djvm.resource.tracing=false",
                "-Djvm.safepoint.enabled=false",
                "-Dlorem.ipsum=dolor").start();

        try {
            assertEquals(12, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testExitCode13WhenResourceTracingPropertyDiffersFromFlag() throws Exception {
        Process process = builder(
                "-Djvm.resource.tracing=false",
                "-Dchronicle.postinit.runnable=" + PostInitEnablesResourceTracingProperty.class.getName(),
                "-Dlorem.ipsum=dolor").start();

        try {
            assertEquals(13, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testNoInit() throws Exception {
        Process process = builder().start();

        try {
            assertNotEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testBadClass() throws Exception {
        Process process = builder("-Dchronicle.init.class=" + ChronicleInitTest.class.getName()).start();

        try {
            assertNotEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    public void testCommandLineOverride() throws Exception {
        Process process = builderWithTracingDisabled().start();

        try {
            assertEquals(0, process.waitFor());
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
