/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.util;

import net.openhft.affinity.Affinity;
import net.openhft.clock.Clock;
import net.openhft.clock.IClock;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

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

    @Test
    @Ignore("Long running")
    public void testManySamples() throws IOException {
        try (FileOutputStream cpu_dma_latency = new FileOutputStream("/dev/cpu_dma_latency")) {
            cpu_dma_latency.write('0');

            Affinity.setAffinity(2);
            System.out.println("Cpu: " + Affinity.getAffinity());
            IClock instance = Clock.INSTANCE;
            for (int t = 0; t < 100; t++) {
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
        }
    }
}