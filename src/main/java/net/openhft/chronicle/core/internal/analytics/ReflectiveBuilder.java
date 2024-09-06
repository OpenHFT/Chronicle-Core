/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.internal.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * A reflective implementation of the {@link AnalyticsFacade.Builder} interface. This class
 * dynamically builds analytics objects through reflection, interacting with the underlying
 * analytics API.
 *
 * <p>This builder supports configuring analytics parameters, including user properties,
 * event parameters, logging, and more. The reflection mechanism allows the builder to
 * interface with an external analytics library, even when it is not available at compile-time.</p>
 */
public final class ReflectiveBuilder implements AnalyticsFacade.Builder {

    private static final String CLASS_NAME = "net.openhft.chronicle.analytics.Analytics$Builder";

    private final Object delegate;

    /**
     * Constructs a new {@code ReflectiveBuilder} instance.
     *
     * @param measurementId The measurement ID for the analytics service. Must not be null.
     * @param apiSecret     The API secret for the analytics service. Must not be null.
     * @throws NullPointerException If either parameter is null.
     */
    public ReflectiveBuilder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        requireNonNull(measurementId);
        requireNonNull(apiSecret);
        this.delegate = ReflectionUtil.analyticsBuilder(measurementId, apiSecret);
    }

    /**
     * Adds a user property to the analytics configuration.
     *
     * @param key   The user property key. Must not be null.
     * @param value The user property value. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If either the key or value is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder putUserProperty(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "putUserProperty", String.class, String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, key, value);
        return this;
    }

    /**
     * Adds an event parameter to the analytics configuration.
     *
     * @param key   The event parameter key. Must not be null.
     * @param value The event parameter value. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If either the key or value is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder putEventParameter(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "putEventParameter", String.class, String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, key, value);
        return this;
    }

    /**
     * Configures the frequency limit for sending analytics events.
     *
     * @param messages The maximum number of messages.
     * @param duration The time duration for the limit.
     * @param timeUnit The unit of time for the duration. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If the {@code timeUnit} is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withFrequencyLimit(final int messages,
                                                               final long duration,
                                                               @NotNull final TimeUnit timeUnit) {
        requireNonNull(timeUnit);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withFrequencyLimit", int.class, long.class, TimeUnit.class);
        ReflectionUtil.invokeOrThrow(m, delegate, messages, duration, timeUnit);
        return this;
    }

    /**
     * Sets a logger for error messages.
     *
     * @param errorLogger The logger for error messages. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If the {@code errorLogger} is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withErrorLogger(@NotNull final Consumer<? super String> errorLogger) {
        requireNonNull(errorLogger);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withErrorLogger", Consumer.class);
        ReflectionUtil.invokeOrThrow(m, delegate, errorLogger);
        return this;
    }

    /**
     * Sets a logger for debug messages.
     *
     * @param debugLogger The logger for debug messages. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If the {@code debugLogger} is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withDebugLogger(@NotNull Consumer<? super String> debugLogger) {
        requireNonNull(debugLogger);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withDebugLogger", Consumer.class);
        ReflectionUtil.invokeOrThrow(m, delegate, debugLogger);
        return this;
    }

    /**
     * Sets the filename for the client ID.
     *
     * @param clientIdFileName The filename for the client ID. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If {@code clientIdFileName} is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withClientIdFileName(@NotNull String clientIdFileName) {
        requireNonNull(clientIdFileName);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withClientIdFileName", String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, clientIdFileName);
        return this;
    }

    /**
     * Sets the URL for sending analytics data.
     *
     * @param url The URL to use. Must not be null.
     * @return The builder instance, allowing for method chaining.
     * @throws NullPointerException If {@code url} is null.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withUrl(@NotNull String url) {
        requireNonNull(url);
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withUrl", String.class);
        ReflectionUtil.invokeOrThrow(m, delegate, url);
        return this;
    }

    /**
     * Enables reporting analytics data even when running under JUnit.
     *
     * @return The builder instance, allowing for method chaining.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withReportDespiteJUnit() {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "withReportDespiteJUnit");
        ReflectionUtil.invokeOrThrow(m, delegate);
        return this;
    }

    /**
     * Builds the {@link AnalyticsFacade} instance using the current configuration.
     *
     * @return A new {@link AnalyticsFacade} instance.
     */
    @Override
    public @NotNull AnalyticsFacade build() {
        final Method m = ReflectionUtil.methodOrThrow(CLASS_NAME, "build");
        final Object analytics = ReflectionUtil.invokeOrThrow(m, delegate);
        return new ReflectiveAnalytics(analytics);
    }
}
