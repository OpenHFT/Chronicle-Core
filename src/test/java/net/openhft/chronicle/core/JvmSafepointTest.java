/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JvmSafepointTest extends CoreTestCommon {

    private volatile long safePointPerfLastAvg;

    @DisplayName("Safepoint stack traces report expected hits")
    @Test
    void testSafepoint() throws InterruptedException {
        @SuppressWarnings("AnonymousHasLambdaAlternative")
        Thread t = new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 1000
                        && !Thread.interrupted()) {
                    for (int i = 0; i < 1000; i++)
                        Jvm.safepoint();
                }
            }
        };
        t.start();
        int counter = 0;
        int min = Jvm.isAzulZing() ? 2 : 200;
        do {
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 1) {
                String s0 = stackTrace[0].toString();
                String s1 = stackTrace[1].toString();
                if (s0.contains("safepoint") || s1.contains("safepoint"))
                    counter++;
                else if (!s0.contains("interrupted") && !s1.contains("interrupt"))
                    System.out.println(s0 + "\n" + s1);
            }
        } while (t.isAlive() && counter <= min);
        if (!t.isAlive()) {
            System.out.println("Thread has died unexpectedly. Quitting...");
            System.out.println("counter: " + counter);
            return;
        }
        t.interrupt();
        t.join();
        System.out.println("counter: " + counter);
        assertTrue(counter >= min, "safepoint stack trace hits should reach minimum: counter=" + counter + ", min=" + min);
    }

    @DisplayName("Safepoint performance stays within expected bounds")
    @Test
    void safePointPerf() {
        // This will enable the C2 compiler to kick in.
        FlakyTestRunner.builder(this::safePointPerf0)
                .withFlakyOnThisArchitecture(true)
                .withMaxIterations(3)
                .build()
                .runOrThrow();
        int maxAvg = Jvm.isArm() ? 400 : 200;
        long avg = safePointPerfLastAvg;
        assertTrue(1 <= avg && avg < maxAvg, "safepoint average should be within bounds: avg=" + avg + ", maxAvg=" + maxAvg);
    }

    private void safePointPerf0() {

        for (int t = 0; t <= 5; t++) {
            long start = System.nanoTime();

            int count = 10_000;
            for (int i = 0; i < count; i++)
                Jvm.safepoint();
            long time = System.nanoTime() - start;
            if (t > 2) {
                long avg = time / count;
                safePointPerfLastAvg = avg;
                System.out.println("avg: " + avg);
                int maxAvg = Jvm.isArm() ? 400 : 200;
                if (1 <= avg && avg < maxAvg) {
                    break;
                }
                if (t == 5) {
                    fail("avg: " + avg + " at t=" + t);
                }
            }
            Jvm.pause(5);
        }
    }
}
