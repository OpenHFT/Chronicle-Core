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

import net.openhft.posix.ClockId;
import net.openhft.posix.PosixAPI;

/**
 * Provides raw POSIX time using native system calls. This enum implements {@link TimeProvider}
 * to provide precise time measurements using the POSIX `clock_gettime` function, which retrieves
 * the nanosecond wall clock time on supported platforms.
 * <p>
 * For use cases requiring unique or aligned time values (such as multiples of 32), consider using
 * a wrapper around this time provider to achieve the desired behavior.
 */
public enum PosixTimeProvider implements TimeProvider {
    /**
     * Singleton instance of {@link PosixTimeProvider}.
     */
    INSTANCE;

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     * This method delegates to {@link System#currentTimeMillis()} to get the time.
     *
     * @return the current time in milliseconds since the Unix epoch.
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current time in microseconds since the Unix epoch.
     * This method converts the nanosecond precision time to microseconds by dividing by 1,000.
     *
     * @return the current time in microseconds since the Unix epoch.
     * @throws IllegalStateException if the underlying system call fails to retrieve the time.
     */
    @Override
    public long currentTimeMicros() throws IllegalStateException {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns the current time in nanoseconds since the Unix epoch.
     * This method calls into the native POSIX `clock_gettime` function to obtain a high-resolution
     * wall clock time using the {@link ClockId#CLOCK_REALTIME} identifier.
     *
     * @return the current time in nanoseconds since the Unix epoch.
     */
    @Override
    public long currentTimeNanos() {
        return PosixAPI.posix().clock_gettime(ClockId.CLOCK_REALTIME);
    }

}
