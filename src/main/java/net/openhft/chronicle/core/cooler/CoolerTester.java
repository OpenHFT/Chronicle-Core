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
    private final Callable tested;
    private int repeat = 1;
    private int runTimeMS = 10_000;

    public CoolerTester(Callable tested) {
        this(tested, CpuCoolers.values());
    }

    public CoolerTester(Callable tested, CpuCooler... disturbers) {
        Collections.addAll(this.disturbers, disturbers);
        this.tested = tested;
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

    public void run() {
        try {
            System.out.println("---- Warmup ----");
            for (int i = 0; i < disturbers.size(); i++) {
                histograms.add(new Histogram(32, 7));
            }
            for (int t = 0; t <= repeat; t++) {
                if (t == 1)
                    System.out.println("\n---- Real Tests ----");
                if (t == repeat)
                System.out.println("\n---- RESULTS ----\n");
                for (int i = 0; i < disturbers.size(); i++) {
                    CpuCooler disturber = disturbers.get(i);
                    Histogram histogram = histograms.get(i);

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
                    } while (count < 40 || (System.currentTimeMillis() - start <= runTimeMS && count < 20_000));
                    System.out.println(disturber + " - " + histogram.toLongMicrosFormat());
                    if (t == 0)
                        histogram.reset();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
