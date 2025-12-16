/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemTimeProviderTest extends CoreTestCommon {
    static void assertBetween(long min, long actual, long max) {
        assertBetween("range check", min, actual, max);
    }

    static void assertBetween(String label, long min, long actual, long max) {
        if (min <= actual && actual <= max)
            return;
        throw new AssertionError(label + ": Not in range " + min + " <= " + actual + " <= " + max);
    }

    @Test
    public void currentTimeMicros() throws IllegalStateException {
        // doCurrentTimeMicros() is very flaky so that is why we retry this operation
        boolean success = false;
        Throwable lastFailure = null;
        for (int i = 0; i < 3; i++) {
            try {
                FlakyTestRunner.builder(this::doCurrentTimeMicros)
                        .withFlakyOnThisArchitecture(Jvm.isArm() || OS.isWindows() || OS.isMacOSX()).build().run();
                success = true;
                break;
            } catch (Throwable t) {
                lastFailure = t;
                System.out.println("Trying to deflake flaky test: " + i);
                int jitter = ThreadLocalRandom.current().nextInt(500);
                Jvm.pause(500 + jitter);
            }
        }
        if (!success)
            throw new AssertionError("currentTimeMicros: failed after retries", lastFailure);
        assertTrue(success, "currentTimeMicros: succeeded after retries");
    }

    private void doCurrentTimeMicros() throws IllegalStateException {
        assertCurrentTimeMicros(SystemTimeProvider.INSTANCE, false, false);
    }

    @Test
    public void currentTime() throws IllegalStateException {
        for (int i = 3; i >= 0; i--) {
            TimeProvider tp = SystemTimeProvider.INSTANCE;
            long time2 = tp.currentTimeMillis();
            long time3 = tp.currentTimeMicros();
            long time4 = tp.currentTimeNanos();
            try {
                assertBetween("currentTimeMillis within currentTimeMicros bounds", time3 / 1000 - 8, time2, time3 / 1000 + 20);
                assertBetween("currentTimeMicros within currentTimeNanos bounds", time4 / 1000 - 100, time3, time4 / 1000 + 2_000);
            } catch (AssertionError ae) {
                Thread.yield();
                if (i == 0)
                    throw ae;
            }
        }
    }

    @Test
    public void resolution() {
        for (int j = 0; j < 3; j++) {
            Histogram h = new Histogram(32, 10, 1);
            long last = SystemTimeProvider.INSTANCE.currentTimeNanos();
            for (int i = 0; i < 5000000; i++) {
                long next = SystemTimeProvider.INSTANCE.currentTimeNanos();
                h.sampleNanos(next - last);
                Jvm.nanoPause();

                last = next;
            }
            System.out.println(h.toMicrosFormat());

            // Performance test
            assertTrue(h.totalCount() > 0, "histogram should record samples");
        }
    }

    static void assertCurrentTimeMicros(TimeProvider tp, boolean logExtremes, boolean skipWindowsMinCheck) {
        long minDiff = 0;
        long maxDiff = 0;
        long lastTimeMicros;
        long start;

        int error = OS.isWindows() || Jvm.isArm() ? 12 : 1;
        for (int i = 0; i <= 20; i++) {
            minDiff = 10;
            maxDiff = 995;
            lastTimeMicros = 0;
            start = System.currentTimeMillis();

            do {
                long now0 = tp.currentTimeMillis();
                long time2 = tp.currentTimeMicros();
                long now1 = tp.currentTimeMillis();
                if (now1 - now0 > 1) {
                    System.out.println("jump: " + (now1 - now0));
                    continue;
                }

                long now = now1 * 1000;
                long diff = time2 - now;
                if (minDiff > diff) {
                    minDiff = diff;
                    if (logExtremes) {
                        System.out.println("min: " + minDiff);
                    }
                }
                if (maxDiff < diff) {
                    maxDiff = diff;
                    if (logExtremes) {
                        System.out.println("max: " + maxDiff);
                    }
                }
                long ns = System.nanoTime();
                while (System.nanoTime() < ns + 100)
                    Jvm.nanoPause();
                assertTrue(time2 >= lastTimeMicros, "currentTimeMicros should be monotonic");
                lastTimeMicros = time2;
            } while (System.currentTimeMillis() < start + 500);

            try {
                if (!skipWindowsMinCheck) {
                    assertBetween("minDiff lower bound", -5L * error, minDiff, 5L * error);
                }
                assertBetween("maxDiff upper bound", 990L, maxDiff, 1000L + 30L * error);
                break;
            } catch (AssertionError e) {
                // retry
            }
        }
        if (!skipWindowsMinCheck) {
            assertBetween("minDiff lower bound", -5L * error, minDiff, 5L * error);
        }
        assertBetween("maxDiff upper bound", 990L, maxDiff, 1000L + 30L * error);
    }
}
