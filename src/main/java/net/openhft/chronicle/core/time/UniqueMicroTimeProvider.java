/*
 * Copyright 2016-2020 chronicle.software
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
package net.openhft.chronicle.core.time;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link TimeProvider} implementation that ensures unique timestamps across threads within a single process.
 * It provides time in milliseconds, microseconds, and nanoseconds while ensuring that the time value is unique
 * even across rapid successive calls.
 *
 * <p> This implementation is particularly useful in environments where unique time stamps are critical and
 * the application might request them at a high rate. </p>
 */
public class UniqueMicroTimeProvider implements TimeProvider {
    public static final UniqueMicroTimeProvider INSTANCE = new UniqueMicroTimeProvider();

    private final AtomicLong lastIssuedTimeMicros = new AtomicLong();
    private TimeProvider provider = SystemTimeProvider.INSTANCE;

    /**
     * Constructs a new UniqueMicroTimeProvider.
     * <p>
     * This constructor initializes the time provider with zero. New instances are typically created for
     * testing purposes, as this class is stateful and maintains the last time value issued.
         */
    public UniqueMicroTimeProvider() {
        // Do nothing
    }

    /**
     * Sets the underlying time provider for this instance and initializes the last time value.
     *
     * @param provider The {@link TimeProvider} to use for time calculations.
     * @return The current {@code UniqueMicroTimeProvider} instance for fluent method chaining.
     */
    public UniqueMicroTimeProvider provider(TimeProvider provider) {
        this.provider = provider;
        lastIssuedTimeMicros.set(provider.currentTimeMicros());
        return this;
    }

    /**
     * Retrieves the current time in milliseconds, ensuring uniqueness across threads.
     *
     * @return The current unique time in milliseconds since the epoch.
     */
    @Override
    public long currentTimeMillis() {
        long proposedTimeMillis = provider.currentTimeMillis();
        while (true) {
            long lastTimeMicros = lastIssuedTimeMicros.get();
            long lastTimeMillis = lastTimeMicros / 1000;
            if (lastTimeMillis >= proposedTimeMillis) {
                validateMillisecondTimestamp(proposedTimeMillis, lastTimeMillis);
                proposedTimeMillis = lastTimeMillis + 1;
            }
            if (lastIssuedTimeMicros.compareAndSet(lastTimeMicros, proposedTimeMillis * 1000))
                return proposedTimeMillis;
            Jvm.nanoPause();
        }
    }

    /**
     * Retrieves the current time in microseconds, ensuring uniqueness across threads.
     * It increments the time value by one microsecond if necessary to guarantee uniqueness.
     *
     * @return The current unique time in microseconds since the epoch.
     */
    @Override
    public long currentTimeMicros() {
        long proposedTimeMicros = provider.currentTimeMicros();
        while (true) {
            long lastTimeMicros = lastIssuedTimeMicros.get();
            if (lastTimeMicros >= proposedTimeMicros) {
                validateMicrosecondTimestamp(proposedTimeMicros, lastTimeMicros);
                proposedTimeMicros = lastTimeMicros + 1;
            }
            if (lastIssuedTimeMicros.compareAndSet(lastTimeMicros, proposedTimeMicros))
                return proposedTimeMicros;
            Jvm.nanoPause();
        }
    }

    /**
     * Retrieves the current time in nanoseconds, ensuring uniqueness across threads.
     * It adapts the nanosecond time based on the microsecond value to maintain unique timestamps.
     *
     * @return The current unique time in nanoseconds since the epoch.
     */
    @Override
    public long currentTimeNanos() {
        long proposedTimeNanos = provider.currentTimeNanos();
        long proposedTimeMicros = proposedTimeNanos / 1000;
        while (true) {
            long lastTimeMicros = lastIssuedTimeMicros.get();
            if (lastTimeMicros >= proposedTimeNanos / 1000) {
                validateMicrosecondTimestamp(proposedTimeMicros, lastTimeMicros);
                proposedTimeMicros = lastTimeMicros + 1;
                proposedTimeNanos = proposedTimeMicros * 1000;
            }
            if (lastIssuedTimeMicros.compareAndSet(lastTimeMicros, proposedTimeMicros))
                return proposedTimeNanos;
            Jvm.nanoPause();
        }
    }

    /**
     * Asserts the correctness of millisecond time calculations.
     *
     * @param realTimeMS The actual time in milliseconds.
     * @param lastTimeMS The last time in milliseconds issued by this provider.
     */
    private void validateMillisecondTimestamp(long realTimeMS, long lastTimeMS) {
        assert (lastTimeMS - realTimeMS) < 1_000
                : "Exceeding 1,000 calls per second will advance time by more than 1 second.";
    }

    /**
     * Asserts the correctness of microsecond time calculations.
     *
     * @param realTimeUS The actual time in microseconds.
     * @param lastTimeUS The last time in microseconds issued by this provider.
     */
    private void validateMicrosecondTimestamp(long realTimeUS, long lastTimeUS) {
        assert (lastTimeUS - realTimeUS) < 1_000_000
                : "Exceeding 1 million calls per second will advance time by more than 1 second.";
    }
}
