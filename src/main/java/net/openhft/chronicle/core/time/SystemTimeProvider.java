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

/**
 * Created by peter on 10/03/16.
 */
public enum SystemTimeProvider implements TimeProvider {
    INSTANCE;

    static {
        // warmUp()
        for (int i = 0; i < 1000; i++)
            INSTANCE.currentTimeMicros();
    }

    long delta = 0;

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long currentTimeMicros() {
        long n0 = System.nanoTime();
        long nowMS = System.currentTimeMillis() * 1000;
        long nowUS = n0 / 1000;
        long estimate = nowUS + delta;
        if (estimate < nowMS) {
            delta = nowMS - nowUS;
            return nowMS;
        } else if (estimate > nowMS + 1000) {
            nowMS += 999;
            delta = nowMS - nowUS;
            return nowMS;
        }
        return estimate;
    }
}
