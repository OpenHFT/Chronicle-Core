/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.cooler;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.testframework.process.JavaProcessBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AffinityFixtureIsolationTest {
    @ParameterizedTest
    @ValueSource(strings = {"earlyBaseline", "lateBaseline", "failingBody", "restrictedMask"})
    void restoresAffinityInFreshJvm(String scenario) throws Exception {
        assumeFalse(OS.isMacOSX(), "macOS does not support thread affinity");
        assumeTrue(Runtime.getRuntime().availableProcessors() >= 2);
        Process process = JavaProcessBuilder.create(AffinityProbe.class)
                .withProgramArguments(scenario).start();
        String output = awaitProbe(process);
        assertEquals(0, process.exitValue(), scenario + ": " + output);
    }

    @Test
    @Timeout(10)
    void capturesLargeFailureOutputWithoutBlockingTheChild() throws Exception {
        Process process = JavaProcessBuilder.create(NoisyFailureProbe.class).start();
        String output = awaitProbe(process);
        assertNotEquals(0, process.exitValue());
        assertTrue(output.contains("stdout complete"), "Missing stdout completion marker");
        assertTrue(output.contains("stderr complete"), "Missing stderr completion marker");
        assertTrue(output.contains("noisy probe assertion"), "Missing child assertion message");
    }

    private static String awaitProbe(Process process) throws Exception {
        ExecutorService readers = Executors.newFixedThreadPool(2, task -> {
            Thread thread = new Thread(task, "affinity-probe-output");
            thread.setDaemon(true);
            return thread;
        });
        // Drain both pipes to EOF while the child runs. Windows stderr is a FileInputStream:
        // buffer it to avoid readAsBytes' available()-sized regular-file read.
        Future<byte[]> stdout = readers.submit(
                () -> IOTools.readAsBytes(new BufferedInputStream(process.getInputStream())));
        Future<byte[]> stderr = readers.submit(
                () -> IOTools.readAsBytes(new BufferedInputStream(process.getErrorStream())));
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "affinity probe did not terminate");
            return new String(stdout.get(10, TimeUnit.SECONDS), StandardCharsets.UTF_8)
                    + new String(stderr.get(10, TimeUnit.SECONDS), StandardCharsets.UTF_8);
        } finally {
            try {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(10, TimeUnit.SECONDS), "affinity probe did not stop");
                }
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
                readers.shutdownNow();
                assertTrue(readers.awaitTermination(10, TimeUnit.SECONDS), "probe output readers did not stop");
            }
        }
    }

    public static final class NoisyFailureProbe {
        public static void main(String[] args) throws Exception {
            byte[] output = new byte[64 * 1024];
            Arrays.fill(output, (byte) 'x');
            for (int i = 0; i < 16; i++) {
                System.out.write(output);
                System.err.write(output);
            }
            System.out.println("stdout complete");
            System.err.println("stderr complete");
            throw new AssertionError("noisy probe assertion");
        }
    }

    public static final class AffinityProbe {
        public static void main(String[] args) {
            BitSet original = (BitSet) Affinity.getAffinity().clone();
            assertFalse(original.isEmpty());
            try {
                switch (args[0]) {
                    case "earlyBaseline":
                        assertBaselineAffinity(original);
                        new CpuCoolersTest().testAffinity();
                        break;
                    case "lateBaseline":
                        new CpuCoolersTest().testAffinity();
                        assertBaselineAffinity(original);
                        break;
                    case "failingBody":
                        AssertionError expected = new AssertionError("fixture failed after pinning");
                        assertSame(expected, assertThrows(AssertionError.class,
                                () -> CpuCoolersTest.withRestoredAffinity(() -> {
                                    Affinity.setAffinity(original.nextSetBit(0));
                                    throw expected;
                                })));
                        assertBaselineAffinity(original);
                        break;
                    case "restrictedMask":
                        // Choose from the supported mask; CPU 1 need not be available.
                        int cpu = original.nextSetBit(0);
                        Affinity.setAffinity(cpu);
                        BitSet restricted = (BitSet) Affinity.getAffinity().clone();
                        CpuCoolersTest.withRestoredAffinity(() -> Affinity.setAffinity(original));
                        assertEquals(restricted, Affinity.getAffinity());
                        Affinity.setAffinity(original);
                        break;
                    default:
                        throw new AssertionError(args[0]);
                }
                assertEquals(original, Affinity.getAffinity(), "Fixture contaminated its caller");
            } finally {
                Affinity.setAffinity(original);
            }
        }

        private static void assertBaselineAffinity(BitSet expectedAffinity) {
            // Initialise AffinityLock only when the scenario is ready to inspect its baseline.
            BitSet actualBaseline = AffinityLock.BASE_AFFINITY;
            assertEquals(expectedAffinity, actualBaseline, "AffinityLock captured a contaminated baseline");
        }
    }
}
