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
 * There are a number of implementations. The simplest and fastest are {@link PosixTimeProvider} and
 * {@link SystemTimeProvider}, with {@link PosixTimeProvider} recommended as being faster, more accurate and
 * more stable, although it does make use of native code.
 */
@FunctionalInterface
public interface TimeProvider {

    long currentTimeMillis();

    default long currentTimeMicros() throws IllegalStateException {
        return currentTimeMillis() * 1000;
    }

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
