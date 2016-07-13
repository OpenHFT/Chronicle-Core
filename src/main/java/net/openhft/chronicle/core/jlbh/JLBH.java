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
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.core.util.NanoSampler;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java Latency Benchmark Harness
 * The harness is intended to be used for benchmarks where co-ordinated ommission is an issue.
 * Typically these would be of the producer/consumer nature where the start time for the benchmark
 * may be on a different thread than the end time.
 * <p></p>
 * This tool was inspired by JMH.
 */
public class JLBH implements NanoSampler {
    private static final Double[] NO_DOUBLES = {};
    private final SortedMap<String, Histogram> additionHistograms = new ConcurrentSkipListMap<>();
    // wait time between invocations in nanoseconds
    private final long rate;
    // wait time in full milli seconds
    private final long waitTimeMillis;
    private final JLBHOptions jlbhOptions;
    private Histogram endToEndHistogram = new Histogram();
    private Histogram osJitterHistogram = new Histogram();
    private long noResultsReturned;
    private AtomicBoolean warmUpComplete = new AtomicBoolean(false);
    //Use non-atomic when so thread synchronisation is necessary
    private boolean warmedUp;

    /**
     * @param jlbhOptions Options to run the benchmark
     */
    public JLBH(JLBHOptions jlbhOptions) {
        this.jlbhOptions = jlbhOptions;
        if (jlbhOptions.jlbhTask == null) throw new IllegalStateException("jlbhTask must be set");
        rate = jlbhOptions.throughputTimeUnit.toNanos(1) / jlbhOptions.throughput;
        // we consider 2 ms the threshold where we use a mixed strategy and sleep for rate - 1 ms and busy wait for the rest of the time
        if (rate > TimeUnit.MILLISECONDS.toNanos(2)) {
            waitTimeMillis = TimeUnit.NANOSECONDS.toMillis(rate - TimeUnit.MILLISECONDS.toNanos(1));
        } else {
            waitTimeMillis = 0;
        }
    }

    /**
     * Add a probe to measure a section of the benchmark.
     * @param name Name of probe
     * @return NanoSampler
     */
    public NanoSampler addProbe(String name) {
        return additionHistograms.computeIfAbsent(name, n -> new Histogram());
    }

    /**
     * Start benchmark
     */
    public void start() {
        jlbhOptions.jlbhTask.init(this);
        OSJitterMonitor osJitterMonitor = new OSJitterMonitor();
        List<double[]> percentileRuns = new ArrayList<>();
        Map<String, List<double[]>> additionalPercentileRuns = new TreeMap<>();

        if (jlbhOptions.recordOSJitter) {
            osJitterMonitor.setDaemon(true);
            osJitterMonitor.start();
        }

        long warmupStart = System.currentTimeMillis();
        for (int i = 0; i < jlbhOptions.warmUpIterations; i++) {
            jlbhOptions.jlbhTask.run(System.nanoTime());
        }

        AffinityLock lock = Affinity.acquireLock();
        try {
            for (int run = 0; run < jlbhOptions.runs; run++) {
                long runStart = System.currentTimeMillis();
                long startTimeNs = System.nanoTime();
                for (int i = 0; i < jlbhOptions.iterations; i++) {

                    if (i == 0 && run == 0) {
                        while (!warmUpComplete.get()) {
                            Jvm.pause(2000);
                            System.out.println("Complete: " + noResultsReturned);
                        }
                        System.out.println("Warm up complete (" + jlbhOptions.warmUpIterations + " iterations took " +
                                ((System.currentTimeMillis()-warmupStart)/1000.0) + "s)");
                        if(jlbhOptions.pauseAfterWarmupMS!=0){
                            System.out.println("Pausing after warmup for " + jlbhOptions.pauseAfterWarmupMS + "ms");
                            Jvm.pause(jlbhOptions.pauseAfterWarmupMS);
                        }
                        runStart = System.currentTimeMillis();
                        startTimeNs = System.nanoTime();
                    } else if (jlbhOptions.accountForCoordinatedOmission) {
                        startTimeNs += rate;
                        if (waitTimeMillis > 0) {
                            Jvm.pause(waitTimeMillis);
                        }
                        Jvm.busyWaitUntil(startTimeNs);
                    } else {
                        if (waitTimeMillis > 0) {
                            long end = System.nanoTime() + rate;
                            Jvm.pause(waitTimeMillis);
                            // account for jitter in Thread.sleep() and wait until a fixed point in time
                            Jvm.busyWaitUntil(end);
                        } else {
                            Jvm.busyWaitMicros(rate / 1000);
                        }
                        startTimeNs = System.nanoTime();
                    }

                    jlbhOptions.jlbhTask.run(startTimeNs);
                }

                while (endToEndHistogram.totalCount() < jlbhOptions.iterations) {
                    Thread.yield();
                }
                long totalRunTime = System.currentTimeMillis()-runStart;

                percentileRuns.add(endToEndHistogram.getPercentiles());

                System.out.println("-------------------------------- BENCHMARK RESULTS (RUN " + (run + 1) + ") --------------------------------------------------------");
                System.out.println("Run time: " + totalRunTime/1000.0 + "s");
                System.out.println("Correcting for co-ordinated:" + jlbhOptions.accountForCoordinatedOmission);
                System.out.println("Target throughput:" + jlbhOptions.throughput + "/" + timeUnitToString(jlbhOptions.throughputTimeUnit ) + " = 1 message every " + (rate / 1000) + "us");
                System.out.printf("%-48s", String.format("End to End: (%,d)", endToEndHistogram.totalCount()));
                System.out.println(endToEndHistogram.toMicrosFormat());

                if (additionHistograms.size() > 0) {
                    additionHistograms.entrySet().stream().forEach(e -> {
                        List<double[]> ds = additionalPercentileRuns.computeIfAbsent(e.getKey(),
                                i -> new ArrayList<>());
                        ds.add(e.getValue().getPercentiles());
                        System.out.printf("%-48s", String.format("%s (%,d) ", e.getKey(), e.getValue().totalCount()));
                        System.out.println(e.getValue().toMicrosFormat());
                    });
                }
                if (jlbhOptions.recordOSJitter) {
                    System.out.printf("%-48s", String.format("OS Jitter (%,d)", osJitterHistogram.totalCount()));
                    System.out.println(osJitterHistogram.toMicrosFormat());
                }
                System.out.println("-------------------------------------------------------------------------------------------------------------------");

                noResultsReturned = 0;
                endToEndHistogram.reset();
                additionHistograms.values().stream().forEach(Histogram::reset);
                osJitterMonitor.reset();
            }
        } finally {
            Jvm.pause(5);
            lock.release();
            Jvm.pause(5);
        }

        printPercentilesSummary("end to end", percentileRuns);
        if (additionalPercentileRuns.size() > 0) {
            additionalPercentileRuns.entrySet().stream().forEach(e -> printPercentilesSummary(e.getKey(), e.getValue()));
        }
        jlbhOptions.jlbhTask.complete();
    }

