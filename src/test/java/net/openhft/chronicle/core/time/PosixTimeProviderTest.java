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
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.openhft.chronicle.core.time.SystemTimeProviderTest.assertBetween;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PosixTimeProviderTest extends CoreTestCommon {

    public static void main(String[] args) {
        for (ClockId value : ClockId.values()) {
            System.out.println(value + " " + PosixAPI.posix().clock_gettime(value));
        }
    }

    @Test
    public void currentTimeMicros() throws IllegalStateException {
        assumeFalse(OS.isMacOSX() || Jvm.isArm());
        AtomicBoolean ran = new AtomicBoolean();
        FlakyTestRunner.builder(() -> {
                    ran.set(true);
                    currentTimeMicros0();
                })
                .withMaxIterations(3)
                .build()
                .runOrThrow();
        assertTrue(ran.get(), "currentTimeMicros: executed");
    }

    private void currentTimeMicros0() {

        SystemTimeProviderTest.assertCurrentTimeMicros(PosixTimeProvider.INSTANCE, true, OS.isWindows());
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
            assertTrue(h.totalCount() > 0, "resolution: L85");
        }
    }
}
