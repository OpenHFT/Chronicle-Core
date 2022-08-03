/*
 * Copyright 2016-2022 chronicle.software
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

public final class LongTime {
    private LongTime() {
    }

    public static final long MAX_NANOS = Long.MAX_VALUE; // 2262-04-11T23:47:16.854775807
    public static final long MAX_MICROS = MAX_NANOS / 1000; // 2262-04-11T23:47:16.854775
    public static final long MAX_MILLIS = MAX_MICROS / 1000; // 2262-04-11T23:47:16.854
    public static final long MAX_SECS = MAX_MILLIS / 1000; // 2262-04-11T23:47:16
    public static final long EPOCH_SECS = 0; // 1970-01-01T00:00:00
    public static final long EPOCH_MILLIS = MAX_SECS + 1; // 1970-04-17T18:02:52.037
    public static final long EPOCH_MICROS = EPOCH_MILLIS * 1000; // 1970-04-17T18:02:52.037
    public static final long EPOCH_NANOS = EPOCH_MICROS * 1000; // 1970-04-17T18:02:52.037

    public static boolean isSecs(long time) {
        return EPOCH_SECS <= time && time <= MAX_SECS;
    }

    public static boolean isMillis(long time) {
        return EPOCH_MILLIS <= time && time <= MAX_MILLIS;
    }

    public static boolean isMicros(long time) {
        return EPOCH_MICROS <= time && time <= MAX_MICROS;
    }

    public static boolean isNanos(long time) {
        return EPOCH_NANOS <= time /*&& time <= MAX_NANOS*/;
    }

    public static long toSecs(long time) {
        if (time < EPOCH_MILLIS) // || time < EPOCH_SECS
            return time;
        if (time < EPOCH_MICROS)
            return time / 1000;
        if (time < EPOCH_NANOS)
            return time / 1000_000;
        return time / 1000_000_000;
    }

    public static long toMillis(long time) {
        if (time < EPOCH_SECS)
            return time;
        if (time < EPOCH_MILLIS)
            return time * 1000;
        if (time < EPOCH_MICROS)
            return time;
        if (time < EPOCH_NANOS)
            return time / 1000;
        return time / 1000_000;
    }

    public static long toMicros(long time) {
        if (time < EPOCH_SECS)
            return time;
        if (time < EPOCH_MILLIS)
            return time * 1000_000;
        if (time < EPOCH_MICROS)
            return time * 1000;
        if (time < EPOCH_NANOS)
            return time;
        return time / 1_000;
    }

    public static long toNanos(long time) {
        if (time >= EPOCH_NANOS)
            return time;
        if (time >= EPOCH_MICROS)
            return time * 1000;
        if (time >= EPOCH_MILLIS)
            return time * 1000_000;
        if (time >= EPOCH_SECS)
            return time * 1000_000_000;
        return time;
    }
}
