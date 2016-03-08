package net.openhft.chronicle.core.latencybenchmark;

/**
 * Created by daniel on 03/03/2016.
 */
public interface LatencyTask {
    void run(long startTimeNS);
    void init(LatencyTestHarness lth);
    void complete();
}
