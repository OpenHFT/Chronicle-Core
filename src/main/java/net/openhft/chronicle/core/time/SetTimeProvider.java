/*
 * Copyright 2016-2025 chronicle.software
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
    private static final long serialVersionUID = 0L;

    private long autoIncrement = 0;

    /**
     * Constructs a time provider initialised to zero nanoseconds.
     *
     * @implSpec Thread-safe via {@link AtomicLong}.
     */
    public SetTimeProvider() {
        this(0L);
    }

    /**
     * Constructs a time provider starting at a specific time in nanoseconds.
     *
     * @param initialNanos starting time in nanoseconds, non-negative
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Values beyond {@code Long.MAX_VALUE} wrap due to overflow.
     */
    public SetTimeProvider(long initialNanos) {
        super(initialNanos);
    }

    /**
     * Constructs a time provider starting at a time specified in ISO8601 format.
     *
     * @param timestamp ISO8601 timestamp, not {@code null}
     * @implSpec Thread-safe via {@link AtomicLong}.
     */
    public SetTimeProvider(String timestamp) {
        super(initialNanos(timestamp));
    }

    /**
     * Constructs a time provider starting at a given {@link Instant}.
     *
     * @param instant initial time instant, not {@code null}
     * @implSpec Thread-safe via {@link AtomicLong}.
     */
    public SetTimeProvider(Instant instant) {
        super(initialNanos(instant));
    }

    /**
     * Constructs a time provider that starts now.
     *
     * @return a new provider initialised to the current time
     * @implSpec Thread-safe. Each instance is independent.
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
     * Configures this provider to auto-increment after each invocation.
     *
     * @param autoIncrement increment amount, non-negative
     * @param timeUnit unit of {@code autoIncrement}
     * @return this instance for chaining
     * @implSpec Thread-safe for the time value but not for concurrent changes to {@code autoIncrement}.
     * @implNote Overflow is unchecked and wraps.
     */
    public SetTimeProvider autoIncrement(long autoIncrement, TimeUnit timeUnit) {
        this.autoIncrement = timeUnit.toNanos(autoIncrement);
        return this;
    }

    /**
     * Explicitly sets the current time in milliseconds.
     *
     * @param millis new time in milliseconds, not less than the current value
     * @throws IllegalArgumentException if the time would go backwards
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public void currentTimeMillis(long millis) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    /**
     * Retrieves the current time in milliseconds.
     *
     * @return the current time in milliseconds since the epoch
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    @Override
    public long currentTimeMillis() {
        return currentTimeNanos() / 1_000_000;
    }

    /**
     * Explicitly sets the current time in microseconds.
     *
     * @param micros new time in microseconds, not less than the current value
     * @throws IllegalArgumentException if the time would go backwards
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public void currentTimeMicros(long micros) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    /**
     * Retrieves the current time in microseconds.
     *
     * @return the current time in microseconds since the epoch
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1_000;
    }

    /**
     * Explicitly sets the current time in nanoseconds.
     *
     * @param nanos new time in nanoseconds, not less than the current value
     * @throws IllegalArgumentException if the time would go backwards
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public void currentTimeNanos(long nanos) throws IllegalArgumentException {
        if (nanos < get())
            throw new IllegalArgumentException("Cannot go back in time!");
        set(nanos);
    }

    /**
     * Retrieves the current time in nanoseconds.
     *
     * @return the current time in nanoseconds since the epoch
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    @Override
    public long currentTimeNanos() {
        return getAndAdd(autoIncrement);
    }

    /**
     * Converts and retrieves the current time in the specified unit.
     *
     * @param unit target unit, not {@code null}
     * @return the current time in that unit
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public long currentTime(TimeUnit unit) {
        return unit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Advances the current time by the specified duration in milliseconds.
     *
     * @param millis duration to add, may be negative
     * @return this instance for chaining
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public SetTimeProvider advanceMillis(long millis) {
        advanceNanos(TimeUnit.MILLISECONDS.toNanos(millis));
        return this;
    }

    /**
     * Advances the current time by the specified duration in microseconds.
     *
     * @param micros duration to add, may be negative
     * @return this instance for chaining
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public SetTimeProvider advanceMicros(long micros) {
        advanceNanos(TimeUnit.MICROSECONDS.toNanos(micros));
        return this;
    }

    /**
     * Advances the current time by the specified duration in nanoseconds.
     *
     * @param nanos duration to add, may be negative
     * @return this instance for chaining
     * @implSpec Thread-safe via {@link AtomicLong}.
     * @implNote Overflow is unchecked.
     */
    public SetTimeProvider advanceNanos(long nanos) {
        addAndGet(nanos);
        return this;
    }

    /**
     * Provides a string representation of the provider state.
     *
     * @return summary of auto-increment value and current time
     * @implSpec Thread-safe via {@link AtomicLong}.
     */
    @Override
    public String toString() {
        return "SetTimeProvider{" +
                "autoIncrement=" + autoIncrement +
                ", nanoTime=" + get() +
                '}';
    }
}
