/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.core.jlbh;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.threads.EventHandler;
import net.openhft.chronicle.core.threads.InvalidEventHandlerException;
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.core.util.NanoSampler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Java Latency Benchmark Harness
 * The harness is intended to be used for benchmarks where co-ordinated omission is an issue.
 * Typically these would be of the producer/consumer nature where the start time for the benchmark
 * may be on a different thread than the end time.
 * <p></p>
 * This tool was inspired by JMH.
 */
public class JLBH implements NanoSampler {
    private static final Double[] NO_DOUBLES = {};
    private final SortedMap<String, Histogram> additionHistograms = new ConcurrentSkipListMap<>();
    // wait time between invocations in nanoseconds
    private final long latencyBetweenTasks;
    @NotNull
    private final JLBHOptions jlbhOptions;
    @NotNull
    private final PrintStream printStream;
    private final Consumer<JLBHResult> resultConsumer;
    @NotNull
    private final List<double[]> percentileRuns;
    @NotNull
    private final Map<String, List<double[]>> additionalPercentileRuns;
    @NotNull
    private final OSJitterMonitor osJitterMonitor = new OSJitterMonitor();
    @NotNull
    private Histogram endToEndHistogram = createHistogram();
    @NotNull
    private Histogram osJitterHistogram = createHistogram();
    private long noResultsReturned;
    @NotNull
    private AtomicBoolean warmUpComplete = new AtomicBoolean(false);
    //Use non-atomic when so thread synchronisation is necessary
    private boolean warmedUp;

    /**
     * @param jlbhOptions Options to run the benchmark
     */
    public JLBH(@NotNull JLBHOptions jlbhOptions) {
        this(jlbhOptions, System.out, null);
    }

    /**
     * Use this constructor if you want to test the latencies in more automated fashion.
     * The result is passed to the result consumer after the JLBH::start method returns.
     * You can create you own consumer, or use provided JLBHResultConsumer::newThreadSafeInstance()
     * that allows you to retrieve the result even if the JLBH has been executed in a different thread.
     *
     * @param jlbhOptions    Options to run the benchmark
     * @param printStream    Used to print text output. Use System.out to show the result on you standard out (e.g. screen)
     * @param resultConsumer If provided, accepts the result data to be retrieved after the latencies have been measured
     */
    public JLBH(@NotNull JLBHOptions jlbhOptions, @NotNull PrintStream printStream, Consumer<JLBHResult> resultConsumer) {
        this.jlbhOptions = jlbhOptions;
        this.printStream = printStream;
        this.resultConsumer = resultConsumer;
        if (jlbhOptions.jlbhTask == null) throw new IllegalStateException("jlbhTask must be set");
        latencyBetweenTasks = jlbhOptions.throughputTimeUnit.toNanos(1) / jlbhOptions.throughput;
        percentileRuns = new ArrayList<>();
        additionalPercentileRuns = new TreeMap<>();

    }

    /**
     * Add a probe to measure a section of the benchmark.
     *
     * @param name Name of probe
     * @return NanoSampler
     */
    public NanoSampler addProbe(String name) {
        return additionHistograms.computeIfAbsent(name, n -> createHistogram());
    }

