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
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

public class ChronicleInitTest extends CoreTestCommon {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStream() {
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStream() {
        System.setErr(originalErr);
    }

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
        assertEquals("dolor", Jvm.getProperty("lorem.ipsum"));

        // Normally enabled via system.properties file
        assertFalse(Jvm.isResourceTracing());
        assertTrue(Jvm.areOptionalSafepointsEnabled());
        assertEquals("false", Jvm.getProperty("jvm.safepoint.enabled"));
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
            assertEquals(1, process.waitFor());
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

    public static class ResourceTracingInit implements Runnable {
        @Override
        public void run() {
            System.err.println("disabling resource tracking");
            System.setProperty("jvm.resource.tracing", "false");
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
}
