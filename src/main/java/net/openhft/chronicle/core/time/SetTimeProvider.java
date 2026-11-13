/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
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
     */
    public SetTimeProvider() {
        this(0L);
    }

    /**
     * Constructs a time provider starting at a specific time in nanoseconds.
     *
     * @param initialNanos starting time in nanoseconds since the epoch (may be negative)
     */
    public SetTimeProvider(long initialNanos) {
        super(initialNanos);
    }

    /**
     * Constructs a time provider starting at a time specified in ISO8601 format.
     * <p>
     * Text with {@code Z} or a numeric offset, such as {@code 2026-01-01T00:00:00Z} or
     * {@code 2026-01-01T01:00:00+01:00}, is an instant. Text without an offset, such as
     * {@code 2026-01-01T00:00:00}, is read as UTC. The date may use {@code /} in place of {@code -}.
     * A bracketed zone id after the offset, as {@link java.time.ZonedDateTime#toString()} prints,
     * is accepted; the offset gives the instant.
     *
     * @param timestamp ISO8601 timestamp, not {@code null}
     * @throws java.time.format.DateTimeParseException if the text is not such a timestamp
     * @throws ArithmeticException if the instant is outside the signed-long nanosecond range
     */
    public SetTimeProvider(String timestamp) {
        super(initialNanos(timestamp));
    }

    /**
     * Constructs a time provider starting at a given {@link Instant}.
     *
     * @param instant initial time instant, not {@code null}
     * @throws ArithmeticException if the instant is outside the signed-long nanosecond range
     */
    public SetTimeProvider(Instant instant) {
        super(initialNanos(instant));
    }

    /**
     * Constructs a time provider that starts now.
     *
     * @return a new provider initialised to the current time
     */
    public SetTimeProvider now() {
        return new SetTimeProvider(SystemTimeProvider.CLOCK.currentTimeNanos());
    }

    // Helper method to convert timestamp to initial nanoseconds; text without an offset is UTC
    static long initialNanos(String timestamp) {
        // '/' may stand for '-' in the date, but not in a zone id such as [Europe/London]
        final int zone = timestamp.indexOf('[');
        final String text = zone < 0 ? timestamp.replace("/", "-")
                : timestamp.substring(0, zone).replace("/", "-") + timestamp.substring(zone);
        TemporalAccessor parsed = DateTimeFormatter.ISO_DATE_TIME
                .parseBest(text, OffsetDateTime::from, LocalDateTime::from);
        return initialNanos(parsed instanceof OffsetDateTime
                ? ((OffsetDateTime) parsed).toInstant()
                : ((LocalDateTime) parsed).toInstant(ZoneOffset.UTC));
    }

    // Helper method to convert instant to initial nanoseconds
    static long initialNanos(Instant instant) {
        final long seconds = instant.getEpochSecond();
        final int nanos = instant.getNano();
        // At the negative boundary, seconds * 1e9 can overflow even when the sum fits.
        if (seconds < 0 && nanos > 0)
            return Math.addExact(Math.multiplyExact(seconds + 1, 1_000_000_000L), nanos - 1_000_000_000L);
        return Math.addExact(Math.multiplyExact(seconds, 1_000_000_000L), nanos);
    }

    /**
     * Enables time auto-increment after each call to {@link #currentTimeNanos()}.
     *
     * <p>Example usage in a JUnit test:</p>
     * <pre>{@code
     * SetTimeProvider tp = new SetTimeProvider("2023-01-01T00:00:00")
     *         .autoIncrement(1, TimeUnit.MILLISECONDS);
     * }
     * </pre>
     * <p>Every read of the current time now advances by one millisecond. When
     * running tests concurrently, coordinate access to avoid unexpected jumps.</p>
     *
     * @param autoIncrement increment amount, non-negative
     * @param timeUnit unit of {@code autoIncrement}
     * @return this instance for chaining
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
     */
    public void currentTimeMillis(long millis) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    /**
     * Retrieves the current time in milliseconds.
     *
     * @return the current time in milliseconds since the epoch
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
     */
    public void currentTimeMicros(long micros) throws IllegalArgumentException {
        currentTimeNanos(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    /**
     * Retrieves the current time in microseconds.
     *
     * @return the current time in microseconds since the epoch
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
     */
    public long currentTime(TimeUnit unit) {
        return unit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Advances the current time by the specified duration in milliseconds.
     *
     * @param millis duration to add, may be negative
     * @return this instance for chaining
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
     */
    public SetTimeProvider advanceNanos(long nanos) {
        addAndGet(nanos);
        return this;
    }

    /**
     * Provides a string representation of the provider state.
     *
     * @return summary of auto-increment value and current time
     */
    @Override
    public String toString() {
        return "SetTimeProvider{" +
                "autoIncrement=" + autoIncrement +
                ", nanoTime=" + get() +
                '}';
    }
}
