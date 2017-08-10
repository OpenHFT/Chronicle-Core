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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 * Created by Peter Lawrey on 08/12/16.
 */
public class SystemTimeProviderTest {
    @Test
    public void currentTimeMicros() throws Exception {
        @NotNull TimeProvider tp = SystemTimeProvider.INSTANCE;
        long start = System.currentTimeMillis();
        long minDiff = Long.MAX_VALUE;
        long maxDiff = Long.MIN_VALUE;
        // mini-warmup.
        warmup(tp);

        while (System.currentTimeMillis() < start + 500) {
            long now0 = tp.currentTimeMillis();
            long time2 = tp.currentTimeMicros();
            long now1 = tp.currentTimeMillis();
            if (now1 - now0 > 1) {
                System.out.println("jump: " + (now1 - now0));
                continue;
            }

            long now = now1 * 1000;
            long diff = time2 - now;
            if (minDiff > diff) minDiff = diff;
            if (maxDiff < diff) maxDiff = diff;
            Thread.yield();
        }
        System.out.println("minDiff: " + minDiff + ", maxDiff: " + maxDiff);
        assertEquals(-45, minDiff, 50);
        assertEquals(1000, maxDiff, 50);
    }

    private void warmup(TimeProvider tp) {
        for (int i = 0; i < 100000; i++)
            tp.currentTimeMillis();
        Thread.yield();
    }
}