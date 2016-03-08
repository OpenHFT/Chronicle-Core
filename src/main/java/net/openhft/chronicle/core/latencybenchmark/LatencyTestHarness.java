package net.openhft.chronicle.core.latencybenchmark;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.Histogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by daniel on 03/03/2016.
 */
public class LatencyTestHarness {
    private int messageCount = -1;
    private int warmUp = 10000;
    private int throughput = 10_000;
    private int rate = 1_000_000_000 / throughput;
    private boolean accountForCoordinatedOmmission = true;
    private Histogram histogram = new Histogram();
    private Histogram osJitter = new Histogram();
    private int recordJitterGreaterThanNs = 1_000;
    private int runs = 1;
    private LatencyTask latencyTask;
    private boolean recordOSJitter = true;
    private long noResultsReturned;
    private AtomicBoolean warmUpComplete = new AtomicBoolean(false);
    private final Map<String, Histogram>additionHistograms = new HashMap<>();

    //Use non-atomic when so thread synchronisation is necessary
    private boolean warmedUp;

    public LatencyTestHarness throughput(int throughput) {
        this.throughput = throughput;
        return this;
    }

    public LatencyTestHarness accountForCoordinatedOmmission(Boolean accountForCoordinatedOmmission) {
        this.accountForCoordinatedOmmission = accountForCoordinatedOmmission;
        return this;
    }

    public LatencyTestHarness recordJitterGreaterThanNs(int recordJitterGreaterThanNs) {
        this.recordJitterGreaterThanNs = recordJitterGreaterThanNs;
        return this;
    }

    public LatencyTestHarness recordOSJitter(boolean recordOSJitter) {
        this.recordOSJitter = recordOSJitter;
        return this;
    }

    public LatencyTestHarness warmUp(int warmUp) {
        this.warmUp = warmUp;
        return this;
    }

    public LatencyTestHarness runs(int runs) {
        this.runs = runs;
        return this;
    }