    /**
     * Start benchmark
     */
    public void start() {
        long warmupStart = initStartOSJitterMonitorWarmup();

        AffinityLock lock = jlbhOptions.acquireLock.get();
        try {
            for (int run = 0; run < jlbhOptions.runs; run++) {
                long runStart = System.currentTimeMillis();
                long startTimeNs = System.nanoTime();
                for (int i = 0; i < jlbhOptions.iterations; i++) {

                    if (i == 0 && run == 0) {
                        warmupComplete(warmupStart);
                        runStart = System.currentTimeMillis();
                        startTimeNs = System.nanoTime();
                    } else if (jlbhOptions.accountForCoordinatedOmission) {
                        startTimeNs += latencyBetweenTasks;
                        long millis = (startTimeNs - System.nanoTime()) / 1000000 - 2;
                        if (millis > 0) {
                            Jvm.pause(millis);
                        }
                        Jvm.busyWaitUntil(startTimeNs);

                    } else {
                        if (latencyBetweenTasks > 2e6) {
                            long end = System.nanoTime() + latencyBetweenTasks;
                            Jvm.pause(latencyBetweenTasks / 1_000_000 - 1);
                            // account for jitter in Thread.sleep() and wait until a fixed point in time
                            Jvm.busyWaitUntil(end);
                        } else {
                            Jvm.busyWaitMicros(latencyBetweenTasks / 1000);
                        }
                        startTimeNs = System.nanoTime();
                    }

                    jlbhOptions.jlbhTask.run(startTimeNs);
                }

                endOfRun(run, runStart);
            }
        } finally {
            Jvm.pause(5);
            lock.release();
            Jvm.pause(5);
        }

        endOfAllRuns();
    }

    private void warmupComplete(long warmupStart) {
        while (!warmUpComplete.get()) {
            Jvm.pause(2000);
            printStream.println("Complete: " + noResultsReturned);
        }
        printStream.println("Warm up complete (" + jlbhOptions.warmUpIterations + " iterations took " +
                ((System.currentTimeMillis() - warmupStart) / 1000.0) + "s)");
        if (jlbhOptions.pauseAfterWarmupMS != 0) {
            printStream.println("Pausing after warmup for " + jlbhOptions.pauseAfterWarmupMS + "ms");
            Jvm.pause(jlbhOptions.pauseAfterWarmupMS);
        }
        jlbhOptions.jlbhTask.warmedUp();
    }

    private long initStartOSJitterMonitorWarmup() {
        jlbhOptions.jlbhTask.init(this);
        if (jlbhOptions.recordOSJitter) {
            osJitterMonitor.setDaemon(true);
            osJitterMonitor.start();
        }

        long warmupStart = System.currentTimeMillis();
        for (int i = 0; i < jlbhOptions.warmUpIterations; i++) {
            jlbhOptions.jlbhTask.run(System.nanoTime());
        }
        return warmupStart;
    }

    private void endOfAllRuns() {
        printPercentilesSummary("end to end", percentileRuns);
        if (additionalPercentileRuns.size() > 0) {
            additionalPercentileRuns.forEach(this::printPercentilesSummary);
        }

        consumeResults();

        jlbhOptions.jlbhTask.complete();
    }

    private void endOfRun(int run, long runStart) {
        while (endToEndHistogram.totalCount() < jlbhOptions.iterations) {
            Thread.yield();
        }
        long totalRunTime = System.currentTimeMillis() - runStart;

        percentileRuns.add(endToEndHistogram.getPercentiles());

        printStream.println("-------------------------------- BENCHMARK RESULTS (RUN " + (run + 1) + ") --------------------------------------------------------");
        printStream.println("Run time: " + totalRunTime / 1000.0 + "s");
        printStream.println("Correcting for co-ordinated:" + jlbhOptions.accountForCoordinatedOmission);
        printStream.println("Target throughput:" + jlbhOptions.throughput + "/" + timeUnitToString(jlbhOptions.throughputTimeUnit) + " = 1 message every " + (latencyBetweenTasks / 1000) + "us");
        printStream.printf("%-48s", String.format("End to End: (%,d)", endToEndHistogram.totalCount()));
        printStream.println(endToEndHistogram.toMicrosFormat());

        if (additionHistograms.size() > 0) {
            additionHistograms.forEach((key, value) -> {
                List<double[]> ds = additionalPercentileRuns.computeIfAbsent(key,
                        i -> new ArrayList<>());
                ds.add(value.getPercentiles());
//                if (value.totalCount() != jlbhOptions.iterations)
//                    warning = " WARNING " + value.totalCount() + "!=" + jlbhOptions.iterations;
                printStream.printf("%-48s", String.format("%s (%,d)", key, value.totalCount()));
                printStream.println(value.toMicrosFormat());
            });
        }
        if (jlbhOptions.recordOSJitter) {
            printStream.printf("%-48s", String.format("OS Jitter (%,d)", osJitterHistogram.totalCount()));
            printStream.println(osJitterHistogram.toMicrosFormat());
        }
        printStream.println("-------------------------------------------------------------------------------------------------------------------");

        noResultsReturned = 0;
        endToEndHistogram.reset();
        additionHistograms.values().forEach(Histogram::reset);
        osJitterMonitor.reset();
    }

