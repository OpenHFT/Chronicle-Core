/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class JvmSafepointTest extends CoreTestCommon {

    @Test
    public void testSafepoint() throws InterruptedException {
        @SuppressWarnings("AnonymousHasLambdaAlternative")
        Thread t = new Thread() {
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < start + 1000
                        && !Thread.interrupted()) {
                    for (int i = 0; i < 10000; i++)
                        Jvm.safepoint();
                }
            }
        };
        t.start();
        int counter = 0;
        int min = 200;
        while (t.isAlive() && counter <= min) {
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 1) {
                String s0 = stackTrace[0].toString();
                String s1 = stackTrace[1].toString();
                if (s0.contains("safepoint") || s1.contains("safepoint"))
                    counter++;
                else if (t.isAlive() && !s0.contains("interrupted") && !s1.contains("interrupt"))
                    System.out.println(s0 + "\n" + s1);
            }
        }
        t.interrupt();
        t.join();
        System.out.println("counter: " + counter);
        assertTrue("counter: " + counter, counter >= min);
    }

    @Test
    public void safePointPerf() {
        // This will enable the C2 compiler to kick in.
        FlakyTestRunner.<RuntimeException>builder(this::safePointPerf0).withFlakyOnThisArchitecture(true).withMaxIterations(3).build().run();
    }

    public void safePointPerf0() {

        for (int t = 0; t <= 5; t++) {
            long start = System.nanoTime();

            int count = 10_000;
            for (int i = 0; i < count; i++)
                Jvm.safepoint();
            long time = System.nanoTime() - start;
            if (t > 2) {
                long avg = time / count;
                System.out.println("avg: " + avg);
                int maxAvg = Jvm.isArm() ? 400 : 200;
                try {
                    assertTrue("avg: " + avg, 1 <= avg && avg < maxAvg);
                    break;
                } catch (AssertionError e) {
                    if (t == 5)
                        throw e;
                }
            }
            Jvm.pause(5);
        }
    }
}
