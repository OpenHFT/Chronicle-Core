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
 * Provides unique microsecond-resolution timestamps that are unique across threads for a single process.
 * This class ensures that each call to get the current time in microseconds or nanoseconds returns a unique value,
 * even when called from multiple threads. It achieves this by internally tracking the last timestamp and incrementing it
 * if necessary to maintain uniqueness.
 * <p>
 * This class can be particularly useful in scenarios where unique timestamps are required across multiple threads
 * within the same process, such as in logging, profiling, or distributed tracing.
 * </p>
 */
public class UniqueMicroTimeProvider implements TimeProvider {

    /**
     * Singleton instance of {@link UniqueMicroTimeProvider}.
     */
    public static final UniqueMicroTimeProvider INSTANCE = new UniqueMicroTimeProvider();

    private final AtomicLong lastTime = new AtomicLong();
    private TimeProvider provider = SystemTimeProvider.INSTANCE;

    /**
     * Constructs a new {@link UniqueMicroTimeProvider}.
     * Intended for testing purposes to allow creating separate instances.
     */
    public UniqueMicroTimeProvider() {
        // No-op constructor
    }

    /**
     * Sets the underlying {@link TimeProvider} that this {@link UniqueMicroTimeProvider} uses to
     * obtain the base time. Resets the internal last time to the current time provided by the new provider.
     *
     * @param provider the new {@link TimeProvider} to use.
     * @return this {@link UniqueMicroTimeProvider} instance, for chaining.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    public UniqueMicroTimeProvider provider(TimeProvider provider) throws IllegalStateException {
        this.provider = provider;
        lastTime.set(provider.currentTimeMicros());
        return this;
    }

    /**
     * Returns the current time in milliseconds from the underlying {@link TimeProvider}.
     *
     * @return the current time in milliseconds.
     */
    @Override
    public long currentTimeMillis() {
        return provider.currentTimeMillis();
    }

    /**
     * Returns the current time in microseconds, ensuring that each call returns a unique value.
     * This method guarantees that the returned time is monotonically increasing across all threads.
     *
     * @return the current time in microseconds.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    @Override
    public long currentTimeMicros() throws IllegalStateException {
        long time = provider.currentTimeMicros();
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time) {
                assertTime(time, time0);
                time = time0 + 1;
            }
            if (lastTime.compareAndSet(time0, time))
                return time;
            Jvm.nanoPause(); // Pause briefly before retrying
        }
    }

    /**
     * Returns the current time in nanoseconds, ensuring that each call returns a unique value.
     * The method converts the time to microseconds to ensure uniqueness and then converts it back to nanoseconds.
     *
     * @return the current time in nanoseconds.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    @Override
    public long currentTimeNanos() throws IllegalStateException {
        long time = provider.currentTimeNanos();
        long timeUS = time / 1000;
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time / 1000) {
                assertTime(timeUS, time0);
                timeUS = time0 + 1;
                time = timeUS * 1000;
            }
            if (lastTime.compareAndSet(time0, timeUS))
                return time;
            Jvm.nanoPause(); // Pause briefly before retrying
        }
    }

    /**
     * Asserts that the provided time is not significantly ahead of the real time.
     * This method is used to ensure that the time does not advance too quickly if
     * this method is called more than 1 million times per second.
     *
     * @param realTimeUS the actual time in microseconds.
     * @param timeUS     the time being validated in microseconds.
     */
    private void assertTime(long realTimeUS, long timeUS) {
        assert (timeUS - realTimeUS) < 1 * 1e6 : "if you call this more than 1 million times a second it will make time go forward";
    }
}
