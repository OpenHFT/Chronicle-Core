package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

public enum MuteBuilder implements AnalyticsFacade.Builder {

    INSTANCE;

    @Override
    public AnalyticsFacade.@NotNull Builder putUserProperty(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder putEventParameter(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withFrequencyLimit(final int messages,
                                                               final long duration,
                                                               @NotNull final TimeUnit timeUnit) {
        requireNonNull(timeUnit);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withErrorLogger(@NotNull final Consumer<? super String> errorLogger) {
        requireNonNull(errorLogger);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withDebugLogger(@NotNull final Consumer<? super String> debugLogger) {
        requireNonNull(debugLogger);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withClientIdFileName(@NotNull final String clientIdFileName) {
        requireNonNull(clientIdFileName);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withUrl(@NotNull final String url) {
        requireNonNull(url);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withReportDespiteJUnit() {
        return this;
    }

    @Override
    public @NotNull AnalyticsFacade build() {
        return MuteAnalytics.INSTANCE;
    }
}