package net.openhft.chronicle.core.cooler;

import net.openhft.chronicle.core.util.Histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class CoolerTester {
    static volatile Object blackhole;
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
                        do {
                            if (t > 0)
                                disturber.disturb();
                            long start0 = System.nanoTime();
                            blackhole = tested.call();
                            long time0 = System.nanoTime() - start0;
                            histogram.sample(time0);
                            count++;
                        }
                        while (count < minCount || (System.currentTimeMillis() - start <= runTimeMS && count < maxCount));
                        if (tests.size() > 1)
                            System.out.print(testNames.get(j) + " ");
                        System.out.print(disturber);
                        System.out.println(" - " + histogram.toLongMicrosFormat());
                        if (t == 0)
                            histogram.reset();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
