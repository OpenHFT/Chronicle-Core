package net.openhft.chronicle.core.jlbh;

/**
 * Data structure to store the options to pass into the JLBH constructor
 */
public class JLBHOptions {
    int throughput = 10_000;
    boolean accountForCoordinatedOmission = true;
    int recordJitterGreaterThanNs = 1_000;
    boolean recordOSJitter = true;
    int warmUpIterations = 10_000;
    int runs = 3;
    int iterations = 100_000;
    JLBHTask jlbhTask;
    int pauseAfterWarmupMS = 0;
    SKIP_FIRST_RUN skipFirstRun = SKIP_FIRST_RUN.NOT_SET;
    boolean jitterAffinity;

    /**
     * Number of iterations per second to be pushed through the benchmark
     *
     * @param throughput defaults to 10,000
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions throughput(int throughput) {
        this.throughput = throughput;
        return this;
    }

    /**
     * Determines whether the start time is the time the event was supposed to have happened
     * (i.e. accounting for co-ordinated ommission) or whether the the start time is just
     * the a factor of the throughput (i.e. not accounting for co-ordinated ommission).
     * @param accountForCoordinatedOmmission defaults to true
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions accountForCoordinatedOmmission(boolean accountForCoordinatedOmmission) {
        this.accountForCoordinatedOmission = accountForCoordinatedOmmission;
        return this;
    }

    /**
     * Determines how much jitter to record.
     * @param recordJitterGreaterThanNs Defaults to 1000
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions recordJitterGreaterThanNs(int recordJitterGreaterThanNs) {
        this.recordJitterGreaterThanNs = recordJitterGreaterThanNs;
        return this;
    }

    /**
     * Determines whether or not to record jitter
     * @param recordOSJitter Defaults to true
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions recordOSJitter(boolean recordOSJitter) {
        this.recordOSJitter = recordOSJitter;
        return this;
    }

    /**
     * Determines how many warmup iterations to perform.
     * Note: warmup iterations are continuous.
     * @param warmUp Defaults to 10,000
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions warmUpIterations(int warmUp) {
        this.warmUpIterations = warmUp;
        return this;
    }

    /**
     * Number of runs of the benchmark
     * @param runs Defaults to 3
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions runs(int runs) {
        this.runs = runs;
        return this;
    }

    /**
     * Number of iterations of the benchmark not including warmup.
     * @param iterations Defaults to 100,000
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions iterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    /**
     * The latency benchmark to be run.
     * @param JLBHTask
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions jlbhTask(JLBHTask JLBHTask) {
        this.jlbhTask = JLBHTask;
        return this;
    }

    /**
     * Option to set a pause after the warmup is complete
     * @param pauseMS pause in ms default to 0
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions pauseAfterWarmupMS(int pauseMS) {
        this.pauseAfterWarmupMS = pauseMS;
        return this;
    }

    /**
     * Option to skip first run from being included in the variation statistics.
     * @param skip defult to true if runs greater than 3
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions skipFirstRun(boolean skip) {
        skipFirstRun = skip ? SKIP_FIRST_RUN.SKIP : SKIP_FIRST_RUN.NO_SKIP;
        return this;
    }

    /**
     * Should the jitter thread set affinity or not
     * @param jitterAffinity default is false
     * @return Instance of the JLBHOptions to be used in the builder pattern.
     */
    public JLBHOptions jitterAffinity(boolean jitterAffinity) {
        this.jitterAffinity = jitterAffinity;
        return this;
    }

    enum SKIP_FIRST_RUN {
        NOT_SET, SKIP, NO_SKIP
    }
}
