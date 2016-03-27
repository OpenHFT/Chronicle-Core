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

package net.openhft.chronicle.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 10/07/15.
 */
public class HistogramTest {

    @Test
    public void testSampleRange() {
        Histogram h = new Histogram(40, 2);
        double base = 1;
        for (int i = 0; i < 40; i++) {
//            System.out.println(i);
            assertEquals(i * 4 + 0, h.sample(base));
            assertEquals(i * 4 + 1, h.sample(base * 1.25));
            assertEquals(i * 4 + 2, h.sample(base * 1.5));
            assertEquals(i * 4 + 3, h.sample(base * 1.75));
            base *= 2;
        }
//        System.out.println(base);
    }

    @Test
    public void testSamples() {
        Histogram h = new Histogram(7, 5);
        for (int i = 1; i <= 100; i++)
            h.sample(i);
        assertEquals(101, h.percentile(1), 0);
        assertEquals(95, h.percentile(0.95), 0);
        assertEquals(91, h.percentile(0.90), 0);
        assertEquals(85, h.percentile(0.85), 0);
        assertEquals(81, h.percentile(0.80), 0);
        assertEquals(71, h.percentile(0.71), 0);
        assertEquals(62, (long) h.percentile(0.62), 0);
        assertEquals(50, (long) h.percentile(0.50), 0);
        assertEquals(40, (long) h.percentile(0.40), 0);
        assertEquals(30, (long) h.percentile(0.30), 0);
    }

/*    @Test
    @Ignore("Long running")
    public void testManySamples() throws IOException {
//        try (FileOutputStream cpu_dma_latency = new FileOutputStream("/dev/cpu_dma_latency")) {
//            cpu_dma_latency.write('0');

//        Affinity.setAffinity(2);
//        System.out.println("Cpu: " + Affinity.getAffinity());
        ITicker instance = Ticker.INSTANCE;
        for (int t = 0; t < 5; t++) {
            Histogram h = new Histogram(32, 4);
            long start = instance.ticks(), prev = start;
            for (int i = 0; i <= 1000_000_000; i++) {
                long now = instance.ticks();
                long time = now - prev;
                h.sample(time);
                prev = now;
            }
            System.out.println(h.toLongMicrosFormat(instance::toMicros));
        }
//        }
    }*/
}