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
 * Provides a nanosecond resolution wall-clock timestamp.
 * <p>
 * There are a number of implementations of this interface, each providing different ways of accessing
 * the current time with nanosecond precision. The simplest and fastest implementations are {@link PosixTimeProvider}
 * and {@link SystemTimeProvider}. Among these, {@link PosixTimeProvider} is recommended for its speed, accuracy,
 * and stability, although it relies on native code.
 * </p>
 */
@FunctionalInterface
public interface TimeProvider {

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     *
     * @return the current time in milliseconds since the Unix epoch.
     */
    long currentTimeMillis();

    /**
     * Returns the current time in microseconds since the Unix epoch.
     * <p>
     * This method provides a default implementation that converts the millisecond time to microseconds
     * by multiplying the result of {@link #currentTimeMillis()} by 1,000.
     * </p>
     *
     * @return the current time in microseconds since the Unix epoch.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    default long currentTimeMicros() throws IllegalStateException {
        return currentTimeMillis() * 1000;
    }

    /**
     * Returns the current time in nanoseconds since the Unix epoch.
     * <p>
     * This method provides a default implementation that converts the microsecond time to nanoseconds
     * by multiplying the result of {@link #currentTimeMicros()} by 1,000.
     * </p>
     *
     * @return the current time in nanoseconds since the Unix epoch.
     * @throws IllegalStateException if the operation cannot be completed.
     */
    default long currentTimeNanos() throws IllegalStateException {
        return currentTimeMicros() * 1000;
    }

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
