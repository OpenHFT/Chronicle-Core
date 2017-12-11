package net.openhft.chronicle.core.jlbh;

import net.openhft.chronicle.core.util.NanoSampler;

public class JLBHDeterministicFixtures {

  static final int WARM_UP_ITERATIONS = 500;
  static final int ITERATIONS = 9_000;
  static final int THROUGHPUT = 1_000_000;
  static final int RUNS = 3;

  static JLBHOptions options() {
    return new JLBHOptions()
            .warmUpIterations(WARM_UP_ITERATIONS)
            .iterations(ITERATIONS)
            .throughput(THROUGHPUT)
            .accountForCoordinatedOmmission(true)
            .runs(RUNS)
            .accountForCoordinatedOmmission(true)
            .jlbhTask(new PredictableJLBHTask());
  }

  static String predictableTaskExpectedResult() {
    return expectedOutput;
  }

  static String withoutNonDeterministicFields(String content) {
    return content
            .replaceAll("Warm up complete \\(\\d+ iterations took .+s\\)", "Warm up complete ...")
            .replaceAll("OS Jitter .+", "OS Jitter ...")
            .replaceAll("Run time: .+s", "Run time: ...");
  }

  static class PredictableJLBHTask implements JLBHTask {

    private int nanoTime = 1_000_000;
    private int latency;
    private JLBH lth;
    private NanoSampler additionalSamplerA;
    private NanoSampler additionalSamplerB;

    @Override
    public void run(long startTimeNS) {
      latency = 1000 + (++this.nanoTime % 11567);
      lth.sample(latency);
      additionalSamplerA.sampleNanos(latency - 1000);
      additionalSamplerB.sampleNanos(100);
    }

    @Override
    public void init(JLBH lth) {
      this.lth = lth;
      this.additionalSamplerA = lth.addProbe("A");
      this.additionalSamplerB = lth.addProbe("B");
    }

    @Override
    public void complete() {
    }
  }

  static class FixedLatencyJLBHTask implements JLBHTask {

    private final int latency;
    private JLBH lth;
    private NanoSampler additionalSamplerA;
    private NanoSampler additionalSamplerB;

    public FixedLatencyJLBHTask(int latency) {
      this.latency = latency;
    }

    @Override
    public void run(long startTimeNS) {
      lth.sample(latency);
      additionalSamplerA.sampleNanos(latency);
      additionalSamplerB.sampleNanos(latency);
    }

    @Override
    public void init(JLBH lth) {
      this.lth = lth;
      this.additionalSamplerA = lth.addProbe("A");
      this.additionalSamplerB = lth.addProbe("B");
    }

    @Override
    public void complete() {
    }
  }

  private final static String expectedOutput = "Warm up complete (500 iterations took 0.092s)\n" +
          "-------------------------------- BENCHMARK RESULTS (RUN 1) --------------------------------------------------------\n" +
          "Run time: 0.09s\n" +
          "Correcting for co-ordinated:true\n" +
          "Target throughput:1000000/s = 1 message every 1us\n" +
          "End to End: (9,000)                             50/90 99/99.9 99.99 - worst was 8.1 / 12  13 / 13  13 - 13\n" +
          "A (9,001)                                       50/90 99/99.9 99.99 - worst was 7.0 / 10  12 / 12  12 - 12\n" +
          "B (9,001)                                       50/90 99/99.9 99.99 - worst was 0.10 / 0.10  0.10 / 0.10  0.10 - 0.10\n" +
          "OS Jitter (169)                                 50/90 99/99.9 99.99 - worst was 1.4 / 6.3  22 / 23  23 - 23\n" +
          "-------------------------------------------------------------------------------------------------------------------\n" +
          "-------------------------------- BENCHMARK RESULTS (RUN 2) --------------------------------------------------------\n" +
          "Run time: 0.09s\n" +
          "Correcting for co-ordinated:true\n" +
          "Target throughput:1000000/s = 1 message every 1us\n" +
          "End to End: (9,000)                             50/90 99/99.9 99.99 - worst was 8.1 / 12  13 / 13  13 - 13\n" +
          "A (9,000)                                       50/90 99/99.9 99.99 - worst was 7.0 / 10  12 / 12  12 - 12\n" +
          "B (9,000)                                       50/90 99/99.9 99.99 - worst was 0.10 / 0.10  0.10 / 0.10  0.10 - 0.10\n" +
          "OS Jitter (62)                                  50/90 99/99.9 99.99 - worst was 2.5 / 10  28 / 28  28 - 28\n" +
          "-------------------------------------------------------------------------------------------------------------------\n" +
          "-------------------------------- BENCHMARK RESULTS (RUN 3) --------------------------------------------------------\n" +
          "Run time: 0.09s\n" +
          "Correcting for co-ordinated:true\n" +
          "Target throughput:1000000/s = 1 message every 1us\n" +
          "End to End: (9,000)                             50/90 99/99.9 99.99 - worst was 6.0 / 9.5  10 / 10  10 - 10\n" +
          "A (9,000)                                       50/90 99/99.9 99.99 - worst was 5.0 / 9.0  9.5 / 9.5  9.5 - 9.5\n" +
          "B (9,000)                                       50/90 99/99.9 99.99 - worst was 0.10 / 0.10  0.10 / 0.10  0.10 - 0.10\n" +
          "OS Jitter (30)                                  50/90 99/99.9 99.99 - worst was 4.0 / 10  14 / 14  14 - 14\n" +
          "-------------------------------------------------------------------------------------------------------------------\n" +
          "-------------------------------- SUMMARY (end to end)------------------------------------------------------------\n" +
          "Percentile   run1         run2         run3      % Variation\n" +
          "50:             8.06         8.06         6.02        18.50\n" +
          "90:            11.52        11.52         9.47        12.60\n" +
          "99:            12.54        12.54        10.50        11.51\n" +
          "99.9:          12.54        12.54        10.50        11.51\n" +
          "99.99:         12.54        12.54        10.50        11.51\n" +
          "worst:         12.54        12.54        10.50        11.51\n" +
          "-------------------------------------------------------------------------------------------------------------------\n" +
          "-------------------------------- SUMMARY (A)------------------------------------------------------------\n" +
          "Percentile   run1         run2         run3      % Variation\n" +
          "50:             7.04         7.04         4.99        21.48\n" +
          "90:            10.50        10.50         8.96        10.26\n" +
          "99:            11.52        11.52         9.47        12.60\n" +
          "99.9:          11.52        11.52         9.47        12.60\n" +
          "99.99:         11.52        11.52         9.47        12.60\n" +
          "worst:         11.52        11.52         9.47        12.60\n" +
          "-------------------------------------------------------------------------------------------------------------------\n" +
          "-------------------------------- SUMMARY (B)------------------------------------------------------------\n" +
          "Percentile   run1         run2         run3      % Variation\n" +
          "50:             0.10         0.10         0.10         0.00\n" +
          "90:             0.10         0.10         0.10         0.00\n" +
          "99:             0.10         0.10         0.10         0.00\n" +
          "99.9:           0.10         0.10         0.10         0.00\n" +
          "99.99:          0.10         0.10         0.10         0.00\n" +
          "worst:          0.10         0.10         0.10         0.00\n" +
          "-------------------------------------------------------------------------------------------------------------------\n";
}
