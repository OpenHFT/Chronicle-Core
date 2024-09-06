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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

/**
 * A mute implementation of the {@link AnalyticsFacade.Builder} interface.
 * <p>
 * This singleton enum represents a builder that ignores all configuration settings for analytics
 * and returns a {@link MuteAnalytics} instance. It is used when analytics are disabled or not required,
 * ensuring that no analytics events or configurations are processed.
 * </p>
 * <p>
 * All methods in this class simply validate input arguments and return the current builder instance
 * without performing any actions, making it safe to use in scenarios where analytics should be muted.
 * </p>
 */
public enum MuteBuilder implements AnalyticsFacade.Builder {

    INSTANCE;  // Singleton instance for mute builder

    /**
     * Mute implementation for setting a user property. This method validates the input but does nothing else.
     *
     * @param key   The user property key (validated but ignored).
     * @param value The user property value (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder putUserProperty(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        return this;
    }

    /**
     * Mute implementation for setting an event parameter. This method validates the input but does nothing else.
     *
     * @param key   The event parameter key (validated but ignored).
     * @param value The event parameter value (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder putEventParameter(@NotNull final String key, @NotNull final String value) {
        requireNonNull(key);
        requireNonNull(value);
        return this;
    }

    /**
     * Mute implementation for configuring a frequency limit for sending analytics messages.
     * This method validates the input but does nothing else.
     *
     * @param messages The number of messages allowed (ignored).
     * @param duration The time duration (ignored).
     * @param timeUnit The time unit for the duration (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withFrequencyLimit(final int messages,
                                                               final long duration,
                                                               @NotNull final TimeUnit timeUnit) {
        requireNonNull(timeUnit);
        return this;
    }

    /**
     * Mute implementation for setting an error logger. This method validates the input but does nothing else.
     *
     * @param errorLogger The error logger (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withErrorLogger(@NotNull final Consumer<? super String> errorLogger) {
        requireNonNull(errorLogger);
        return this;
    }

    /**
     * Mute implementation for setting a debug logger. This method validates the input but does nothing else.
     *
     * @param debugLogger The debug logger (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withDebugLogger(@NotNull final Consumer<? super String> debugLogger) {
        requireNonNull(debugLogger);
        return this;
    }

    /**
     * Mute implementation for setting the client ID file name. This method validates the input but does nothing else.
     *
     * @param clientIdFileName The client ID file name (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withClientIdFileName(@NotNull final String clientIdFileName) {
        requireNonNull(clientIdFileName);
        return this;
    }

    /**
     * Mute implementation for setting the analytics server URL. This method validates the input but does nothing else.
     *
     * @param url The URL of the analytics server (validated but ignored).
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withUrl(@NotNull final String url) {
        requireNonNull(url);
        return this;
    }

    /**
     * Mute implementation for enabling analytics reporting even in JUnit tests. This method does nothing.
     *
     * @return The current instance of {@link MuteBuilder}.
     */
    @Override
    public AnalyticsFacade.@NotNull Builder withReportDespiteJUnit() {
        return this;
    }

    /**
     * Builds and returns the {@link MuteAnalytics} instance, which is the mute implementation of analytics.
     *
     * @return A singleton instance of {@link MuteAnalytics}.
     */
    @Override
    public @NotNull AnalyticsFacade build() {
        return MuteAnalytics.INSTANCE;
    }
}
