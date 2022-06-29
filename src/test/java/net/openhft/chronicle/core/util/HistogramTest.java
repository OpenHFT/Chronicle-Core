/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HistogramTest {
    @Test
    public void percentilesFor() {
        assertEquals("[0.5, 0.9, 0.99, 0.997, 0.999, 0.9997, 0.9999, 0.99997, 0.99999, 0.999997, 1.0]", Arrays.toString(Histogram.percentilesFor(50_000_000)));
    }

    @Test
    public void singleSample() {
        Histogram h = new Histogram();
        h.sampleNanos(100_000);
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 100.0 / 100.0  100.0 / 100.0  100.0 / 100.0  100.0 / 100.0 - 100.0", h.toLongMicrosFormat());
    }

    @Test
    public void testSampleRange() {
        @NotNull Histogram h = new Histogram(40, 2);
        double base = 1;
        for (int i = 0; i < 40; i++) {
            assertEquals(i * 4 + 0, h.sample(base));
            assertEquals(i * 4 + 1, h.sample(base * 1.25));
            assertEquals(i * 4 + 2, h.sample(base * 1.5));
            assertEquals(i * 4 + 3, h.sample(base * 1.75));
            base *= 2;
        }
        assertEquals("50/90 99/99.9 99.99 - worst was 980 / 77,309,410  893,353,200 / 1,030,792,150  1,030,792,150 - 1,030,792,150",
                h.toMicrosFormat());
    }


    @Test
    public void testSamples() {
        @NotNull Histogram h = new Histogram(10, 5, 1000);

        long seed = 2141;
        for (int i = 0; i <= 500; i++) {
            h.sampleNanos(seed);
            seed += 128_981;
            if (seed > 1_000_000)
                seed -= 1_000_000;
        }

        assertEquals("50/90 99/99.9 99.99 - worst was 500 / 890  990 / 990  990 - 990",
                h.toMicrosFormat());
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 500 / 890  970 / 990  990 / 990  990 / 990 - 990",
                h.toLongMicrosFormat());

        for (int i = 1; i <= 100; i++)
            assertEquals("i: " + i, i, percentile(h, i / 100.0), 1);
        for (int i = 1; i <= 100; i++)
            assertEquals(i, h.percentageLessThan(i * 10_000), 2);
    }

    private int percentile(@NotNull Histogram h, double fraction) {
        return (int) h.percentile(fraction) / 10000;
    }
}