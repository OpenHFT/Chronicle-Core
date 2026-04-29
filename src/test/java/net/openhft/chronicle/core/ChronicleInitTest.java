/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;
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
    void setUpStream() {
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStream() {
        System.setErr(originalErr);
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
    void initShouldNotThrowException() {
        assertDoesNotThrow(ChronicleInit::init, "Calling init should not throw an exception");
    }

    @Test
    void postInitShouldNotThrowException() {
        assertDoesNotThrow(ChronicleInit::postInit, "Calling postInit should not throw an exception");
    }

    @Test
    void shouldLoadServiceProviders() {
        ServiceLoader<ChronicleInitRunnable> runnableLoader = ServiceLoader.load(ChronicleInitRunnable.class);
        assertTrue(runnableLoader.iterator().hasNext(), "Service providers should be loaded");
    }

    @Test
    void testPositive() throws Exception {
        Process process = JavaProcessBuilder.create(ChronicleInitTest.class)
                .withJvmArguments("-Dchronicle.init.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    void testPostInitNegative() throws Exception {
        Process process = JavaProcessBuilder.create(ChronicleInitTest.class)
                .withJvmArguments("-Dchronicle.postinit.runnable=" + ResourceTracingInit.class.getName()).start();

        try {
            assertEquals(1, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    void testNoInit() throws Exception {
        Process process = JavaProcessBuilder.create(ChronicleInitTest.class).start();

        try {
            assertNotEquals(0, process.waitFor());
        } finally {
            JavaProcessBuilder.printProcessOutput("ChronicleInitTest", process);
        }
    }

    @Test
    void testBadClass() throws Exception {
        Process process = JavaProcessBuilder.create(ChronicleInitTest.class)
                .withJvmArguments("-Dchronicle.init.class=" + ChronicleInitTest.class.getName()).start();

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
