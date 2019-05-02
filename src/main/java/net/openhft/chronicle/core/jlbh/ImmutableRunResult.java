package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.Collections.unmodifiableMap;
import static net.openhft.chronicle.core.jlbh.JLBHResult.RunResult.Percentile.*;

class ImmutableRunResult implements JLBHResult.RunResult {

    private final Map<Percentile, Duration> percentiles;

    public ImmutableRunResult(double[] percentiles) {
        this.percentiles = asMap(percentiles);
    }

    private static Map<Percentile, Duration> asMap(double[] percentiles) {
        final Map<Percentile, Duration> data = new EnumMap<>(Percentile.class);
        // TODO: duplicate of knowledge with Histogram.percentilesFor()
        data.put(PERCENTILE_50TH, durationOf(percentiles[0]));
        data.put(PERCENTILE_90TH, durationOf(percentiles[1]));
        data.put(PERCENTILE_99TH, durationOf(percentiles[2]));
        if (percentiles.length > 4) {
            data.put(PERCENTILE_99_7TH, durationOf(percentiles[3]));
        }
        if (percentiles.length > 5) {
            data.put(PERCENTILE_99_9TH, durationOf(percentiles[4]));
        }
        if (percentiles.length > 6) {
            data.put(PERCENTILE_99_97TH, durationOf(percentiles[5]));
        }
        if (percentiles.length > 7) {
            data.put(PERCENTILE_99_99TH, durationOf(percentiles[6]));
        }
        data.put(WORST, durationOf(percentiles[percentiles.length - 1]));
        return unmodifiableMap(data);
    }

    private static Duration durationOf(double percentile) {
        return Duration.of((long) percentile, NANOS);
    }

    @Override
    @NotNull
    public Map<Percentile, Duration> percentiles() {
        return percentiles;
    }

    @Override
    @NotNull
    public Duration get50thPercentile() {
        return percentiles.get(PERCENTILE_50TH);
    }

    @Override
    @NotNull
    public Duration get90thPercentile() {
        return percentiles.get(PERCENTILE_90TH);
    }

    @Override
    @NotNull
    public Duration get99thPercentile() {
        return percentiles.get(PERCENTILE_99TH);
    }

    @Override
    @NotNull
    public Duration get999thPercentile() {
        return percentiles.get(PERCENTILE_99_9TH);
    }

    @Override
    @NotNull
    public Duration get9999thPercentile() {
        return percentiles.get(PERCENTILE_99_99TH);
    }

    @Override
    @NotNull
    public Duration getWorst() {
        return percentiles.get(WORST);
    }

    @Override
    public String toString() {
        return "ImmutableRunResult{" +
                "percentiles=" + percentiles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableRunResult summary = (ImmutableRunResult) o;

        return percentiles.equals(summary.percentiles);

    }

    @Override
    public int hashCode() {
        return percentiles.hashCode();
    }
}
