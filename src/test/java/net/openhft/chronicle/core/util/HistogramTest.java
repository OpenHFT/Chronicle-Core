/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HistogramTest extends CoreTestCommon {

    @Test
    public void defaultConstructorInitializesProperly() {
        Histogram histogram = new Histogram();
        assertNotNull(histogram, "Histogram should be created with default constructor");
    }

    @Test
    public void constructorWithParametersInitializesProperly() {
        int powersOf2 = 10;
        int fractionBits = 5;
        Histogram histogram = new Histogram(powersOf2, fractionBits);
        assertNotNull(histogram, "Histogram should be created with custom powersOf2 and fractionBits");
    }

    @Test
    public void sampleCorrectlyUpdatesHistogram() {
        Histogram histogram = new Histogram();
        int bucket = histogram.sample(1000.0);
        assertTrue(bucket >= 0, "bucket index should be non-negative for sampled value");
        assertTrue(histogram.toMicrosFormat().contains("worst"), "formatted output should contain worst-case value after sampling");
    }

    @Test
    public void addCombinesHistogramsCorrectly() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();
        h2.sample(10);
        h1.add(h2);
        assertTrue(h1.toMicrosFormat().contains("worst"), "combined histogram should contain worst-case value after adding");
    }

    @Test
    public void testEqualsAndHashCode() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();

        assertEquals(h1, h2, "empty histograms with same configuration should be equal");
        assertEquals(h1.hashCode(), h2.hashCode(), "equal histograms should have equal hash codes");
    }

    @Test
    public void percentilesForReturnsCorrectValues() {
        long count = 10000;
        double[] percentiles = Histogram.percentilesFor(count);
        assertNotNull(percentiles, "percentilesFor should return non-null array for valid count");
        assertTrue(percentiles.length > 0, "percentiles array should contain at least one element");
    }

    @Test
    public void percentilesFor() {
        assertEquals("[0.5, 0.9, 0.99, 0.997, 0.999, 0.9997, 0.9999, 0.99997, 0.99999, 0.999997, 1.0]", Arrays.toString(Histogram.percentilesFor(50_000_000)), "percentilesFor 50M samples should include standard percentiles up to six nines");
    }

    @Test
    public void singleSample() {
        Histogram h = new Histogram();
        h.sampleNanos(100_000);
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 100.0 / 100.0  100.0 / 100.0  100.0 / 100.0  100.0 / 100.0 - 100.0", h.toLongMicrosFormat(), "single 100us sample should show 100.0 for all percentiles");
    }

    @Test
    public void testSampleRange() {
        @NotNull Histogram h = new Histogram(40, 2);
        double base = 1;
        for (int i = 0; i < 40; i++) {
            assertEquals(i * 4, h.sample(base), "bucket for base value at power " + i + " should be i*4");
            assertEquals(i * 4 + 1, h.sample(base * 1.25), "bucket for 1.25x base at power " + i + " should be i*4+1");
            assertEquals(i * 4 + 2, h.sample(base * 1.5), "bucket for 1.5x base at power " + i + " should be i*4+2");
            assertEquals(i * 4 + 3, h.sample(base * 1.75), "bucket for 1.75x base at power " + i + " should be i*4+3");
            base *= 2;
        }
        assertEquals("50/90 99/99.9 99.99 - worst was 980 / 77,309,410  893,353,200 / 1,030,792,150  1,030,792,150 - 1,030,792,150",
                h.toMicrosFormat(),
                "40-power histogram should produce expected percentile distribution format");
    }

    @Test
    public void testSamples() {
        @NotNull Histogram h = new Histogram(10, 5, 1000);

        sampleWithSeed(h, 2141);

        assertEquals("50/90 99/99.9 99.99 - worst was 500 / 890  990 / 990  990 - 990",
                h.toMicrosFormat(),
                "seeded samples should produce expected short format percentiles");
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 500 / 890  970 / 990  990 / 990  990 / 990 - 990",
                h.toLongMicrosFormat(),
                "seeded samples should produce expected long format percentiles");

        for (int i = 1; i <= 100; i++)
            assertEquals(i, percentile(h, i / 100.0), 1, "percentile at " + i + "% should match expected value");
        for (int i = 1; i <= 100; i++)
            assertEquals(i, h.percentageLessThan(i * 10_000), 2, "percentage less than " + (i * 10) + "us should be approximately " + i);
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
        assertEquals(both, h1, "h1 plus h2 should equal histogram sampled with both seeds");
    }

    private int percentile(@NotNull Histogram h, double fraction) {
        return (int) h.percentile(fraction) / 10000;
    }
}
