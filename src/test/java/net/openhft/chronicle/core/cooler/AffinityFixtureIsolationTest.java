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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
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
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "affinity probe did not terminate");
            String output = new String(IOTools.readAsBytes(process.getInputStream()), StandardCharsets.UTF_8)
                    + new String(IOTools.readAsBytes(process.getErrorStream()), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), scenario + ": " + output);
        } finally {
            try {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(10, TimeUnit.SECONDS), "affinity probe did not stop");
                }
            } finally {
                Closeable.closeQuietly(process.getInputStream(), process.getErrorStream(), process.getOutputStream());
            }
        }
    }

    public static final class AffinityProbe {
        public static void main(String[] args) {
            BitSet original = (BitSet) Affinity.getAffinity().clone();
            assertFalse(original.isEmpty());
            try {
                switch (args[0]) {
                    case "earlyBaseline":
                        assertEquals(original, AffinityLock.BASE_AFFINITY);
                        new CpuCoolersTest().testAffinity();
                        break;
                    case "lateBaseline":
                        new CpuCoolersTest().testAffinity();
                        assertEquals(original, AffinityLock.BASE_AFFINITY);
                        break;
                    case "failingBody":
                        AssertionError expected = new AssertionError("fixture failed after pinning");
                        assertSame(expected, assertThrows(AssertionError.class,
                                () -> CpuCoolersTest.withRestoredAffinity(() -> {
                                    Affinity.setAffinity(original.nextSetBit(0));
                                    throw expected;
                                })));
                        assertEquals(original, AffinityLock.BASE_AFFINITY);
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
    }
}
