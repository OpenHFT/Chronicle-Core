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
 * A {@link TimeProvider} implementation that allows for explicit setting and advancing of time values
 * for testing purposes. This class supports time values in milliseconds, microseconds, and nanoseconds,
 * making it versatile for different time-related testing scenarios.
 * <p>
 * This class is thread-safe as it extends {@link AtomicLong} for its internal time representation.
 * It is particularly useful in unit tests where you want to control the flow of time precisely.
 * </p>
 * <p>
 * Created by Peter Lawrey on 10/03/16.
 * </p>
 */
public class SetTimeProvider extends AtomicLong implements TimeProvider {

    private long autoIncrement = 0;

    /**
     * Creates a {@link SetTimeProvider} which starts with a time value of 0 nanoseconds.
     */
    public SetTimeProvider() {
        this(0L);
    }

    /**
     * Creates a {@link SetTimeProvider} starting at a specific time in nanoseconds.
     *
     * @param initialNanos initial time in nanoseconds since the epoch.
     */
    public SetTimeProvider(long initialNanos) {
        super(initialNanos);
    }

    /**
     * Creates a {@link SetTimeProvider} which starts at a time specified by an ISO8601 format string.
     *
     * @param timestamp the initial timestamp in ISO8601 format.
     */
    public SetTimeProvider(String timestamp) {
        super(initialNanos(timestamp));
    }

    /**
     * Creates a {@link SetTimeProvider} which starts at a given {@link Instant}.
     *
     * @param instant the initial time as an {@link Instant}.
     */
    public SetTimeProvider(Instant instant) {
        super(initialNanos(instant));
    }

    /**
     * Factory method to create a {@link SetTimeProvider} that starts at the current system time.
     *
     * @return a new {@link SetTimeProvider} initialized to the current system time in nanoseconds.
     */
    public SetTimeProvider now() {
        return new SetTimeProvider(SystemTimeProvider.CLOCK.currentTimeNanos());
    }

    /**
     * Parses an ISO8601 formatted timestamp to its equivalent nanoseconds since epoch.
     *
     * @param timestamp the ISO8601 formatted timestamp string.
     * @return the corresponding time in nanoseconds since epoch.
     */
    static long initialNanos(String timestamp) {
        LocalDateTime dateTime = LocalDateTime.parse(timestamp.replace("/", "-"));
        return initialNanos(dateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * Converts an {@link Instant} to nanoseconds since epoch.
     *
     * @param instant the {@link Instant} to convert.
     * @return the corresponding time in nanoseconds since epoch.
     */
    static long initialNanos(Instant instant) {
        long initialNanos = instant.getEpochSecond() * 1_000_000_000L;
        if (instant.isSupported(ChronoField.NANO_OF_SECOND))
            initialNanos += instant.getLong(ChronoField.NANO_OF_SECOND);
        return initialNanos;
    }

    /**
     * Configures this {@link SetTimeProvider} to automatically increment time by a specified amount.
     *
     * @param autoIncrement the increment value.
     * @param timeUnit      the unit of time for the increment.
     * @return this {@link SetTimeProvider} instance.
     */
    public SetTimeProvider autoIncrement(long autoIncrement, TimeUnit timeUnit) {
        this.autoIncrement = timeUnit.toNanos(autoIncrement);
        return this;
    }

    /**
     * Sets the current time in milliseconds.
     *
     * @param millis new time value in milliseconds since the epoch. Cannot be less than the current value.
     * @throws IllegalArgumentException if time is set backwards.
     */
    public void currentTimeMillis(long millis) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    @Override
    public long currentTimeMillis() {
        return currentTimeNanos() / 1_000_000;
    }

    /**
     * Sets the current time in microseconds.
     *
     * @param micros new time value in microseconds since the epoch. Cannot be less than the current value.
     * @throws IllegalArgumentException if time is set backwards.
     */
    public void currentTimeMicros(long micros) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    @Override
    public long currentTimeMicros() {
        return currentTimeNanos() / 1_000;
    }

    /**
     * Sets the current time in nanoseconds.
     *
     * @param nanos new time value in nanoseconds since the epoch. Cannot be less than the current value.
     * @throws IllegalArgumentException if time is set backwards.
     */
    public void currentTimeNanos(long nanos) throws IllegalArgumentException {
        if (nanos < get())
            throw new IllegalArgumentException("Cannot go back in time!");
        set(nanos);
    }

    @Override
    public long currentTimeNanos() {
        return getAndAdd(autoIncrement);
    }

    @Override
    public long currentTime(TimeUnit unit) {
        return unit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Advances the current time by a specified duration in milliseconds.
     *
     * @param millis the duration to advance the time by.
     * @return this {@link SetTimeProvider} instance.
     */
    public SetTimeProvider advanceMillis(long millis) {
        advanceNanos(TimeUnit.MILLISECONDS.toNanos(millis));
        return this;
    }

    /**
     * Advances the current time by a specified duration in microseconds.
     *
     * @param micros the duration to advance the time by.
     * @return this {@link SetTimeProvider} instance.
     */
    public SetTimeProvider advanceMicros(long micros) {
        advanceNanos(TimeUnit.MICROSECONDS.toNanos(micros));
        return this;
    }

    /**
     * Advances the current time by a specified duration in nanoseconds.
     *
     * @param nanos the duration to advance the time by.
     * @return this {@link SetTimeProvider} instance.
     */
    public SetTimeProvider advanceNanos(long nanos) {
        addAndGet(nanos);
        return this;
    }

    @Override
    public String toString() {
        return "SetTimeProvider{" +
                "autoIncrement=" + autoIncrement +
                ", nanoTime=" + get() +
                '}';
    }
}
