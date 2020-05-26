/*
 * Copyright 2016-2020 Chronicle Software
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

    public UniqueMicroTimeProvider provider(TimeProvider provider) {
        this.provider = provider;
        lastTime.set(provider.currentTimeMicros());
        return this;
    }

    @Override
    public long currentTimeMillis() {
        return provider.currentTimeMillis();
    }

    @Override
    public long currentTimeMicros() {
        long time = provider.currentTimeMicros();
        while (true) {
            long time0 = lastTime.get();
            if (time0 >= time)
                time = time0 + 1;
            if (lastTime.compareAndSet(time0, time))
                return time;
        }
    }
}
