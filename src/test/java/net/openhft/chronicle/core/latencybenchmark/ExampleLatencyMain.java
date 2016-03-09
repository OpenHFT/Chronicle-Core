package net.openhft.chronicle.core.latencybenchmark;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.NanoSampler;

/**
 * Created by daniel on 08/03/2016.
 */
public class ExampleLatencyMain implements LatencyTask {
    int count = 0;
    private LatencyTestHarness lth;
    private NanoSampler nanoSamplerSin;
    private NanoSampler nanoSamplerWait;

    public static void main(String[] args) {
        LatencyTestHarness lth = new LatencyTestHarness()
                .warmUp(500_000)
                .messageCount(20_000)
                .throughput(25_000)
                .accountForCoordinatedOmmission(true)
                .runs(7)
                .build(new ExampleLatencyMain());
        lth.start();
    }

    double sin;
    @Override
    public void run(long startTimeNS) {
        count++;
        if(count%200==0) {
            long now = System.nanoTime();
            Jvm.busyWaitMicros(1);
            nanoSamplerWait.sampleNanos(System.nanoTime()-now);
        }

        long now = System.nanoTime();
        sin = Math.sin(count);
        nanoSamplerSin.sampleNanos(System.nanoTime()-now);

        lth.sample(System.nanoTime()-startTimeNS);
    }

    @Override
    public void init(LatencyTestHarness lth) {

        this.lth = lth;
        nanoSamplerSin = lth.createAdditionalSampler("sin");
        nanoSamplerWait = lth.createAdditionalSampler("busyWait");
    }

    @Override
    public void complete() {
    }
}
