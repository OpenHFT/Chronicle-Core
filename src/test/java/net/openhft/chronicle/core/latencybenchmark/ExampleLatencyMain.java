package net.openhft.chronicle.core.latencybenchmark;

/**
 * Created by daniel on 08/03/2016.
 */
public class ExampleLatencyMain implements LatencyTask {
    int count = 0;
    private LatencyTestHarness lth;

    public static void main(String[] args) {
        LatencyTestHarness lth = new LatencyTestHarness()
                .warmUp(50_000)
                .messageCount(10_000)
                .throughput(25_000)
                .accountForCoordinatedOmmission(true)
                .runs(3)
                .build(new ExampleLatencyMain());
        lth.start();
    }

    @Override
    public void run(long startTimeNS) {
        count++;
        if(count==60_000) {
            //System.out.println("PAUSE");
            //Jvm.pause(1000);
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
}
