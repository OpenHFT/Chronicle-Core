/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Timestamps are unique across threads for a single process.
 */
public class UniqueMicroTimeProvider implements TimeProvider {
    public static final UniqueMicroTimeProvider INSTANCE = new UniqueMicroTimeProvider();

    private final AtomicLong lastTime = new AtomicLong();
    private TimeProvider provider = SystemTimeProvider.INSTANCE;

    /**
     * Create new instances for testing purposes as it is stateful
     */
    public UniqueMicroTimeProvider() {
    }

    public UniqueMicroTimeProvider provider(TimeProvider provider) throws IllegalStateException {
        this.provider = provider;
        lastTime.set(provider.currentTimeMicros());
        return this;
    }

    @Override
    public long currentTimeMillis() {
        return provider.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() throws IllegalStateException {
        long time = provider.currentTimeMicros();
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time) {
                assertTime(time, time0);
                time = time0 + 1;
            }
            if (lastTime.compareAndSet(time0, time))
                return time;
            Jvm.nanoPause();
        }
    }

    @Override
    public long currentTimeNanos() throws IllegalStateException {
        long time = provider.currentTimeNanos();
        long timeUS = time / 1000;
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time / 1000) {
                assertTime(timeUS, time0);
                timeUS = time0 + 1;
                time = timeUS * 1000;
            }
            if (lastTime.compareAndSet(time0, timeUS))
                return time;
            Jvm.nanoPause();
        }
    }

    private void assertTime(long realTimeUS, long timeUS) {
        assert (timeUS - realTimeUS) < 1 * 1e6 : "if you call this more than 1 million times a second it will make time go forward";
    }
}
