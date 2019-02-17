package net.openhft.chronicle.core.jlbh;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;

class ImmutableJLBHResult implements JLBHResult {

    @NotNull
    private final ProbeResult endToEndProbeResult;
    @NotNull
    private final Map<String, ProbeResult> additionalProbeResults;

    ImmutableJLBHResult(@NotNull ProbeResult endToEndProbeResult, @NotNull Map<String, ? extends ProbeResult> additionalProbeResults) {
        this.endToEndProbeResult = endToEndProbeResult;
        this.additionalProbeResults = unmodifiableMap(additionalProbeResults);
    }

    @Override
    @NotNull
    public ProbeResult endToEnd() {
        return endToEndProbeResult;
    }

    @Override
    @NotNull
    public Optional<ProbeResult> probe(String probeName) {
        return Optional.ofNullable(additionalProbeResults.get(probeName));
    }
}
