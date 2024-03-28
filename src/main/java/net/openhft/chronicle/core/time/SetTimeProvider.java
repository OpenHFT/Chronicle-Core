/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link TimeProvider} implementation that allows explicit setting and manipulation of time values
 * for testing purposes. This class supports time values in milliseconds, microseconds, and nanoseconds.
 * It can be set to a specific time and can be auto-incremented at each call, which is useful for
 * simulating time progression in a controlled testing environment.
 */
public class SetTimeProvider extends AtomicLong implements TimeProvider {

    private long autoIncrement = 0;

    /**
     * Constructs a time provider initialized to 0 nanoseconds.
     */
    public SetTimeProvider() {
        this(0L);
    }

    /**
     * Constructs a time provider starting at a specific time in nanoseconds.
     *
     * @param initialNanos Initial time in nanoseconds since the epoch.
     */
    public SetTimeProvider(long initialNanos) {
        super(initialNanos);
    }

    /**
     * Constructs a time provider starting at a time specified in ISO8601 format.
     *
     * @param timestamp The initial timestamp in ISO8601 format.
     */
    public SetTimeProvider(String timestamp) {
        super(initialNanos(timestamp));
    }

    /**
     * Constructs a time provider starting at a given {@link Instant}.
     *
     * @param instant The initial time instant.
     */
    public SetTimeProvider(Instant instant) {
        super(initialNanos(instant));
    }

    /**
     * Constructs a time provider that starts now, using the current nanosecond time.
     *
     * @return A new {@code SetTimeProvider} initialized to the current time.
     */
    public SetTimeProvider now() {
        return new SetTimeProvider(SystemTimeProvider.CLOCK.currentTimeNanos());
    }

    // Helper method to convert timestamp to initial nanoseconds
    static long initialNanos(String timestamp) {
        LocalDateTime dateTime = LocalDateTime.parse(timestamp.replace("/", "-"));
        return initialNanos(dateTime.toInstant(ZoneOffset.UTC));
    }

    // Helper method to convert instant to initial nanoseconds
    static long initialNanos(Instant instant) {
        long initialNanos = instant.getEpochSecond() * 1_000_000_000L;
        if (instant.isSupported(ChronoField.NANO_OF_SECOND))
            initialNanos += instant.getLong(ChronoField.NANO_OF_SECOND);
        return initialNanos;
    }

    /**
     * Configures this time provider to auto-increment the time after each invocation.
     *
     * @param autoIncrement The amount of time to auto-increment after each time retrieval.
     * @param timeUnit      The time unit of the autoIncrement value.
     * @return The current {@code SetTimeProvider} instance for fluent method chaining.
     */
    public SetTimeProvider autoIncrement(long autoIncrement, TimeUnit timeUnit) {
        this.autoIncrement = timeUnit.toNanos(autoIncrement);
        return this;
    }

    /**
     * Explicitly sets the current time in milliseconds.
     *
     * @param millis New time value in milliseconds since the epoch. It must not be less than the previous value.
     * @throws IllegalArgumentException if the time is set to a value earlier than the current time.
     */
    public void currentTimeMillis(long millis) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    /**
     * Retrieves the current time in milliseconds.
     *
     * @return Current time in milliseconds since the epoch.
     */
    @Override
    public long currentTimeMillis() {
        return currentTimeNanos() / 1_000_000;
    }

    /**
     * Explicitly sets the current time in microseconds.
     *
     * @param micros New time value in microseconds since the epoch. It must not be less than the previous value.
     * @throws IllegalArgumentException if the time is set to a value earlier than the current time.
     */
    public void currentTimeMicros(long micros) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    /**
     * Retrieves the current time in microseconds.
     *
     * @return Current time in microseconds since the epoch.
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1_000;
    }

    /**
     * Explicitly sets the current time in nanoseconds.
     *
     * @param nanos New time value in nanoseconds since the epoch. It must not be less than the previous value.
     * @throws IllegalArgumentException if the time is set to a value earlier than the current time.
     */
    public void currentTimeNanos(long nanos) throws IllegalArgumentException {
        if (nanos < get())
            throw new IllegalArgumentException("Cannot go back in time!");
        set(nanos);
    }

    /**
     * Retrieves the current time in nanoseconds.
     *
     * @return Current time in nanoseconds since the epoch.
     */
    @Override
    public long currentTimeNanos() {
        return getAndAdd(autoIncrement);
    }

    /**
     * Converts and retrieves the current time in the specified time unit.
     *
     * @param unit The time unit to return the current time in.
     * @return The current time in the specified time unit.
     */
    public long currentTime(TimeUnit unit) {
        return unit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Advances the current time by the specified duration in milliseconds.
     *
     * @param millis The duration in milliseconds to advance the time.
     * @return The current {@code SetTimeProvider} instance for fluent method chaining.
     */
    public SetTimeProvider advanceMillis(long millis) {
        advanceNanos(TimeUnit.MILLISECONDS.toNanos(millis));
        return this;
    }

    /**
     * Advances the current time by the specified duration in microseconds.
     *
     * @param micros The duration in microseconds to advance the time.
     * @return The current {@code SetTimeProvider} instance for fluent method chaining.
     */
    public SetTimeProvider advanceMicros(long micros) {
        advanceNanos(TimeUnit.MICROSECONDS.toNanos(micros));
        return this;
    }

    /**
     * Advances the current time by the specified duration in nanoseconds.
     *
     * @param nanos The duration in nanoseconds to advance the time.
     * @return The current {@code SetTimeProvider} instance for fluent method chaining.
     */
    public SetTimeProvider advanceNanos(long nanos) {
        addAndGet(nanos);
        return this;
    }

    /**
     * Provides a string representation of the {@code SetTimeProvider} state.
     *
     * @return A string representation, including the auto-increment value and current nanosecond time.
     */
    @Override
    public String toString() {
        return "SetTimeProvider{" +
                "autoIncrement=" + autoIncrement +
                ", nanoTime=" + get() +
                '}';
    }
}
