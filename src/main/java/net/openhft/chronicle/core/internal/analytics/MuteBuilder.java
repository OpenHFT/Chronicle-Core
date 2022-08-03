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