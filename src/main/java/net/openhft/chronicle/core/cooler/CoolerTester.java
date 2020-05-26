/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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
package net.openhft.chronicle.core.cooler;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.Histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public class CoolerTester {
    static Object blackhole;
    private final List<CpuCooler> disturbers = new ArrayList<>();
    private final List<Histogram> histograms = new ArrayList<>();
    private final List<String> testNames = new ArrayList<>();
    private final List<Callable> tests = new ArrayList<>();
    private int repeat = 10;
    private int runTimeMS = 5_000;
    private int minCount = 20;
    private int maxCount = 20_000;

    public CoolerTester(Callable tested, CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
        this.testNames.add("");
        this.tests.add(tested);
    }

    public CoolerTester(CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
    }

    public CoolerTester(CpuCooler disturber, Callable... tests) {
        this.disturbers.add(disturber);
        Collections.addAll(this.tests, tests);
    }

    static void innerloop0(Callable tested, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount * 10));
    }

    static void innerloop1(Callable tested, CpuCooler disturber, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            disturber.disturb();
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount));
    }

    private static void innerLoop2(Callable tested, Histogram histogram) throws Exception {
        UNSAFE.fullFence();
        long start0 = System.nanoTime();
        blackhole = tested.call();
//            UNSAFE.fullFence();
        long time0 = System.nanoTime() - start0;
        histogram.sample(time0);
    }

    public CoolerTester add(String name, Callable test) {
        testNames.add(name);
        tests.add(test);
        return this;
    }

    public int repeat() {
        return repeat;
    }

    public CoolerTester repeat(int repeat) {
        this.repeat = repeat;
        return this;
    }

    public int runTimeMS() {
        return runTimeMS;
    }

    public CoolerTester runTimeMS(int runTimeMS) {
        this.runTimeMS = runTimeMS;
        return this;
    }

    public int minCount() {
        return minCount;
    }

    public CoolerTester minCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int maxCount() {
        return maxCount;
    }

    public CoolerTester maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public void run() {
        try {
            System.out.println("---- Warmup ----");
            for (int j = 0; j < tests.size(); j++) {
                for (int i = 0; i < disturbers.size(); i++) {
                    histograms.add(new Histogram(32, 7));
                }
            }
            for (int t = 0; t <= repeat; t++) {
                if (t == 1)
                    System.out.println("\n---- Real Tests ----");
                if (t == repeat)
                    System.out.println("\n---- RESULTS ----\n");
                for (int j = 0; j < tests.size(); j++) {
                    Callable tested = tests.get(j);
                    for (int i = 0; i < disturbers.size(); i++) {
                        CpuCooler disturber = disturbers.get(i);
                        Histogram histogram = histograms.get(j * disturbers.size() + i);

                        long start = System.currentTimeMillis();
                        int count = 0;
                        if (t > 0)
                            innerloop1(tested, disturber, histogram, start, count, minCount, runTimeMS, maxCount);
                        else
                            innerloop0(tested, histogram, start, count, minCount, runTimeMS, maxCount);
                        if (tests.size() > 1)
                            System.out.print(testNames.get(j) + " ");
                        System.out.print(disturber);
                        System.out.println(" - " + histogram.toLongMicrosFormat());
                        if (t == 0)
                            histogram.reset();
                    }
                    if (t == 0)
                        Jvm.pause(500);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
