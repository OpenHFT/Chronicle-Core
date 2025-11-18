/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.testframework.FlakyTestRunner;
import net.openhft.posix.ClockId;
import net.openhft.posix.PosixAPI;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static net.openhft.chronicle.core.time.SystemTimeProviderTest.assertBetween;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class PosixTimeProviderTest extends CoreTestCommon {

    public static void main(String[] args) {
        for (ClockId value : ClockId.values()) {
            System.out.println(value + " " + PosixAPI.posix().clock_gettime(value));
        }
    }

    @Test
    public void currentTimeMicros() throws IllegalStateException {
        assumeFalse(OS.isMacOSX() || Jvm.isArm());
        FlakyTestRunner.builder(this::currentTimeMicros0)
                .withMaxIterations(3)
                .build()
                .runOrThrow();
    }

    private void currentTimeMicros0() {

        @NotNull TimeProvider tp = PosixTimeProvider.INSTANCE;
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
                    System.out.println("min: " + minDiff);
                }
                if (maxDiff < diff) {
                    maxDiff = diff;
                    System.out.println("max: " + maxDiff);
                }
                long ns = System.nanoTime();
                while (System.nanoTime() < ns + 100)
                    Jvm.nanoPause();
                assertTrue(time2 >= lastTimeMicros);
                lastTimeMicros = time2;
            } while (System.currentTimeMillis() < start + 500);

            try {
                if (!OS.isWindows())
                    assertBetween(-5L * error, minDiff, 5L * error);
                assertBetween(990L, maxDiff, 1000L + 30L * error);
                break;
            } catch (AssertionError e) {
                // do nothing
            }
        }
        if (!OS.isWindows())
            assertBetween(-5L * error, minDiff, 5L * error);
        assertBetween(990L, maxDiff, 1000L + 30L * error);
    }

    @Test
    public void currentTime() throws IllegalStateException {
        assumeTrue(!OS.isMacOSX());
        TimeProvider tp = PosixTimeProvider.INSTANCE;
        for (int i = 3; i >= 0; i--) {
            long time2 = tp.currentTimeMillis();
            long time3 = tp.currentTimeMicros();
            long time4 = tp.currentTimeNanos();
            try {
                assertBetween(time3 / 1000 - 1, time2, time3 / 1000 + 20);
                assertBetween(time4 / 1000 - 100, time3, time4 / 1000 + 2_000);
            } catch (AssertionError ae) {
                Thread.yield();
                if (i == 0)
                    throw ae;
            }
        }
    }

    @Test
    public void resolution() {
        assumeTrue(!OS.isMacOSX());
        final PosixTimeProvider instance = PosixTimeProvider.INSTANCE;
        for (int j = 0; j < 3; j++) {
            Histogram h = new Histogram(32, 10, 1);
            long last = instance.currentTimeNanos();
            for (int i = 0; i < 5000000; i++) {
                long next = instance.currentTimeNanos();
                h.sampleNanos(next - last);
                Jvm.nanoPause();

                last = next;
            }
            System.out.println(h.toMicrosFormat());

            // Performance test
            assertTrue(h.totalCount() > 0);
        }
    }
}
