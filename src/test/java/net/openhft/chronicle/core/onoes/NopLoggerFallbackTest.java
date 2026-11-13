/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.File;
import java.lang.reflect.Proxy;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class NopLoggerFallbackTest {
    /** An explicitly installed NOP provider, discovered only in the child fixture jar. */
    public static class Provider implements SLF4JServiceProvider {
        public ILoggerFactory getLoggerFactory() { return new NOPLoggerFactory(); }
        public IMarkerFactory getMarkerFactory() { return new BasicMarkerFactory(); }
        public MDCAdapter getMDCAdapter() { return new NOPMDCAdapter(); }
        public String getRequestedApiVersion() { return "2.0.99"; }
        public void initialize() { /* Stateless provider: nothing to initialise. */ }
    }

    public static class Disposable { }

    public static class Events {
        // GC has no completion signal; bounded polling tests eventual class-loader reclamation.
        @SuppressWarnings("java:S2925")
        public static void main(String[] args) throws Exception {
            if ("unload".equals(args[0])) {
                WeakReference<ClassLoader> reference = logFromDisposableLoader();
                for (int i = 0; i < 100 && reference.get() != null; i++) {
                    System.gc();
                    Thread.sleep(20);
                }
                if (reference.get() != null) throw new AssertionError("Source loader retained");
                System.out.println("DONE");
                return;
            }
            if ("jvm".equals(args[0])) {
                Jvm.warn().on(Events.class, "FALLBACK_TEST_JVM_WARN");
                Jvm.error().on(Events.class, "FALLBACK_TEST_JVM_ERROR");
                System.out.println("DONE");
                return;
            }
            if ("custom".equals(args[0])) {
                AtomicInteger calls = new AtomicInteger();
                Jvm.setWarnExceptionHandler((logger, message, thrown) -> calls.incrementAndGet());
                Jvm.warn().on(Events.class, "FALLBACK_TEST_CUSTOM");
                System.out.println("CUSTOM=" + calls.get());
                return;
            }
            if ("throwing".equals(args[0])) {
                Logger bad = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class[]{Logger.class},
                        (proxy, method, arguments) -> {
                            if ("getName".equals(method.getName())) return "broken-provider";
                            throw new IllegalStateException("FALLBACK_TEST_LOGGER_FAILURE");
                        });
                Slf4jExceptionHandler.ERROR.on(bad, "FALLBACK_TEST_ORIGINAL", new IllegalArgumentException("FALLBACK_TEST_CAUSE"));
            } else {
                Slf4jExceptionHandler.WARN.on(Events.class, "FALLBACK_TEST_WARNING");
                Slf4jExceptionHandler.ERROR.on(LoggerFactory.getLogger(Events.class), "FALLBACK_TEST_ERROR",
                        new IllegalStateException("FALLBACK_TEST_STACK", new IllegalArgumentException("FALLBACK_TEST_NESTED")));
                Slf4jExceptionHandler.PERF.on(Events.class, "FALLBACK_TEST_PERF");
                Slf4jExceptionHandler.DEBUG.on(Events.class, "FALLBACK_TEST_DEBUG");
                List<Thread> threads = new ArrayList<>();
                for (int t = 0; t < 4; t++) {
                    final int thread = t;
                    Thread worker = new Thread(() -> {
                        for (int i = 0; i < 10; i++)
                            Slf4jExceptionHandler.WARN.on(Events.class, "FALLBACK_TEST_CONCURRENT_" + thread + "_" + i);
                    });
                    threads.add(worker);
                    worker.start();
                }
                for (Thread thread : threads) thread.join();
            }
            System.out.println("DONE");
        }

        private static WeakReference<ClassLoader> logFromDisposableLoader() throws Exception {
            try (URLClassLoader loader = new URLClassLoader(new URL[]{Events.class.getProtectionDomain().getCodeSource().getLocation()}, null)) {
                Class<?> source = loader.loadClass(Disposable.class.getName());
                Slf4jExceptionHandler.WARN.on(source, "FALLBACK_TEST_DISPOSABLE");
                return new WeakReference<>(loader);
            }
        }
    }

    @Test
    void missingProviderIsSilentWhenFallbackDisabled() throws Exception {
        Result result = run("missing", "true", "warn", "events");
        assertFalse(result.err.contains("FALLBACK_TEST_"), result.err);
        assertFalse(result.err.contains("Chronicle stderr fallback enabled"), result.err);
    }

    @Test
    void defaultAndExplicitFalsePreserveEveryEvent() throws Exception {
        for (String provider : new String[]{"missing", "nop"})
            for (String disabled : new String[]{null, "false"}) {
                Result result = run(provider, disabled, "warn", "events");
                assertEquals(1, occurrences(result.err, "Chronicle stderr fallback enabled"), result.err);
                assertEquals(1, occurrences(result.err, "FALLBACK_TEST_WARNING"), result.err);
                assertEquals(1, occurrences(result.err, "FALLBACK_TEST_ERROR"), result.err);
                assertTrue(result.err.contains("[WARN] " + Events.class.getName()), result.err);
                assertTrue(result.err.contains("FALLBACK_TEST_STACK"), result.err);
                assertTrue(result.err.contains("Caused by: java.lang.IllegalArgumentException: FALLBACK_TEST_NESTED"), result.err);
                for (int t = 0; t < 4; t++)
                    for (int i = 0; i < 10; i++)
                        assertEquals(1, occurrences(result.err, "FALLBACK_TEST_CONCURRENT_" + t + "_" + i + System.lineSeparator()), result.err);
                assertFalse(result.err.contains("FALLBACK_TEST_PERF"), result.err);
                assertFalse(result.err.contains("FALLBACK_TEST_DEBUG"), result.err);
            }
    }

    @Test
    void explicitNopProviderIsRespectedWhenFallbackDisabled() throws Exception {
        for (String provider : new String[]{"missing", "nop"})
            for (String disabled : new String[]{"", "true", "yes", "T", "Y", " TRUE "}) {
                Result result = run(provider, disabled, "warn", "events");
                assertFalse(result.err.contains("FALLBACK_TEST_"), result.err);
                assertFalse(result.err.contains("Chronicle stderr fallback enabled"), result.err);
            }
    }

    @Test
    void realProviderIsNotDuplicatedOrBypassed() throws Exception {
        Result enabled = run("simple", null, "warn", "events");
        assertEquals(1, occurrences(enabled.err, "FALLBACK_TEST_WARNING"), enabled.err);
        assertEquals(1, occurrences(enabled.err, "FALLBACK_TEST_ERROR"), enabled.err);
        assertFalse(enabled.err.contains("Chronicle stderr fallback enabled"), enabled.err);
        Result filtered = run("simple", null, "off", "events");
        assertFalse(filtered.err.contains("FALLBACK_TEST_"), filtered.err);
        assertFalse(filtered.err.contains("Chronicle stderr fallback enabled"), filtered.err);
    }

    @Test
    void applicationHandlerAndThrowingLoggerKeepTheirBehaviour() throws Exception {
        Result custom = run("missing", null, "warn", "custom");
        assertFalse(custom.err.contains("FALLBACK_TEST_CUSTOM"), custom.err);
        Result throwing = run("missing", null, "warn", "throwing");
        assertTrue(throwing.err.contains("FALLBACK_TEST_ORIGINAL"), throwing.err);
        assertTrue(throwing.err.contains("FALLBACK_TEST_CAUSE"), throwing.err);
        assertTrue(throwing.err.contains("FALLBACK_TEST_LOGGER_FAILURE"), throwing.err);
        assertFalse(throwing.err.contains("Chronicle stderr fallback enabled"), throwing.err);
    }

    @Test
    void defaultJvmHandlersUseFallbackWithoutRetainingSourceLoaders() throws Exception {
        Result jvm = run("missing", null, "warn", "jvm");
        assertEquals(1, occurrences(jvm.err, "FALLBACK_TEST_JVM_WARN"), jvm.err);
        assertEquals(1, occurrences(jvm.err, "FALLBACK_TEST_JVM_ERROR"), jvm.err);
        Result unload = run("missing", null, "warn", "unload");
        assertTrue(unload.err.contains("FALLBACK_TEST_DISPOSABLE"), unload.err);
    }

    private static int occurrences(String text, String token) {
        int count = 0;
        for (int p = 0; (p = text.indexOf(token, p)) >= 0; p += token.length()) count++;
        return count;
    }

    private static Result run(String provider, String disabled, String level, String mode) throws Exception {
        Path directory = Files.createTempDirectory("nop logger fallback ");
        Path stdout = directory.resolve("stdout.log"), stderr = directory.resolve("stderr.log"), jar = directory.resolve("provider.jar");
        StringBuilder cp = new StringBuilder();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!"simple".equals(provider) && entry.contains("slf4j-simple")) continue;
            if (cp.length() > 0) cp.append(File.pathSeparator);
            cp.append(Paths.get(entry).toAbsolutePath());
        }
        if ("nop".equals(provider)) {
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
                out.putNextEntry(new JarEntry("META-INF/services/org.slf4j.spi.SLF4JServiceProvider"));
                out.write(Provider.class.getName().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
            cp.append(File.pathSeparator).append(jar);
        }
        List<String> command = new java.util.ArrayList<>(Arrays.asList(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dchronicle.analytics.disable=true",
                "-Dorg.slf4j.simpleLogger.defaultLogLevel=" + level,
                "-cp", cp.toString(), Events.class.getName(), mode));
        if (disabled != null)
            command.add(1, "-Dchronicle.logging.disableStderrFallback" + (disabled.isEmpty() ? "" : "=" + disabled));
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectOutput(stdout.toFile()).redirectError(stderr.toFile());
        for (String key : new String[]{"JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS"}) builder.environment().remove(key);
        Process process = null;
        try {
            process = builder.start();
            assertTrue(process.waitFor(60, TimeUnit.SECONDS), "child timeout");
            String err = new String(Files.readAllBytes(stderr), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), err);
            String out = new String(Files.readAllBytes(stdout), StandardCharsets.UTF_8).trim();
            assertEquals("custom".equals(mode) ? "CUSTOM=1" : "DONE", out, "stdout must contain only application output");
            return new Result(err);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
            Files.deleteIfExists(jar);
            Files.deleteIfExists(directory);
        }
    }

    private static class Result {
        final String err;
        Result(String err) { this.err = err; }
    }
}
