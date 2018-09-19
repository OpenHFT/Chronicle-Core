package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;
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
    public void shouldWriteResultToTheOutputProvided() {
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
    /*
     * To understand the data, please go to JLBHDeterministicFixtures
     * and JLBHDeterministicFixtures::expectedOutput in particular
     */
    public void shouldProvideResultData() {
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
        assertThat(lastRunSummary.getWorst().toNanos(), equalTo(10_604L));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_50TH), equalTo(lastRunSummary.get50thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_90TH), equalTo(lastRunSummary.get90thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99TH), equalTo(lastRunSummary.get99thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99_9TH), equalTo(lastRunSummary.get999thPercentile()));
        assertThat(lastRunSummary.percentiles().get(PERCENTILE_99_99TH), equalTo(lastRunSummary.get9999thPercentile()));
        assertNull(lastRunSummary.percentiles().get(PERCENTILE_99_999TH));
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
//        assertThat(probeALastRunSummary.get9999thPercentile().toNanos(), equalTo(9_604L));
        assertThat(probeALastRunSummary.getWorst().toNanos(), equalTo(9_604L));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_50TH), equalTo(probeALastRunSummary.get50thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_90TH), equalTo(probeALastRunSummary.get90thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99TH), equalTo(probeALastRunSummary.get99thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99_9TH), equalTo(probeALastRunSummary.get999thPercentile()));
        assertThat(probeALastRunSummary.percentiles().get(PERCENTILE_99_99TH), equalTo(probeALastRunSummary.get9999thPercentile()));
        assertNull(probeALastRunSummary.percentiles().get(PERCENTILE_99_999TH));
        assertThat(probeALastRunSummary.percentiles().get(WORST), equalTo(probeALastRunSummary.getWorst()));

        final List<JLBHResult.RunResult> summaryOfProbeAEachRun = resultConsumer.get().probe("A").get().eachRunSummary();
        assertThat(summaryOfProbeAEachRun.size(), equalTo(3));
        assertThat(summaryOfProbeAEachRun.get(0), not(equalTo(probeALastRunSummary)));
        assertThat(summaryOfProbeAEachRun.get(1), not(equalTo(probeALastRunSummary)));
        assertThat(summaryOfProbeAEachRun.get(2), equalTo(probeALastRunSummary));
    }

    @Test
    public void shouldProvideResultDataEvenIfProbesDoNotProvideSameShapedData() {
        // given
        final JLBHResultConsumer resultConsumer = resultConsumer();
        JLBHOptions jlbhOptions = options().jlbhTask(new PredictableJLBHTaskDifferentShape()).iterations(ITERATIONS * 2);
        final JLBH jlbh = new JLBH(jlbhOptions, printStream(), resultConsumer);

        // when
        jlbh.start();

        // then
        final JLBHResult.RunResult probeALastRunSummary = resultConsumer.get().probe("A").get().summaryOfLastRun();
        assertThat(probeALastRunSummary.percentiles().size(), equalTo(5));

        final JLBHResult.RunResult probeBLastRunSummary = resultConsumer.get().probe("B").get().summaryOfLastRun();
        assertThat(probeBLastRunSummary.percentiles().size(), equalTo(4));
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