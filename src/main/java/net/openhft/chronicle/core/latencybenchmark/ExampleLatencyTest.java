package net.openhft.chronicle.core.latencybenchmark;

import net.openhft.chronicle.core.Jvm;

/**
 * Created by daniel on 08/03/2016.
 */
public class ExampleLatencyTest implements LatencyTask {
    private LatencyTestHarness lth;
    int count = 0;

    @Override
    public void run(long startTimeNS) {
        count++;
        if(count==610_000) {
            System.out.println("PAUSE");
            Jvm.pause(1000);
        }
        lth.sample(System.nanoTime()-startTimeNS);
    }

    @Override
    public void init(LatencyTestHarness lth) {

        this.lth = lth;
    }

    @Override
    public void complete() {
    }

    public static void main(String[] args) {
        LatencyTestHarness lth = new LatencyTestHarness()
                .warmUp(500_000)
                .messageCount(100_000)
                .throughput(25_000)
                .accountForCoordinatedOmmission(true)
                .runs(4)
                .build(new ExampleLatencyTest());
        lth.start();
    }
}
