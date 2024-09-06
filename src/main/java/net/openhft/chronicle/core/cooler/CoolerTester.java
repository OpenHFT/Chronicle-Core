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
 * The CoolerTester class is used to benchmark the effectiveness of different {@link CpuCooler} implementations.
 *
 * <p>It allows the user to specify multiple tests and apply various coolers (or CPU disturbers) to evaluate
 * their performance impact. The execution time of each test is measured and recorded into {@link Histogram} objects,
 * providing insights on the performance behavior under different cooler implementations.</p>
 *
 * <p>The class supports multiple configuration options like setting the number of repetitions, minimum
 * test count, runtime duration, and task boundaries.</p>
 */
public class CoolerTester {

    /**
     * A placeholder to prevent the JVM from optimizing away meaningful operations.
     */
    static Object blackhole;

    /**
     * List of {@link CpuCooler} implementations to be used as disturbers in the tests.
     */
    private final List<CpuCooler> disturbers = new ArrayList<>();

    /**
     * List of {@link Histogram} objects to record the execution time results.
     */
    private final List<Histogram> histograms = new ArrayList<>();

    /**
     * List of names of the tests being run.
     */
    private final List<String> testNames = new ArrayList<>();

    /**
     * List of {@link Callable} tasks representing the tests to be executed.
     */
    private final List<Callable<?>> tests = new ArrayList<>();

    /**
     * Number of times each test will be repeated.
     */
    private int repeat = 10;

    /**
     * The runtime duration for each test in milliseconds.
     */
    private int runTimeMS = 5_000;

    /**
     * Minimum count of test executions before stopping.
     */
    private int minCount = 20;

    /**
     * Maximum count of test executions before stopping.
     */
    private int maxCount = 20_000;

    /**
     * Constructor for configuring the CoolerTester with a single test and multiple CpuCoolers.
     *
     * @param tested    the task to be tested
     * @param disturbers the list of CpuCooler implementations to be tested alongside the task
     */
    public CoolerTester(Callable<?> tested, CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
        this.testNames.add(""); // Empty test name as default
        this.tests.add(tested);
    }

    /**
     * Constructor for configuring the CoolerTester with a list of CpuCoolers but no initial tasks.
     *
     * @param disturbers the list of CpuCooler implementations to be used in the test
     */
    public CoolerTester(CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
    }

    /**
     * Constructor for configuring the CoolerTester with a single CpuCooler and multiple tests.
     *
     * @param disturber the CpuCooler to be tested
     * @param tests     the list of tasks to be executed during the test
     */
    public CoolerTester(CpuCooler disturber, Callable<?>... tests) {
        this.disturbers.add(disturber);
        Collections.addAll(this.tests, tests);
    }