    public LatencyTestHarness messageCount(int messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    public LatencyTestHarness build(LatencyTask latencyTask) {
        if (messageCount == -1) throw new IllegalStateException("messageCount must be set");
        if (throughput == -1) throw new IllegalStateException("throughput must be set");
        this.latencyTask = latencyTask;
        rate = 1_000_000_000 / throughput;
        return this;
    }

    public Histogram createAdditionalHistogram(String name){
        return additionHistograms.computeIfAbsent(name, n->new Histogram());
    }

    public void start() {
        latencyTask.init(this);
        OSJitterMonitor osJitterMonitor = new OSJitterMonitor();
        List<double[]> percentileRuns = new ArrayList<>();
        Map<String, List<double[]>> additionalPercentileRuns = new HashMap<>();

        if (recordOSJitter) {
            osJitterMonitor.setDaemon(true);
            osJitterMonitor.start();
        }

        for (int i = 0; i < warmUp; i++) {
            latencyTask.run(System.nanoTime());
        }

        for (int run = 0; run < runs; run++) {
            long startTimeNs = System.nanoTime();
            for (int i = 0; i < messageCount; i++) {

                if (i == 0 && run == 0) {
                    while (!warmUpComplete.get())
                        startTimeNs = System.nanoTime();
                }
                else if (accountForCoordinatedOmmission) {
                    startTimeNs += rate;
                    while (System.nanoTime() < startTimeNs)
                        ;
                } else {
                    Jvm.busyWaitMicros(rate / 1000);
                    startTimeNs = System.nanoTime();
                }

                latencyTask.run(startTimeNs);
            }

            while (histogram.totalCount() < messageCount) {
                Thread.yield();
            }

            percentileRuns.add(histogram.getPercentiles());

            System.out.println("-------------------------------- BENCHMARK RESULTS (RUN " + (run + 1) + ") --------------------------------------------------------");
            System.out.println("Correcting for co-ordinated:" + accountForCoordinatedOmmission);
            System.out.println("Target throughtput:" + throughput + "/s" + " = 1 message every " + (rate / 1000) + "us");
            System.out.println("TotalCount:" + histogram.totalCount());
            System.out.printf("%-40s", "whole run:");
            System.out.println(histogram.toMicrosFormat());

            if (additionHistograms.size() > 0) {
                additionHistograms.entrySet().stream().forEach(e -> {
                    List<double[]> ds = additionalPercentileRuns.computeIfAbsent(e.getKey(),
                            i->new ArrayList<>());
                    ds.add(e.getValue().getPercentiles());
                    System.out.printf("%-40s", e.getKey() + ":");
                    System.out.println(e.getValue().toMicrosFormat());
                });
            }
            if (recordOSJitter){
                System.out.printf("%-40s", "OS Jitter:");
                System.out.println(osJitter.toMicrosFormat());
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            noResultsReturned = 0;
            histogram.reset();
            additionHistograms.values().stream().forEach(Histogram::reset);
            osJitterMonitor.reset();
        }

        printPercentilesSummary("whole run", percentileRuns);
        if (additionalPercentileRuns.size() > 0) {
            additionalPercentileRuns.entrySet().stream().forEach(e -> {
                printPercentilesSummary(e.getKey(), e.getValue());
            });
        }
        latencyTask.complete();
    }

    public void printPercentilesSummary(String label, List<double[]> percentileRuns){
        System.out.println("-------------------------------- SUMMARY ("+ label + ")------------------------------------------------------------");
        List<Double> consistencies = new ArrayList<>();
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;
        for(int i=0; i<percentileRuns.get(0).length; i++){
            for(int j=0; j<percentileRuns.size(); j++){
                if(percentileRuns.get(j)[i] > maxValue)
                    maxValue = percentileRuns.get(j)[i];
                if(percentileRuns.get(j)[i] < minValue)
                    minValue = percentileRuns.get(j)[i];
            }
            consistencies.add(100 * (maxValue-minValue)/(maxValue+minValue/2));
            maxValue = Double.MIN_VALUE;
            minValue = Double.MAX_VALUE;
        }

        List<Double> summary = new ArrayList<>();
        for(int i=0; i<percentileRuns.get(0).length; i++){
            for(int j=0; j<percentileRuns.size(); j++){
                summary.add(percentileRuns.get(j)[i]/1e3);
            }
            summary.add(consistencies.get(i));
        }

        StringBuilder sb = new StringBuilder();
        addHeaderToPrint(sb, runs);
        System.out.println(sb.toString());

        sb= new StringBuilder();
        addPrToPrint(sb, "50:   ", runs);
        addPrToPrint(sb, "90:   ", runs);
        addPrToPrint(sb, "99:   ", runs);
        addPrToPrint(sb, "99.9: ", runs);
        addPrToPrint(sb, "99.99:", runs);
        addPrToPrint(sb, "worst:", runs);

        System.out.printf(sb.toString(), summary.toArray(new Double[0]));
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
    }

    private void addPrToPrint(StringBuilder sb, String pr, int runs){
        sb.append(pr);
        for(int i=0; i<runs; i++){
            sb.append("%12.2f ");
        }
        sb.append("%12.4f%n");
    }

    private void addHeaderToPrint(StringBuilder sb, int runs){
        sb.append("Percentile");
        for(int i=1; i<runs+1; i++){
            if(i==1)
                sb.append("   run" + i);
            else
             sb.append("         run" + i);
        }
        sb.append("      % Variation");
    }


    public void sample(long nanoTime) {
        noResultsReturned++;
        if (noResultsReturned < warmUp && !warmedUp) {
            return;
        }
        if (noResultsReturned == warmUp && !warmedUp) {
            warmedUp = true;
            warmUpComplete.set(true);
            return;
        }
        histogram.sample(nanoTime);
    }

    private class OSJitterMonitor extends Thread{
        AtomicBoolean reset = new AtomicBoolean(false);

        public void run(){
            long lastTime = System.nanoTime();
            while (true) {
                if(reset.get()){
                    reset.set(false);
                    osJitter.reset();
                    lastTime = System.nanoTime();
                }
                long time = System.nanoTime();
                if (time - lastTime > recordJitterGreaterThanNs) {
                    //System.out.println("DELAY " + (time - lastTime) / 100_000 / 10.0 + "ms");
                    osJitter.sample(time - lastTime);
                }
                lastTime = time;
            }
        }

        public void reset(){
            reset.set(true);
        }
    }
}
