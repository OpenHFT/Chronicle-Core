package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ReflectiveBuilder implements AnalyticsFacade.Builder {

    private final static String CLASS_NAME = "net.openhft.chronicle.analytics.Analytics$Builder";

    private final Object delegate;

    public ReflectiveBuilder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        this.delegate = ReflectionUtil.analyticsBuilder(measurementId, apiSecret);
    }

    @Override
    public AnalyticsFacade.@NotNull Builder putUserProperty(@NotNull String key, @NotNull String value) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "putUserProperty", String.class, String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, key, value);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder putEventParameter(@NotNull String key, @NotNull String value) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "putEventParameter", String.class, String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, key, value);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withFrequencyLimit(int messages, long duration, @NotNull TimeUnit timeUnit) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withFrequencyLimit", int.class, long.class, TimeUnit.class);
        ReflectionUtil.invokeOrThrow(m, delegate, messages, duration, timeUnit);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withErrorLogger(@NotNull Consumer<String> errorLogger) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withErrorLogger", Consumer.class);
        ReflectionUtil.invokeOrThrow(m, delegate, errorLogger);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withDebugLogger(@NotNull Consumer<String> debugLogger) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withDebugLogger", Consumer.class);
        ReflectionUtil.invokeOrThrow(m, delegate, debugLogger);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withClientIdFileName(@NotNull String clientIdFileName) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withClientIdFileName", String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, clientIdFileName);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withUrl(@NotNull String url) {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withUrl", String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, url);
        return this;
    }

    @Override
    public AnalyticsFacade.@NotNull Builder withReportDespiteJUnit() {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withReportDespiteJUnit");
        ReflectionUtil.invokeOrThrow(m, delegate);
        return this;
    }

    @Override
    public @NotNull AnalyticsFacade build() {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "build");
        final Object analytics = ReflectionUtil.invokeOrThrow(m, delegate);
        return new ReflectiveAnalytics(analytics);
    }

}