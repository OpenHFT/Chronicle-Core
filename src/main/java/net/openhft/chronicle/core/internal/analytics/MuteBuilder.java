package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public enum MuteBuilder implements AnalyticsFacade.Builder {

    INSTANCE;

    @Override
    public AnalyticsFacade.@NotNull Builder putUserProperty(@NotNull String key, @NotNull String value) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder putEventParameter(@NotNull String key, @NotNull String value) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withFrequencyLimit(int messages, long duration, @NotNull TimeUnit timeUnit) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withErrorLogger(@NotNull Consumer<String> errorLogger) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withDebugLogger(@NotNull Consumer<String> debugLogger) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withClientIdFileName(@NotNull String clientIdFileName) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withUrl(@NotNull String url) { return this; }

    @Override
    public AnalyticsFacade.@NotNull Builder withReportDespiteJUnit() { return this; }

    @Override
    public @NotNull AnalyticsFacade build() { return MuteAnalytics.INSTANCE; }
}