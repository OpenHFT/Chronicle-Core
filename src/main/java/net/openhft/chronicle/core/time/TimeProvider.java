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

/**
 * Supplies wall clock timestamps in milliseconds, microseconds and nanoseconds.
 * <p>
 * Implementations typically delegate to the operating system clock, so the value returned can
 * move backwards if the wall clock is corrected. Key implementations include
 * {@link PosixTimeProvider} and {@link SystemTimeProvider}. The {@code PosixTimeProvider} is often
 * preferred for its enhanced speed, accuracy and stability, though it relies on native code and thus
 * may have platform specific dependencies.
 * <p>
 * This interface is crucial in contexts where precise time measurements are vital, such as in performance
 * monitoring, timestamping events, or handling time-sensitive operations.
 *
 * @apiNote Use {@link UniqueMicroTimeProvider} if monotonic timestamps are required.
 * @see PosixTimeProvider
 * @see SystemTimeProvider
 */
@FunctionalInterface
public interface TimeProvider {

    /**
     * Retrieves the current time in milliseconds.
     * <p>
     * This method returns the current time with millisecond precision, measured from the Unix epoch
     * (00:00:00 UTC on 1 January 1970). Implementations must guarantee thread-safe access.
     *
     * @return the current time in milliseconds since the Unix epoch
     * @implSpec Implementations must be thread-safe. Values may overflow after year 2262.
     */
    long currentTimeMillis();

    /**
     * Retrieves the current time in microseconds.
     * <p>
     * This default implementation offers microsecond precision by scaling the millisecond value from
     * {@link #currentTimeMillis()} by a factor of 1000. Implementations may override this for higher
     * accuracy if available.
     *
     * @return the current time in microseconds since the Unix epoch
     * @throws IllegalStateException if the time value cannot be accurately determined or converted
     * @implSpec Thread-safe if {@link #currentTimeMillis()} is thread-safe. Values may overflow after year 2262.
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
     * @return the current time in nanoseconds since the Unix epoch
     * @throws IllegalStateException if the time value cannot be accurately determined or converted
     * @implSpec Thread-safe if {@link #currentTimeMicros()} is thread-safe. Values may overflow after year 2262.
     */
    default long currentTimeNanos() throws IllegalStateException {
        return currentTimeMicros() * 1000;
    }
}
