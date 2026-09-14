/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IOToolsLocaleInterruptionTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void firstNonEnglishLookupPreservesInterruption(boolean initiallyInterrupted) throws Exception {
        // Language initialisation is single-use; isolate both entry states from other tests and locales.
        Process process = JavaProcessBuilder.create(LocaleProbe.class)
                .withJvmArguments("-Duser.language=fr", "-Duser.country=FR", "-ea")
                .withProgramArguments(Boolean.toString(initiallyInterrupted))
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "locale probe did not terminate");
            String output = new String(IOTools.readAsBytes(process.getInputStream()), StandardCharsets.UTF_8)
                    + new String(IOTools.readAsBytes(process.getErrorStream()), StandardCharsets.UTF_8);
            assumeTrue(process.exitValue() != 77, "loopback networking unavailable: " + output);
            assertEquals(0, process.exitValue(), output);
        } finally {
            try {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(10, TimeUnit.SECONDS), "locale probe did not stop");
                }
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
            }
        }
    }

    public static final class LocaleProbe {
        public static void main(String[] args) throws Exception {
            assertEquals("fr", Locale.getDefault().getLanguage());
            try (ServerSocket server = new ServerSocket(0)) {
                assertTrue(server.getLocalPort() > 0);
            } catch (IOException unavailable) {
                System.err.println(unavailable);
                System.exit(77);
                return;
            }
            // Warm unrelated bootstrap code without initialising IOTools.Language.
            Jvm.isDebug();
            IOTools.tempName("locale-probe");
            boolean initiallyInterrupted = Boolean.parseBoolean(args[0]);
            if (initiallyInterrupted)
                Thread.currentThread().interrupt();
            boolean learntChannelMessage = IOTools.isClosedException(new IOException());
            boolean statusOnReturn = Thread.interrupted();
            boolean lateInterrupt = false;
            // Observe any worker from the old implementation too, so merely clearing a flag cannot hide a late delivery.
            for (Thread worker : Thread.getAllStackTraces().keySet()) {
                if (!worker.getName().equals("close~3") && !worker.getName().equals("close~4")
                        && !worker.getName().equals("socket-probe-close"))
                    continue;
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (worker.isAlive() && System.nanoTime() < deadline) {
                    try {
                        worker.join(100);
                    } catch (InterruptedException unexpected) {
                        lateInterrupt = true;
                    }
                }
                assertFalse(worker.isAlive(), "probe worker did not finish");
            }
            assertEquals(initiallyInterrupted, statusOnReturn, "lookup changed the caller's interrupt status");
            assertFalse(lateInterrupt || Thread.interrupted(), "probe worker delivered a late interrupt");
            assertTrue(learntChannelMessage, "initial interruption aborted the regional socket probes");
        }
    }
}