    /**
     * Runs the inner loop for executing the test without any disturbers.
     *
     * @param tested      the test task to execute
     * @param histogram   the histogram to record test results
     * @param start       the start time in milliseconds
     * @param count       the current execution count
     * @param minCount    the minimum number of executions to perform
     * @param runTimeMS   the total runtime for the test in milliseconds
     * @param maxCount    the maximum number of executions allowed
     * @throws Exception if the tested callable throws an exception
     */
    static void innerloop0(Callable<?> tested, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount * 10));
    }

    /**
     * Runs the inner loop for executing the test with a disturber (CpuCooler).
     *
     * @param tested      the test task to execute
     * @param disturber   the CpuCooler to disturb the CPU during the test
     * @param histogram   the histogram to record test results
     * @param start       the start time in milliseconds
     * @param count       the current execution count
     * @param minCount    the minimum number of executions to perform
     * @param runTimeMS   the total runtime for the test in milliseconds
     * @param maxCount    the maximum number of executions allowed
     * @throws Exception if the tested callable throws an exception
     */
    static void innerloop1(Callable<?> tested, CpuCooler disturber, Histogram histogram, long start, int count, int minCount, int runTimeMS, int maxCount) throws Exception {
        do {
            disturber.disturb();
            innerLoop2(tested, histogram);
            count++;
        }
        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount));
    }

    /**
     * Executes a single test iteration and records the result in a histogram.
     *
     * @param tested    the test task to execute
     * @param histogram the histogram to record test results
     * @throws Exception if the tested callable throws an exception
     */
    private static void innerLoop2(Callable<?> tested, Histogram histogram) throws Exception {
        UNSAFE.fullFence();  // Ensures full memory fencing for accurate timing
        long start0 = System.nanoTime(); // Start timing
        blackhole = tested.call(); // Execute the test and store result to prevent optimization
        long time0 = System.nanoTime() - start0; // Calculate execution time
        histogram.sample(time0); // Record execution time in the histogram
    }

    /**
     * Adds a test to be executed during the tests.
     *
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
     *
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
     *
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
     *
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
     *
     * @param maxCount the maximum number of times each test is run with each cooler
     * @return this object
     */
    public CoolerTester maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * This method runs the tests and outputs the results. Each test is run with each cooler multiple times,
     * and the execution times are recorded. The results are printed to the console after the warm-up and real tests.
     */
    public void run() {
        try {
            // Warm-up phase
            System.out.println("---- Warmup ----");
            for (int j = 0; j < tests.size(); j++) {
                // Create a histogram for each test-disturber pair
                for (int i = 0; i < disturbers.size(); i++) {
                    histograms.add(new Histogram(32, 7)); // Record results in a histogram for this test-disturber pair
                }
            }

            // Repeat the test the specified number of times (repeat + 1, since 0th is warmup)
            for (int t = 0; t <= repeat; t++) {
                if (t == 1)
                    System.out.println("\n---- Real Tests ----");
                if (t == repeat)
                    System.out.println("\n---- RESULTS ----\n");

                // Run all tests
                for (int j = 0; j < tests.size(); j++) {
                    runInnerLoop(t, j); // Execute the test in the inner loop
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // If any exception occurs, throw a runtime exception
        }
    }

    /**
     * Runs the inner loop of the tests for a single test and multiple coolers.
     * It runs the test either in the warmup phase (t = 0) or the actual test (t > 0),
     * and outputs the results to the console, including histogram data.
     *
     * @param t the iteration number (0 is warm-up, > 0 is actual tests)
     * @param j the index of the current test in the list of tests
     * @throws Exception if the tested callable throws an exception
     */
    private void runInnerLoop(int t, int j) throws Exception {
        // Get the test callable
        Callable<?> tested = tests.get(j);

        // Iterate over all disturbers (CpuCoolers)
        for (int i = 0; i < disturbers.size(); i++) {
            CpuCooler disturber = disturbers.get(i); // Get the disturber (cooler)
            Histogram histogram = histograms.get(j * disturbers.size() + i); // Get the corresponding histogram

            long start = System.currentTimeMillis(); // Start time for this round
            int count = 0; // Initialize the test count

            // If this is not the warm-up round, run the disturbed test; otherwise, run the undisturbed one
            if (t > 0)
                innerloop1(tested, disturber, histogram, start, count, minCount, runTimeMS, maxCount);
            else
                innerloop0(tested, histogram, start, count, minCount, runTimeMS, maxCount);

            // Print the results for the current cooler and test
            if (tests.size() > 1)
                System.out.print(testNames.get(j) + " ");
            System.out.print(disturber);
            System.out.println(",band,<0.1,<1,<10,<100, "
                    + histogram.percentageLessThan(0.1e3) + ", "
                    + histogram.percentageLessThan(1e3) + ", "
                    + histogram.percentageLessThan(10e3) + ", "
                    + histogram.percentageLessThan(100e3) + ",%iles,"
                    + histogram.toLongMicrosFormat());

            // Reset the histogram after warm-up phase
            if (t == 0)
                histogram.reset();
        }

        // After the warm-up round, pause to allow the system to cool down
        if (t == 0)
            Jvm.pause(500);
    }
}
