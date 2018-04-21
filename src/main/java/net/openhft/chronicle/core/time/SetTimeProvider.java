/*
 * Copyright 2016 higherfrequencytrading.com
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * TimeProvider whose value can be explicitly set and advanced for testing purposes.  Supports time values in milliseconds, microseconds and nanoseconds.
 * <p>
 * Created by Peter Lawrey on 10/03/16.
 **/
public class SetTimeProvider implements TimeProvider {

    private final AtomicLong nanoTime;

    public SetTimeProvider() {
        this(0L);
    }

    public SetTimeProvider(long initialNanos) {
        nanoTime = new AtomicLong(initialNanos);
    }

    /**
     * Set the current time in milliseconds.
     *
     * @param millis New time value in milliseconds since the epoch. May not be less than the previous value.
     */
    public void currentTimeMillis(long millis) {
        currentTimeNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    @Override
    public long currentTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(currentTimeNanos());
    }

    /**
     * Set the current time in microseconds.
     *
     * @param micros New time value in microseconds since the epoch. May not be less than the previous value.
     */
    public void currentTimeMicros(long micros) {
        currentTimeNanos(TimeUnit.MICROSECONDS.toNanos(micros));
    }

    @Override
    public long currentTimeMicros() {
        return TimeUnit.NANOSECONDS.toMicros(currentTimeNanos());
    }

    /**
     * Set the current time in nanoseconds.
     *
     * @param nanos New time value in nanoseconds since the epoch. May not be less than the previous value.
     */
    public void currentTimeNanos(long nanos) {
        if (nanos < nanoTime.get()) throw new IllegalArgumentException("Cannot go back in time!");
        nanoTime.set(nanos);
    }

    @Override
    public long currentTimeNanos() {
        return nanoTime.get();
    }

    @Override
    public long currentTime(TimeUnit unit) {
        return unit.convert(currentTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Advances time in milliseconds.
     *
     * @param millis duration.
     */
    public SetTimeProvider advanceMillis(long millis) {
        advanceNanos(TimeUnit.MILLISECONDS.toNanos(millis));
        return this;
    }

    /**
     * Advances time in microseconds.
     *
     * @param micros duration.
     */
    public SetTimeProvider advanceMicros(long micros) {
        advanceNanos(TimeUnit.MICROSECONDS.toNanos(micros));
        return this;
    }

    /**
     * Advances time in nanoseconds.
     *
     * @param nanos duration.
     */
    public SetTimeProvider advanceNanos(long nanos) {
        nanoTime.addAndGet(nanos);
        return this;
    }

}
