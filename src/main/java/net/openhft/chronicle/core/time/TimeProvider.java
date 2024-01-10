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

import java.util.concurrent.TimeUnit;

/**
 * Defines an interface for providing high-resolution wall-clock timestamps.
 * <p>
 * This interface specifies methods to retrieve the current time with varying degrees of precision,
 * namely in milliseconds, microseconds, and nanoseconds. Implementations of this interface are expected
 * to provide time values with the highest accuracy and precision feasible. Key implementations include
 * {@link PosixTimeProvider} and {@link SystemTimeProvider}. The {@code PosixTimeProvider} is often
 * preferred for its enhanced speed, accuracy, and stability, though it relies on native code and thus
 * may have platform-specific dependencies.
 * <p>
 * This interface is crucial in contexts where precise time measurements are vital, such as in performance
 * monitoring, timestamping events, or handling time-sensitive operations.
 *
 * @see PosixTimeProvider
 * @see SystemTimeProvider
 */
@FunctionalInterface
public interface TimeProvider {

    /**
     * Retrieves the current time in milliseconds.
     * <p>
     * This method returns the current time with millisecond precision, measured from the Unix epoch
     * (00:00:00 UTC on 1 January 1970). It is expected to be implemented by all subclasses, providing
     * the baseline precision for time measurements.
     *
     * @return the current time in milliseconds since the Unix epoch
     */
    long currentTimeMillis();

    /**
     * Retrieves the current time in microseconds.
     * <p>
     * This default implementation offers microsecond precision by scaling the millisecond value from
     * {@link #currentTimeMillis()} by a factor of 1000. Implementations may override this for higher
     * accuracy if available.
     *
     * @return The current time in microseconds since the Unix epoch.
     * @throws IllegalStateException if the time value cannot be accurately determined or converted.
     */
    default long currentTimeMicros() throws IllegalStateException {
        return currentTimeMillis() * 1000;
    }

    /**
     * Retrieves the current time in nanoseconds.
     * <p>
     * This default method provides nanosecond precision by further scaling the microsecond value
     * from {@link #currentTimeMicros()} by 1000. Implementations may provide more precise or direct
     * measurements if their underlying system supports it.
     *
     * @return The current time in nanoseconds since the Unix epoch.
     * @throws IllegalStateException if the time value cannot be accurately determined or converted.
     */
    default long currentTimeNanos() throws IllegalStateException {
        return currentTimeMicros() * 1000;
    }

    /**
     * Returns the current time in the specified {@link TimeUnit}.
     * <p>
     * This method is deprecated and will be removed in a future version.
     * Use {@link #currentTimeMillis()}, {@link #currentTimeMicros()}, or {@link #currentTimeNanos()}
     * as per the required precision.
     *
     * @param timeUnit the {@link TimeUnit} to return the current time in
     * @return the current time in the specified time unit
     * @throws IllegalStateException if the time cannot be determined or converted
     * @deprecated to be removed in x.26
     */
    @Deprecated(/* to be removed in x.26 */)
    default long currentTime(TimeUnit timeUnit) throws IllegalStateException {
        switch (timeUnit) {
            case NANOSECONDS:
                return currentTimeNanos();
            case MICROSECONDS:
                return currentTimeMicros();
            case MILLISECONDS:
                return currentTimeMillis();
            default:
                return timeUnit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
        }
    }
}