    /**
     * Call this instead of start if you want to install JLBH as a handler on your event loop thread
     */
    public JLBHEventHandler eventLoopHandler() {
        if (!jlbhOptions.accountForCoordinatedOmission)
            throw new UnsupportedOperationException();
        long warmupStart = initStartOSJitterMonitorWarmup();
        warmupComplete(warmupStart);
        return new JLBHEventHandler();
    }

    private void consumeResults() {
        if (resultConsumer != null) {
            final JLBHResult.ProbeResult endToEndProbeResult = new ImmutableProbeResult(percentileRuns);
            final Map<String, ImmutableProbeResult> additionalProbeResults = additionalPercentileRuns.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            probe -> new ImmutableProbeResult(probe.getValue())));
            resultConsumer.accept(new ImmutableJLBHResult(endToEndProbeResult, additionalProbeResults));
        }
    }

    private void printPercentilesSummary(String label, @NotNull List<double[]> percentileRuns) {
        printStream.println("-------------------------------- SUMMARY (" + label + ")------------------------------------------------------------");
        @NotNull List<Double> consistencies = new ArrayList<>();
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;
        double[] percentFor = Histogram.percentilesFor(jlbhOptions.iterations);
        int length = percentFor.length;
        for (int i = 0; i < length; i++) {
            boolean skipFirst = length > 3;
            if (jlbhOptions.skipFirstRun == JLBHOptions.SKIP_FIRST_RUN.SKIP) {
                skipFirst = true;
            } else if (jlbhOptions.skipFirstRun == JLBHOptions.SKIP_FIRST_RUN.NO_SKIP) {
                skipFirst = false;
            }
            for (double[] percentileRun : percentileRuns) {
                if (skipFirst) {
                    skipFirst = false;
                    continue;
                }
                // not all measures may have got the same number of samples
                if (i < percentileRun.length) {
                    double v = percentileRun[i];
                    if (v > maxValue)
                        maxValue = v;
                    if (v < minValue)
                        minValue = v;
                }
            }
            consistencies.add(100 * (maxValue - minValue) / (maxValue + minValue / 2));

            maxValue = Double.MIN_VALUE;
            minValue = Double.MAX_VALUE;
        }

        @NotNull List<Double> summary = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            for (double[] percentileRun : percentileRuns) {
                if (i < percentileRun.length)
                    summary.add(percentileRun[i] / 1e3);
                else
                    summary.add(Double.POSITIVE_INFINITY);
            }
            summary.add(consistencies.get(i));
        }

        @NotNull StringBuilder sb = new StringBuilder();
        addHeaderToPrint(sb, jlbhOptions.runs);
        printStream.println(sb.toString());

        sb = new StringBuilder();
        for (double p : percentFor) {
            String s;
            if (p == 1) {
                s = "worst";
            } else {
                double p2 = p * 100;
                s = (long) p2 == p2 ? Long.toString((long) p2) : Double.toString(p2);
            }
            s += ":     ";
            s = s.substring(0, 8);
            addPrToPrint(sb, s, jlbhOptions.runs);
        }

        printStream.printf(sb.toString(), summary.toArray(NO_DOUBLES));
        printStream.println("-------------------------------------------------------------------------------------------------------------------");
    }

    private void addPrToPrint(@NotNull StringBuilder sb, String pr, int runs) {
        sb.append(pr);
        for (int i = 0; i < runs; i++) {
            sb.append("%12.2f ");
        }
        sb.append("%12.2f");
        sb.append("%n");
    }

    private void addHeaderToPrint(@NotNull StringBuilder sb, int runs) {
        sb.append("Percentile");
        for (int i = 1; i < runs + 1; i++) {
            if (i == 1)
                sb.append("   run").append(i);
            else
                sb.append("         run").append(i);
        }
        sb.append("      % Variation");
    }

    private String timeUnitToString(@NotNull TimeUnit timeUnit) {
        switch (timeUnit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "day";
            default:
                throw new IllegalArgumentException("Unrecognized time unit value '" + timeUnit + "'");
        }
    }

    @Override
    public void sampleNanos(long nanos) {
        sample(nanos);
    }

    public void sample(long nanoTime) {
        noResultsReturned++;
        if (noResultsReturned < jlbhOptions.warmUpIterations && !warmedUp) {
            endToEndHistogram.sample(nanoTime);
            return;
        }
        if (noResultsReturned == jlbhOptions.warmUpIterations && !warmedUp) {
            warmedUp = true;
            endToEndHistogram.reset();
            if (additionHistograms.size() > 0) {
                additionHistograms.values().forEach(Histogram::reset);
            }
            warmUpComplete.set(true);
            return;
        }
        endToEndHistogram.sample(nanoTime);
    }

    @NotNull
    protected Histogram createHistogram() {
        return new Histogram(32, 10);
    }

    private class OSJitterMonitor extends Thread {
        final AtomicBoolean reset = new AtomicBoolean(false);

        @Override
        public void run() {
            // make sure this thread is not bound by its parent.
            Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
            @Nullable AffinityLock affinityLock = null;
            if (jlbhOptions.jitterAffinity) {
                printStream.println("Jitter thread running with affinity.");
                affinityLock = AffinityLock.acquireLock();
            }

            try {
                long lastTime = System.nanoTime(), start = lastTime;
                while (true) {
                    if (reset.compareAndSet(true, false)) {
                        osJitterHistogram.reset();
                        lastTime = System.nanoTime();
                    }
                    for (int i = 0; i < 1000; i++) {
                        long time = System.nanoTime();
                        if (time - lastTime > jlbhOptions.recordJitterGreaterThanNs) {
                            osJitterHistogram.sample(time - lastTime);
                        }
                        lastTime = time;
                    }
                    if (lastTime > start + 60e9)
                        Jvm.pause(1);
                }
            } finally {
                if (affinityLock != null)
                    affinityLock.release();
            }
        }

        void reset() {
            reset.set(true);
        }
    }

    private class JLBHEventHandler implements EventHandler {
        private int iteration;
        private long runStart;
        private long nextInvokeTime;
        private boolean waitingForEndOfRun = false;

        JLBHEventHandler() {
            resetTime();
        }

        private void resetTime() {
            runStart = System.currentTimeMillis();
            nextInvokeTime = System.nanoTime() + latencyBetweenTasks;
        }

        @Override
        public boolean action() throws InvalidEventHandlerException, InterruptedException {
            boolean busy = false;

            int run = iteration / jlbhOptions.iterations;
            int i = iteration % jlbhOptions.iterations;

            if (!waitingForEndOfRun) {
                long now = System.nanoTime();
                if (now >= nextInvokeTime) {
                    nextInvokeTime += latencyBetweenTasks;
                    jlbhOptions.jlbhTask.run(nextInvokeTime);
                    busy = true;
                    ++iteration;

                    if (i == jlbhOptions.iterations - 1)
                        waitingForEndOfRun = true;
                }
            } else {
                if (endToEndHistogram.totalCount() >= jlbhOptions.iterations) {
                    endOfRun(run - 1, runStart);
                    resetTime();
                    waitingForEndOfRun = false;
                    if (run == jlbhOptions.runs)
                        endOfAllRuns();
                }
            }

            return busy;
        }
    }

}
