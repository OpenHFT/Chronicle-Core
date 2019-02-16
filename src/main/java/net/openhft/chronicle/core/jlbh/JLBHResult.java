package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JLBHResult {

    @NotNull
    ProbeResult endToEnd();

    @NotNull
    Optional<ProbeResult> probe(String probeName);

    interface ProbeResult {

        @NotNull
        RunResult summaryOfLastRun();

        @NotNull
        List<RunResult> eachRunSummary();
    }

    interface RunResult {

        @NotNull
        Map<Percentile, Duration> percentiles();

        @NotNull
        Duration get50thPercentile();

        @NotNull
        Duration get90thPercentile();

        @NotNull
        Duration get99thPercentile();

        @NotNull
        Duration get999thPercentile();

        @NotNull
        Duration get9999thPercentile();

        @NotNull
        Duration getWorst();

        enum Percentile {
            PERCENTILE_50TH,
            PERCENTILE_90TH,
            PERCENTILE_99TH,
            PERCENTILE_99_7TH,
            PERCENTILE_99_9TH,
            PERCENTILE_99_97TH,
            PERCENTILE_99_99TH,
            PERCENTILE_99_999TH,
            WORST
        }
    }
}