    private void printPercentilesSummary(String label, List<double[]> percentileRuns) {
        System.out.println("-------------------------------- SUMMARY (" + label + ")------------------------------------------------------------");
        List<Double> consistencies = new ArrayList<>();
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;
        int length = percentileRuns.get(0).length;
        for (int i = 0; i < length; i++) {
            boolean skipFirst = length > 3;
            if(jlbhOptions.skipFirstRun== JLBHOptions.SKIP_FIRST_RUN.SKIP) {
                skipFirst = true;
            }else if(jlbhOptions.skipFirstRun== JLBHOptions.SKIP_FIRST_RUN.NO_SKIP){
                skipFirst = false;
            }
            for (double[] percentileRun : percentileRuns) {
                if (skipFirst) {
                    skipFirst = false;
                    continue;
                }
                double v = percentileRun[i];
                if (v > maxValue)
                    maxValue = v;
                if (v < minValue)
                    minValue = v;
            }
            consistencies.add(100 * (maxValue - minValue) / (maxValue + minValue / 2));

            maxValue = Double.MIN_VALUE;
            minValue = Double.MAX_VALUE;
        }

        List<Double> summary = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            for (double[] percentileRun : percentileRuns) {
                summary.add(percentileRun[i] / 1e3);
            }
            summary.add(consistencies.get(i));
        }

        StringBuilder sb = new StringBuilder();
        addHeaderToPrint(sb, jlbhOptions.runs);
        System.out.println(sb.toString());

        sb = new StringBuilder();
        addPrToPrint(sb, "50:     ", jlbhOptions.runs);
        addPrToPrint(sb, "90:     ", jlbhOptions.runs);
        addPrToPrint(sb, "99:     ", jlbhOptions.runs);
        addPrToPrint(sb, "99.9:   ", jlbhOptions.runs);
        addPrToPrint(sb, "99.99:  ", jlbhOptions.runs);
        if(jlbhOptions.iterations > 1_000_000)
            addPrToPrint(sb, "99.999: ", jlbhOptions.runs);
        if(jlbhOptions.iterations > 10_000_000)
            addPrToPrint(sb, "99.9999:", jlbhOptions.runs);
        addPrToPrint(sb, "worst:  ", jlbhOptions.runs);

        System.out.printf(sb.toString(), summary.toArray(NO_DOUBLES));
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
    }

    private void addPrToPrint(StringBuilder sb, String pr, int runs) {
        sb.append(pr);
        for (int i = 0; i < runs; i++) {
            sb.append("%12.2f ");
        }
        sb.append("%12.2f");
        sb.append("%n");
    }

    private void addHeaderToPrint(StringBuilder sb, int runs) {
        sb.append("Percentile");
        for (int i = 1; i < runs + 1; i++) {
            if (i == 1)
                sb.append("   run").append(i);
            else
                sb.append("         run").append(i);
        }
        sb.append("      % Variation");
    }

    private String timeUnitToString(TimeUnit timeUnit) {
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

    private class OSJitterMonitor extends Thread {
        final AtomicBoolean reset = new AtomicBoolean(false);

        public void run() {
            // make sure this thread is not bound by its parent.
            Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
            AffinityLock affinityLock = null;
            if(jlbhOptions.jitterAffinity){
                System.out.println("Jitter thread running with affinity.");
                affinityLock = AffinityLock.acquireLock();
            }

            try {
                long lastTime = System.nanoTime();
                while (true) {
                    if (reset.get()) {
                        reset.set(false);
                        osJitterHistogram.reset();
                        lastTime = System.nanoTime();
                    }
                    long time = System.nanoTime();
                    if (time - lastTime > jlbhOptions.recordJitterGreaterThanNs) {
                        osJitterHistogram.sample(time - lastTime);
                    }
                    lastTime = time;
                }
            }finally{
                if(affinityLock!=null)
                    affinityLock.release();
            }
        }

        void reset() {
            reset.set(true);
        }
    }
}
