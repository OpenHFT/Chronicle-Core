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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;

/**
 * A utility class providing timing functions that are resilient to pauses in the JVM. This class
 * ensures that the generated time-based unique identifiers (`uniqueId()`) are monotonically increasing,
 * even if the JVM experiences pauses.
 * <p>
 * The timing logic depends on the {@link UniqueMicroTimeProvider} and falls back to
 * {@link SystemTimeProvider} if necessary. For accurate time tracking, the `currentTimeMillis()`
 * method (or one of its callers) should be invoked more frequently than every millisecond. This
 * requirement is typically met in systems using the EventLoop implementations in the
 * chronicle-threads library.
 */
public final class Time {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Time() {
    }

    /**
     * Generates a unique identifier based on the current time in microseconds. The identifier
     * is derived from the current time provided by {@link UniqueMicroTimeProvider} or
     * {@link SystemTimeProvider} in case of an {@link IllegalStateException}.
     * <p>
     * The identifier is returned as a base-36 encoded string for compactness.
     *
     * @return A unique identifier string based on the current time in microseconds.
     */
    public static String uniqueId() {
        long l;
        try {
            // Attempt to get the current time in microseconds from the unique time provider
            l = UniqueMicroTimeProvider.INSTANCE.currentTimeMicros();
        } catch (IllegalStateException e) {
            // Fallback to the system time provider in case of an exception
            l = SystemTimeProvider.INSTANCE.currentTimeMicros();
        }
        // Convert the time to a base-36 string for a compact unique identifier
        return Long.toString(l, 36);
    }

}
