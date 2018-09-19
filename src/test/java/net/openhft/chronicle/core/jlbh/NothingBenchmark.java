package net.openhft.chronicle.core.jlbh;

import static java.util.concurrent.TimeUnit.SECONDS;

public class NothingBenchmark implements JLBHTask {
    private JLBH jlbh;

    public static void main(String[] args) {
        int iterations = 10; // 10, 50, 100
        JLBHOptions lth = new JLBHOptions()
                .warmUpIterations(iterations)
                .iterations(iterations)
                .throughput(5, SECONDS)
                .runs(3)
                .recordOSJitter(true)
                .accountForCoordinatedOmmission(true)
                .jlbhTask(new NothingBenchmark());
        new JLBH(lth).start();
    }

    @Override
    public void run(long startTimeNS) {
        jlbh.sample(System.nanoTime() - startTimeNS);
    }

    @Override
    public void init(JLBH jlbh) {
        this.jlbh = jlbh;
    }
}