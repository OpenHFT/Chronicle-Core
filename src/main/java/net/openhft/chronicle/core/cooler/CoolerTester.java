/*
 * Copyright 2016-2020 chronicle.software
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
package net.openhft.chronicle.core.cooler;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.Histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

/**
 * This class is used to test the effectiveness of various {@link CpuCooler} implementations.
 * It allows configuring multiple tests and multiple coolers to test against.
 * It executes each test multiple times with each cooler and records the time it takes to run each test.
 */
public class CoolerTester {

    /**
     * Holds the results of the tests.
     */
    static Object blackhole;

    /**
     * List of disturbers or CPU coolers to be used in the tests.
     */
    private final List<CpuCooler> disturbers = new ArrayList<>();

    /**
     * List of Histogram objects to record the test results.
     */
    private final List<Histogram> histograms = new ArrayList<>();

    /**
     * List of test names.
     */
    private final List<String> testNames = new ArrayList<>();

    /**
     * List of tests to be run.
     */
    private final List<Callable<?>> tests = new ArrayList<>();
    private int repeat = 10;
    private int runTimeMS = 5_000;
    private int minCount = 20;
    private int maxCount = 20_000;

    /**
     * Constructor with Callable and array of CpuCoolers. The Callable is the task to be executed
     * while each of the CpuCoolers is in use.
     *
     * @param tested     the task to be executed during the tests
     * @param disturbers the array of CpuCoolers to be tested
     */
    public CoolerTester(Callable<?> tested, CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
        this.testNames.add("");
        this.tests.add(tested);
    }

    /**
     * Constructor with an array of CpuCoolers. No tasks are initially configured for execution.
     *
     * @param disturbers the array of CpuCoolers to be tested
     */
    public CoolerTester(CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
    }

    /**
     * Constructor with a single CpuCooler and an array of tasks to be executed while the CpuCooler is in use.
     *
     * @param disturber the CpuCooler to be tested
     * @param tests     the array of tasks to be executed during the tests
     */
    public CoolerTester(CpuCooler disturber, Callable<?>... tests) {
        this.disturbers.add(disturber);
        Collections.addAll(this.tests, tests);
    }

    static void innerloop0(Callable<?> tested, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount * 10));
    }

    static void innerloop1(Callable<?> tested, CpuCooler disturber, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            disturber.disturb();
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount));
    }

    private static void innerLoop2(Callable<?> tested, Histogram histogram) throws Exception {
        UNSAFE.fullFence();
        long start0 = System.nanoTime();
        blackhole = tested.call();
//            UNSAFE.fullFence();
        long time0 = System.nanoTime() - start0;
        histogram.sample(time0);
    }

    /**
     * Adds a test to be executed during the tests.
     * @param name the name of the test
     * @param test the task to be executed during the tests
     * @return this object
     */
    public CoolerTester add(String name, Callable<?> test) {
        testNames.add(name);
        tests.add(test);
        return this;
    }

    /**
     * @return the maximum number of times each test is run with each cooler.
     */
    public int repeat() {
        return repeat;
    }

    /**
     * Sets the number of times each test is run with each cooler.
     * @param repeat the number of times each test is run with each cooler
     * @return this object
     */
    public CoolerTester repeat(int repeat) {
        this.repeat = repeat;
        return this;
    }

    /**
     * @return the time in milliseconds that each test is run with each cooler.
     */
    public int runTimeMS() {
        return runTimeMS;
    }

    /**
     * Sets the time in milliseconds that each test is run with each cooler.
     * @param runTimeMS the time in milliseconds that each test is run with each cooler
     * @return this object
     */
    public CoolerTester runTimeMS(int runTimeMS) {
        this.runTimeMS = runTimeMS;
        return this;
    }

    /**
     * @return the minimum number of times each test is run with each cooler.
     */
    public int minCount() {
        return minCount;
    }

    /**
     * Sets the minimum number of times each test is run with each cooler.
     * @param minCount the minimum number of times each test is run with each cooler
     * @return this object
     */
    public CoolerTester minCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    /**
     * @return the maximum number of times each test is run with each cooler.
     */
    public int maxCount() {
        return maxCount;
    }

    /**
     * Sets the maximum number of times each test is run with each cooler.
     * @param maxCount the maximum number of times each test is run with each cooler
     * @return this object
     */
    public CoolerTester maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * This method runs the tests and outputs the results. Each test is run with each cooler multiple times,
     * and the execution times are recorded.
     */
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
                    runInnerLoop(t, j);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runInnerLoop(int t, int j) throws Exception {
        Callable<?> tested = tests.get(j);
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
            System.out.println(",band,<0.1,<1,<10,<100, "
                    + histogram.percentageLessThan(0.1e3) + ", "
                    + histogram.percentageLessThan(1e3) + ", "
                    + histogram.percentageLessThan(10e3) + ", "
                    + histogram.percentageLessThan(100e3) + ",%iles,"
                    + histogram.toLongMicrosFormat());
            if (t == 0)
                histogram.reset();
        }
        if (t == 0)
            Jvm.pause(500);
    }
}
