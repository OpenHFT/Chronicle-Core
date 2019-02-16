package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

class ImmutableProbeResult implements JLBHResult.ProbeResult {

    @NotNull
    private final List<JLBHResult.RunResult> runsSummary;

    public ImmutableProbeResult(List<double[]> percentileRuns) {
        runsSummary = unmodifiableList(percentileRuns.stream().map(ImmutableRunResult::new).collect(toList()));
    }

    @NotNull
    @Override
    public JLBHResult.RunResult summaryOfLastRun() {
        return runsSummary.get(runsSummary.size() - 1);
    }

    @NotNull
    @Override
    public List<JLBHResult.RunResult> eachRunSummary() {
        return runsSummary;
    }
}
