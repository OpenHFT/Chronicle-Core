/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.analytics;

import net.openhft.chronicle.core.analytics.AnalyticsFacade;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Test double that mimics the public API expected by {@code ReflectionUtil} and {@code ReflectiveBuilder}.
 */
public interface Analytics extends AnalyticsFacade {

    static Builder builder(@NotNull final String measurementId, @NotNull final String apiSecret) {
        return new Builder(measurementId, apiSecret);
    }

    final class Builder {
        private final String measurementId;
        private final String apiSecret;
        private final Map<String, String> userProps = new LinkedHashMap<>();
        private final Map<String, String> eventParams = new LinkedHashMap<>();
        private int frequencyMessages;
        private long frequencyDuration;
        private TimeUnit frequencyUnit = TimeUnit.HOURS;
        private Consumer<? super String> errorLogger = s -> { };
        private Consumer<? super String> debugLogger = s -> { };
        private String clientIdFileName = "";
        private String url = "";
        private boolean reportDespiteJUnit;

        private Builder(final String measurementId, final String apiSecret) {
            this.measurementId = Objects.requireNonNull(measurementId);
            this.apiSecret = Objects.requireNonNull(apiSecret);
        }

        public Builder putUserProperty(final String key, final String value) {
            userProps.put(key, value);
            return this;
        }

        public Builder putEventParameter(final String key, final String value) {
            eventParams.put(key, value);
            return this;
        }

        public Builder withFrequencyLimit(final int messages, final long duration, final TimeUnit timeUnit) {
            this.frequencyMessages = messages;
            this.frequencyDuration = duration;
            this.frequencyUnit = Objects.requireNonNull(timeUnit);
            return this;
        }

        public Builder withErrorLogger(final Consumer<? super String> logger) {
            this.errorLogger = Objects.requireNonNull(logger);
            return this;
        }

        public Builder withDebugLogger(final Consumer<? super String> logger) {
            this.debugLogger = Objects.requireNonNull(logger);
            return this;
        }

        public Builder withClientIdFileName(final String fileName) {
            this.clientIdFileName = Objects.requireNonNull(fileName);
            return this;
        }

        public Builder withUrl(final String url) {
            this.url = Objects.requireNonNull(url);
            return this;
        }

        public Builder withReportDespiteJUnit() {
            this.reportDespiteJUnit = true;
            return this;
        }

        public AnalyticsFacade build() {
            return new RecordingFacade(
                    measurementId,
                    apiSecret,
                    userProps,
                    eventParams,
                    frequencyMessages,
                    frequencyDuration,
                    frequencyUnit,
                    errorLogger,
                    debugLogger,
                    clientIdFileName,
                    url,
                    reportDespiteJUnit);
        }
    }

    final class RecordingFacade implements Analytics {
        private final String measurementId;
        private final String apiSecret;
        private final Map<String, String> userProps;
        private final Map<String, String> eventParams;
        private final int frequencyMessages;
        private final long frequencyDuration;
        private final TimeUnit frequencyUnit;
        private final Consumer<? super String> errorLogger;
        private final Consumer<? super String> debugLogger;
        private final String clientIdFileName;
        private final String url;
        private final boolean reportDespiteJUnit;
        private final Map<String, Map<String, String>> emittedEvents = new LinkedHashMap<>();

        RecordingFacade(final String measurementId,
                         final String apiSecret,
                         final Map<String, String> userProps,
                         final Map<String, String> eventParams,
                         final int frequencyMessages,
                         final long frequencyDuration,
                         final TimeUnit frequencyUnit,
                         final Consumer<? super String> errorLogger,
                         final Consumer<? super String> debugLogger,
                         final String clientIdFileName,
                         final String url,
                         final boolean reportDespiteJUnit) {
            this.measurementId = measurementId;
            this.apiSecret = apiSecret;
            this.userProps = new LinkedHashMap<>(userProps);
            this.eventParams = new LinkedHashMap<>(eventParams);
            this.frequencyMessages = frequencyMessages;
            this.frequencyDuration = frequencyDuration;
            this.frequencyUnit = frequencyUnit;
            this.errorLogger = errorLogger;
            this.debugLogger = debugLogger;
            this.clientIdFileName = clientIdFileName;
            this.url = url;
            this.reportDespiteJUnit = reportDespiteJUnit;
        }

        @Override
        public void sendEvent(@NotNull final String name, @NotNull final Map<String, String> additionalEventParameters) {
            final Map<String, String> combined = new LinkedHashMap<>(eventParams);
            combined.putAll(additionalEventParameters);
            emittedEvents.put(name, combined);
        }

        public Map<String, String> userProperties() {
            return Collections.unmodifiableMap(userProps);
        }

        public Map<String, Map<String, String>> emittedEvents() {
            return Collections.unmodifiableMap(emittedEvents);
        }

        public String measurementId() {
            return measurementId;
        }

        public String apiSecret() {
            return apiSecret;
        }

        public int frequencyMessages() {
            return frequencyMessages;
        }

        public long frequencyDuration() {
            return frequencyDuration;
        }

        public TimeUnit frequencyUnit() {
            return frequencyUnit;
        }

        public Consumer<? super String> errorLogger() {
            return errorLogger;
        }

        public Consumer<? super String> debugLogger() {
            return debugLogger;
        }

        public String clientIdFileName() {
            return clientIdFileName;
        }

        public String url() {
            return url;
        }

        public boolean reportDespiteJUnit() {
            return reportDespiteJUnit;
        }
    }
}
