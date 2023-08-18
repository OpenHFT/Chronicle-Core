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
 * This is the raw Posix time, if you need it to be unique or a multiple of 32 use a wrapper.
 * <p>
 * PosixTimeProvider calls directly into clock_gettime (native code) to get the nanosecond wall clock on platforms which support it.
 */
public enum PosixTimeProvider implements TimeProvider {
    INSTANCE;

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() throws IllegalStateException {
        return currentTimeNanos() / 1000;
    }

    @Override
    public long currentTimeNanos() {
        return PosixAPI.posix().clock_gettime(ClockId.CLOCK_REALTIME);
    }

}
