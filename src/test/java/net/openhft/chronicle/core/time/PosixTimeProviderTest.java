/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.testframework.FlakyTestRunner;
import net.openhft.posix.ClockId;
import net.openhft.posix.PosixAPI;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class PosixTimeProviderTest {

    @Test
    public void currentTimeMicros() throws IllegalStateException {
        assumeTrue(!OS.isMacOSX());
        FlakyTestRunner.builder(this::currentTimeMicros0)
                .withMaxIterations(3)
                .build()
                .runOrThrow();
    }

    public void currentTimeMicros0() {

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
                    assertBetween(-5 * error, minDiff, 5 * error);
                assertBetween(990, maxDiff, 1000 + 30 * error);
                break;
            } catch (AssertionError e) {
                // do nothing
            }
        }
        if (!OS.isWindows())
            assertBetween(-5 * error, minDiff, 5 * error);
        assertBetween(990, maxDiff, 1000 + 30 * error);
    }

    static void assertBetween(long min, long actual, long max) {
        if (min <= actual && actual <= max)
            return;
        throw new AssertionError("Not in range " + min + " <= " + actual + " <= " + max);
    }

    @Test
    public void currentTime() throws IllegalStateException {
        assumeTrue(!OS.isMacOSX());
        TimeProvider tp = PosixTimeProvider.INSTANCE;
        for (int i = 3; i >= 0; i--) {
            long time1 = tp.currentTime(TimeUnit.SECONDS);
            long time2 = tp.currentTimeMillis();
            long time3 = tp.currentTimeMicros();
            long time4 = tp.currentTimeNanos();
            try {
                assertBetween(time2 / 1000, time1, time2 / 1000 + 2);
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


    public static void main(String[] args) {
        for (ClockId value : ClockId.values()) {
            System.out.println(value + " " + PosixAPI.posix().clock_gettime(value));
        }
    }
}