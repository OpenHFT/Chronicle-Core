package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.openhft.chronicle.core.jlbh.JLBHDeterministicFixtures.*;
import static net.openhft.chronicle.core.jlbh.JLBHResult.RunResult.Percentile.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class JLBHTest {

    @Test
    public void shouldWriteResultToTheOutputProvided() throws Exception {
        // given
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JLBH jlbh = new JLBH(options(), new PrintStream(outputStream), resultConsumer());

        // when
        jlbh.start();

        // then
        String result = outputStream.toString().replace("\r", "");
        assertThat(result, containsString("OS Jitter"));
        assertThat(result, containsString("Warm up complete (500 iterations took "));
        assertThat(result, containsString("Run time: "));
        assertThat(withoutNonDeterministicFields(result), equalTo(withoutNonDeterministicFields(predictableTaskExpectedResult())));
    }

    @Test
    /**
     * To understand the data, please go to JLBHDeterministicFixtures
     * and JLBHDeterministicFixtures::expectedOutput in particular
     */
    public void shouldProvideResultData() throws Exception {
        // given
        final JLBHResultConsumer resultConsumer = resultConsumer();
        final JLBH jlbh = new JLBH(options(), printStream(), resultConsumer);

        // when
        jlbh.start();

        // then
        final JLBHResult.RunResult lastRunSummary = resultConsumer.get().endToEnd().summaryOfLastRun();
        assertThat(lastRunSummary.get50thPercentile().toNanos(), equalTo(6_106L));
        assertThat(lastRunSummary.get90thPercentile().toNanos(), equalTo(9_708L));
        assertThat(lastRunSummary.get99thPercentile().toNanos(), equalTo(10_516L));
        assertThat(lastRunSummary.get999thPercentile().toNanos(), equalTo(10_596L));
        assertThat(lastRunSummary.get9999thPercentile().toNanos(), equalTo(10_604L));
        assertThat(lastRunSummary.getWorst().toNanos(), equalTo(10_604L));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_50TH), equalTo(lastRunSummary.get50thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_90TH), equalTo(lastRunSummary.get90thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99TH), equalTo(lastRunSummary.get99thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99_9TH), equalTo(lastRunSummary.get999thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99_99TH), equalTo(lastRunSummary.get9999thPercentile()));
        assertNull(lastRunSummary.percentiles().get(PERCENTILE_99_999TH));
        assertNull(lastRunSummary.percentiles().get(PERCENTILE_99_9999TH));
        assertNull(lastRunSummary.percentiles().get(PERCENTILE_99_9999TH));
        assertThat(lastRunSummary.percentiles().get(WORST), equalTo(lastRunSummary.getWorst()));

        final List<JLBHResult.RunResult> summaryOfEachRun = resultConsumer.get().endToEnd().eachRunSummary();
        assertThat(summaryOfEachRun.size(), equalTo(3));
        assertThat(summaryOfEachRun.get(0), not(equalTo(lastRunSummary)));
        assertThat(summaryOfEachRun.get(1), not(equalTo(lastRunSummary)));
        assertThat(summaryOfEachRun.get(2), equalTo(lastRunSummary));

        assertThat(resultConsumer.get().probe("A").isPresent(), equalTo(true));
        assertThat(resultConsumer.get().probe("B").isPresent(), equalTo(true));
        assertThat(resultConsumer.get().probe("C").isPresent(), equalTo(false));

        final JLBHResult.RunResult probeALastRunSummary = resultConsumer.get().probe("A").get().summaryOfLastRun();
        assertThat(probeALastRunSummary.get50thPercentile().toNanos(), equalTo(5_106L));
        assertThat(probeALastRunSummary.get90thPercentile().toNanos(), equalTo(8_708L));
        assertThat(probeALastRunSummary.get99thPercentile().toNanos(), equalTo(9_516L));
        assertThat(probeALastRunSummary.get999thPercentile().toNanos(), equalTo(9_596L));
        assertThat(probeALastRunSummary.get9999thPercentile().toNanos(), equalTo(9_604L));
        assertThat(probeALastRunSummary.getWorst().toNanos(), equalTo(9_604L));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_50TH), equalTo(probeALastRunSummary.get50thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_90TH), equalTo(probeALastRunSummary.get90thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99TH), equalTo(probeALastRunSummary.get99thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99_9TH), equalTo(probeALastRunSummary.get999thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99_99TH), equalTo(probeALastRunSummary.get9999thPercentile()));
        assertNull(probeALastRunSummary.percentiles().get(PERCENTILE_99_999TH));
        assertNull(probeALastRunSummary.percentiles().get(PERCENTILE_99_9999TH));
        assertNull(probeALastRunSummary.percentiles().get(PERCENTILE_99_9999TH));
        assertThat(probeALastRunSummary.percentiles().get(WORST), equalTo(probeALastRunSummary.getWorst()));

        final List<JLBHResult.RunResult> summaryOfProbeAEachRun = resultConsumer.get().probe("A").get().eachRunSummary();
        assertThat(summaryOfProbeAEachRun.size(), equalTo(3));
        assertThat(summaryOfProbeAEachRun.get(0), not(equalTo(probeALastRunSummary)));
        assertThat(summaryOfProbeAEachRun.get(1), not(equalTo(probeALastRunSummary)));
        assertThat(summaryOfProbeAEachRun.get(2), equalTo(probeALastRunSummary));
    }

    @Test
    @Ignore("Test passes but takes ~10s to run, so ignored not to slow down the build")
    public void shouldProvideHigherPercentilesIfEnoughSamples() throws Exception {
        // given
        final JLBHOptions optionsA = options().runs(1).jlbhTask(new FixedLatencyJLBHTask(777)).iterations(1_000_000);
        final JLBHOptions optionsB = options().runs(1).jlbhTask(new FixedLatencyJLBHTask(777)).iterations(10_000_000);
        final JLBHResultConsumer resultConsumerA = resultConsumer();
        final JLBHResultConsumer resultConsumerB = resultConsumer();
        new JLBH(optionsA, printStream(), resultConsumerA).start();
        new JLBH(optionsB, printStream(), resultConsumerB).start();

        // when
        final JLBHResult.RunResult summaryA = resultConsumerA.get().endToEnd().summaryOfLastRun();
        final JLBHResult.RunResult summaryB = resultConsumerB.get().endToEnd().summaryOfLastRun();

        // then
        assertNotNull(summaryA.percentiles().get(PERCENTILE_99_99TH));
        assertNotNull(summaryA.percentiles().get(PERCENTILE_99_999TH));
        assertNull(summaryA.percentiles().get(PERCENTILE_99_9999TH));

        assertNotNull(summaryA.percentiles().get(PERCENTILE_99_99TH));
        assertNotNull(summaryB.percentiles().get(PERCENTILE_99_999TH));
        assertNotNull(summaryB.percentiles().get(PERCENTILE_99_9999TH));

        assertThat(percentilesUniqueLatenciesIn(summaryA).size(), equalTo(1));
        assertThat(percentilesUniqueLatenciesIn(summaryB).size(), equalTo(1));
    }

    private Set<Duration> percentilesUniqueLatenciesIn(JLBHResult.RunResult summaryA) {
        return summaryA.percentiles().values().stream().collect(Collectors.toSet());
    }

    @NotNull
    private JLBHResultConsumer resultConsumer() {
        return JLBHResultConsumer.newThreadSafeInstance();
    }

    @NotNull
    private PrintStream printStream() {
        return new PrintStream(new ByteArrayOutputStream());
    }
}