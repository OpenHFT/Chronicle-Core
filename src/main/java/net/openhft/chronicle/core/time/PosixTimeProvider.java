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
 * Provides time based on the Posix standard, particularly utilizing native code
 * to access high-resolution time.
 * <p>
 * This provider interfaces directly with the native method {@code clock_gettime}
 * to fetch nanosecond precision wall-clock time on platforms that support it. It is
 * important to note that this time provider relies on native system calls, thus is
 * platform-dependent. It is typically more accurate and has finer resolution than
 * standard Java time providers.
 */
public enum PosixTimeProvider implements TimeProvider {
    INSTANCE;

    /**
     * Returns the current time in milliseconds using the standard Java system clock.
     *
     * @return the current time in milliseconds since the Unix epoch
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the current time in microseconds.
     * <p>
     * This method provides microsecond precision by dividing the nanosecond value by 1000.
         *
     * @return the current time in microseconds since the Unix epoch
     * @throws IllegalStateException if the time cannot be determined or converted
     */
    @Override
    public long currentTimeMicros() throws IllegalStateException {
        return currentTimeNanos() / 1000;
    }

    /**
     * Returns the current time in nanoseconds.
     * <p>
     * This method directly calls the native {@code clock_gettime} function, providing
     * highly precise and accurate nanosecond resolution time. It uses {@link ClockId#CLOCK_REALTIME}
     * for fetching the current time.
         *
     * @return the current time in nanoseconds since the Unix epoch
     * @throws IllegalStateException if the native call fails
     */
    @Override
    public long currentTimeNanos() {
        return PosixAPI.posix().clock_gettime(ClockId.CLOCK_REALTIME);
    }
}

