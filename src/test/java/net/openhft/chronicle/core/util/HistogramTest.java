/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class HistogramTest extends CoreTestCommon {

    @Test
    public void defaultConstructorInitializesProperly() {
        Histogram histogram = new Histogram();
        assertNotNull(histogram);
    }

    @Test
    public void constructorWithParametersInitializesProperly() {
        int powersOf2 = 10;
        int fractionBits = 5;
        Histogram histogram = new Histogram(powersOf2, fractionBits);
        assertNotNull(histogram);
    }

    @Test
    public void sampleCorrectlyUpdatesHistogram() {
        Histogram histogram = new Histogram();
        int bucket = histogram.sample(1000.0);
    }

    @Test
    public void addCombinesHistogramsCorrectly() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();
        h1.add(h2);
    }

    @Test
    public void testEqualsAndHashCode() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
    }

    @Test
    public void percentilesForReturnsCorrectValues() {
        long count = 10000;
        double[] percentiles = Histogram.percentilesFor(count);
    }

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

        sampleWithSeed(h, 2141);

        assertEquals("50/90 99/99.9 99.99 - worst was 500 / 890  990 / 990  990 - 990",
                h.toMicrosFormat());
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 500 / 890  970 / 990  990 / 990  990 / 990 - 990",
                h.toLongMicrosFormat());

        for (int i = 1; i <= 100; i++)
            assertEquals("i: " + i, i, percentile(h, i / 100.0), 1);
        for (int i = 1; i <= 100; i++)
            assertEquals(i, h.percentageLessThan(i * 10_000), 2);
    }

    private void sampleWithSeed(@NotNull Histogram h, long seed) {
        for (int i = 0; i <= 500; i++) {
            h.sampleNanos(seed);
            seed += 128_981;
            if (seed > 1_000_000)
                seed -= 1_000_000;
        }
    }

    @Test
    public void testAdd() {
        int seed1 = 2141;
        int seed2 = 33;
        Histogram h1 = Histogram.timeMicros();
        Histogram h2 = Histogram.timeMicros();
        sampleWithSeed(h1, seed1);
        sampleWithSeed(h2, seed2);

        Histogram both = Histogram.timeMicros();
        sampleWithSeed(both, seed1);
        sampleWithSeed(both, seed2);

        h1.add(h2);
        assertEquals(both, h1);
    }

    private int percentile(@NotNull Histogram h, double fraction) {
        return (int) h.percentile(fraction) / 10000;
    }
}
