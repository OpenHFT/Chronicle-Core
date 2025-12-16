/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.JUnit5TestShouldBePackagePrivate") // JUnit4 annotations require public class
public class HistogramTest extends CoreTestCommon {

    @Test
    public void defaultConstructorInitializesProperly() {
        Histogram histogram = new Histogram();
        assertNotNull(histogram, "defaultConstructorInitializesProperly: L21");
    }

    @Test
    public void constructorWithParametersInitializesProperly() {
        int powersOf2 = 10;
        int fractionBits = 5;
        Histogram histogram = new Histogram(powersOf2, fractionBits);
        assertNotNull(histogram, "constructorWithParametersInitializesProperly: L29");
    }

    @Test
    public void sampleCorrectlyUpdatesHistogram() {
        Histogram histogram = new Histogram();
        int bucket = histogram.sample(1000.0);
        assertTrue(bucket >= 0, "sampleCorrectlyUpdatesHistogram: L36");
        assertTrue(histogram.toMicrosFormat().contains("worst"), "sampleCorrectlyUpdatesHistogram: L37");
    }

    @Test
    public void addCombinesHistogramsCorrectly() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();
        h2.sample(10);
        h1.add(h2);
        assertTrue(h1.toMicrosFormat().contains("worst"), "addCombinesHistogramsCorrectly: L46");
    }

    @Test
    public void testEqualsAndHashCode() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();

        assertEquals(h1, h2, "testEqualsAndHashCode: L54");
        assertEquals(h1.hashCode(), h2.hashCode(), "testEqualsAndHashCode: L55");
    }

    @Test
    public void percentilesForReturnsCorrectValues() {
        long count = 10000;
        double[] percentiles = Histogram.percentilesFor(count);
        assertNotNull(percentiles, "percentilesForReturnsCorrectValues: L62");
        assertTrue(percentiles.length > 0, "percentilesForReturnsCorrectValues: L63");
    }

    @Test
    public void percentilesFor() {
        assertEquals("[0.5, 0.9, 0.99, 0.997, 0.999, 0.9997, 0.9999, 0.99997, 0.99999, 0.999997, 1.0]", Arrays.toString(Histogram.percentilesFor(50_000_000)), "percentilesFor: L68");
    }

    @Test
    public void singleSample() {
        Histogram h = new Histogram();
        h.sampleNanos(100_000);
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 100.0 / 100.0  100.0 / 100.0  100.0 / 100.0  100.0 / 100.0 - 100.0", h.toLongMicrosFormat(), "singleSample: L75");
    }

    @Test
    public void testSampleRange() {
        @NotNull Histogram h = new Histogram(40, 2);
        double base = 1;
        for (int i = 0; i < 40; i++) {
            assertEquals(i * 4, h.sample(base), "testSampleRange: L83");
            assertEquals(i * 4 + 1, h.sample(base * 1.25), "testSampleRange: L84");
            assertEquals(i * 4 + 2, h.sample(base * 1.5), "testSampleRange: L85");
            assertEquals(i * 4 + 3, h.sample(base * 1.75), "testSampleRange: L86");
            base *= 2;
        }
        assertEquals("50/90 99/99.9 99.99 - worst was 980 / 77,309,410  893,353,200 / 1,030,792,150  1,030,792,150 - 1,030,792,150",
                h.toMicrosFormat(),
                "testSampleRange: L89");
    }

    @Test
    public void testSamples() {
        @NotNull Histogram h = new Histogram(10, 5, 1000);

        sampleWithSeed(h, 2141);

        assertEquals("50/90 99/99.9 99.99 - worst was 500 / 890  990 / 990  990 - 990",
                h.toMicrosFormat(),
                "testSamples: L99");
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 500 / 890  970 / 990  990 / 990  990 / 990 - 990",
                h.toLongMicrosFormat(),
                "testSamples: L101");

        for (int i = 1; i <= 100; i++)
            assertEquals(i, percentile(h, i / 100.0), 1, "i: " + i);
        for (int i = 1; i <= 100; i++)
            assertEquals(i, h.percentageLessThan(i * 10_000), 2, "testSamples: L107");
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
        assertEquals(both, h1, "testAdd: L133");
    }

    private int percentile(@NotNull Histogram h, double fraction) {
        return (int) h.percentile(fraction) / 10000;
    }
}
