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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 08/12/16.
 */
public class SystemTimeProviderTest {
    @Test
    public void currentTimeMicros() throws Exception {
        TimeProvider tp = SystemTimeProvider.INSTANCE;
        long start = System.currentTimeMillis();
        long minDiff = Long.MAX_VALUE;
        long maxDiff = Long.MIN_VALUE;
        while (System.currentTimeMillis() < start + 500) {
            long time2 = tp.currentTimeMicros();
            long now = System.currentTimeMillis() * 1000;
            long diff = time2 - now;
            if (minDiff > diff) minDiff = diff;
            if (maxDiff < diff) maxDiff = diff;
            Thread.yield();
        }
        System.out.println("minDiff: " + minDiff + ", maxDiff: " + maxDiff);
        assertEquals(minDiff, 0, 50);
        assertEquals(maxDiff, 1000, 50);
    }
}