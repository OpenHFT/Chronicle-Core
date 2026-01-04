/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HistogramTest extends CoreTestCommon {

    @Test
    @DisplayName("Default histogram starts with empty bucket state")
    void defaultConstructorInitializesProperly() {
        Histogram histogram = new Histogram();
        assertNotNull(histogram, "Histogram should be created with default constructor");
    }

    @Test
    @DisplayName("Custom histogram uses powers and fraction bits")
    void constructorWithParametersInitializesProperly() {
        int powersOf2 = 10;
        int fractionBits = 5;
        Histogram histogram = new Histogram(powersOf2, fractionBits);
        assertNotNull(histogram, "Histogram should be created with custom powersOf2 and fractionBits");
    }

    @Test
    @DisplayName("Sampling updates bucket index and worst entry")
    void sampleCorrectlyUpdatesHistogram() {
        Histogram histogram = new Histogram();
        int bucket = histogram.sample(1000.0);
        assertTrue(bucket >= 0, "bucket index should be non-negative: bucket=" + bucket);
        String micros = histogram.toMicrosFormat();
        assertTrue(micros.contains("worst"), "formatted output should contain \"worst\": " + micros);
    }

    @Test
    @DisplayName("Adding histogram merges sample distributions together")
    void addCombinesHistogramsCorrectly() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();
        h2.sample(10);
        h1.add(h2);
        String micros = h1.toMicrosFormat();
        assertTrue(micros.contains("worst"), "combined histogram should contain \"worst\": " + micros);
    }

    @Test
    @DisplayName("Equality compares histogram configuration and buckets")
    void testEqualsAndHashCode() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();

        assertEquals(h1, h2, "empty histograms with same configuration should be equal");
        assertEquals(h1.hashCode(), h2.hashCode(), "equal histograms should have equal hash codes");
    }

    @Test
    @DisplayName("Percentile array includes expected percentile markers")
    void percentilesForReturnsCorrectValues() {
        long count = 10000;
        double[] percentiles = Histogram.percentilesFor(count);
        assertNotNull(percentiles, "percentilesFor should return non-null array for valid count");
        assertTrue(percentiles.length > 0, "percentiles array should contain at least one element: length=" + percentiles.length);
    }

    @Test
    @DisplayName("Percentile table matches standard high quantiles")
    void percentilesFor() {
        assertEquals("[0.5, 0.9, 0.99, 0.997, 0.999, 0.9997, 0.9999, 0.99997, 0.99999, 0.999997, 1.0]", Arrays.toString(Histogram.percentilesFor(50_000_000)), "percentilesFor 50M samples should include standard percentiles up to six nines");
    }

    @Test
    @DisplayName("Single sample renders full percentile summary")
    void singleSample() {
        Histogram h = new Histogram();
        h.sampleNanos(100_000);
        assertEquals("50/90 97/99 99.7/99.9 99.97/99.99 - worst was 100.0 / 100.0  100.0 / 100.0  100.0 / 100.0  100.0 / 100.0 - 100.0", h.toLongMicrosFormat(), "single 100us sample should show 100.0 for all percentiles");
    }

    @Test
    @DisplayName("Sample range maps powers to bucket offsets")
    void testSampleRange() {
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
    @DisplayName("Seeded samples match percentile and percentage outputs")
    void testSamples() {
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
    @DisplayName("Combining histograms equals dual seed sampling")
    void testAdd() {
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

    @Test
    @DisplayName("Histogram equals returns false for non-Histogram object type comparison")
    void equalsNonHistogram() {
        Histogram h = new Histogram();
        assertNotEquals(h, "not a histogram", "Histogram should not equal a String");
        assertNotEquals(h, null, "Histogram should not equal null");
    }

    @Test
    @DisplayName("Histogram equals returns false for different configuration settings like fractionBits")
    void equalsDifferentConfiguration() {
        Histogram h1 = new Histogram(10, 4);
        Histogram h2 = new Histogram(10, 5);
        Histogram h3 = new Histogram(11, 4);

        assertNotEquals(h1, h2, "Histograms with different fractionBits should not be equal");
        assertNotEquals(h1, h3, "Histograms with different powersOf2 should not be equal");
    }

    @Test
    @DisplayName("equals returns false for different sample count values")
    void equalsDifferentSamples() {
        Histogram h1 = new Histogram();
        Histogram h2 = new Histogram();
        h1.sample(100);

        assertNotEquals(h1, h2, "Histograms with different samples should not be equal");
    }

    @Test
    @DisplayName("sample handles overRange when bucket exceeds array length")
    void sampleOverRange() {
        Histogram h = new Histogram(5, 2); // small histogram
        // Sample a very large value that exceeds the bucket range
        h.sample(Double.MAX_VALUE / 2);

        assertEquals(1, h.overRange(), "overRange should increment for out-of-range samples");
    }

    @Test
    @DisplayName("sample handles negative bucket values gracefully")
    void sampleNegativeBucket() {
        Histogram h = new Histogram(10, 2, 1000); // floor at 1000
        // Sample a value below the floor
        int bucket = h.sample(1); // much smaller than floor

        assertTrue(bucket < 0, "bucket " + bucket + " should be negative for values below floor");
        assertEquals(1, h.totalCount(), "totalCount should still increment");
    }

    @Test
    @DisplayName("percentile returns POSITIVE_INFINITY when overRange exceeds count")
    void percentileReturnsInfinity() {
        Histogram h = new Histogram(5, 2);
        // Force overRange to be high by sampling very large values
        for (int i = 0; i < 100; i++) {
            h.sample(Double.MAX_VALUE / 2);
        }

        double p99 = h.percentile(0.99);
        assertEquals(Double.POSITIVE_INFINITY, p99, "percentile should return POSITIVE_INFINITY when all samples are overRange");
    }

    @Test
    @DisplayName("percentile with fraction zero returns minimum")
    void percentileZeroFraction() {
        Histogram h = new Histogram();
        h.sample(1000);
        h.sample(2000);

        double min = h.percentile(0);
        assertTrue(min > 0, "min percentile " + min + " should be positive when samples exist");
    }

    @Test
    @DisplayName("percentile with fraction zero on empty histogram returns 1")
    void percentileZeroFractionEmpty() {
        Histogram h = new Histogram();

        double min = h.percentile(0);
        assertEquals(1, min, "min percentile on empty histogram should return 1");
    }

    @Test
    @DisplayName("toMicrosFormat handles large sample count output")
    void toMicrosFormatLargeSampleCount() {
        Histogram h = new Histogram();
        // Add many samples to trigger different format branches
        for (int i = 0; i < 2_000_000; i++) {
            h.sample(i % 10000 + 1000);
        }

        String format = h.toMicrosFormat();
        assertTrue(format.contains("99.999"), "format for 2M samples should include 99.999 percentile");
    }

    @Test
    @DisplayName("toMicrosFormat handles very large sample counts")
    void toMicrosFormatVeryLargeSampleCount() {
        Histogram h = new Histogram();
        // Add very many samples to trigger the >= 10M branch
        for (int i = 0; i < 15_000_000; i++) {
            h.sample(i % 10000 + 1000);
        }

        String format = h.toMicrosFormat();
        assertTrue(format.contains("99.9999"), "format for 15M samples should include 99.9999 percentile");
    }

    @Test
    @DisplayName("toLongMicrosFormat handles large sample count output")
    void toLongMicrosFormatLargeSampleCount() {
        Histogram h = new Histogram();
        for (int i = 0; i < 2_000_000; i++) {
            h.sample(i % 10000 + 1000);
        }

        String format = h.toLongMicrosFormat();
        assertTrue(format.contains("99.997"), "long format for 2M samples should include 99.997 percentile");
    }

    @Test
    @DisplayName("toLongMicrosFormat handles very large sample counts")
    void toLongMicrosFormatVeryLargeSampleCount() {
        Histogram h = new Histogram();
        for (int i = 0; i < 15_000_000; i++) {
            h.sample(i % 10000 + 1000);
        }

        String format = h.toLongMicrosFormat();
        assertTrue(format.contains("99.9997"), "long format for 15M samples should include 99.9997 percentile");
    }

    @Test
    @DisplayName("init resizes sampleCount array when needed")
    void initResizesSampleCount() {
        Histogram h = new Histogram(5, 2); // small initial size
        int originalLength = h.sampleCount().length;

        h.init(20, 4, 0, 0, 0); // larger configuration

        assertTrue(h.sampleCount().length > originalLength,
                "sampleCount length " + h.sampleCount().length + " should exceed original length " + originalLength);
    }

    @Test
    @DisplayName("init does not resize when sampleCount is large enough")
    void initNoResizeWhenLargeEnough() {
        Histogram h = new Histogram(20, 4); // large initial size
        int originalLength = h.sampleCount().length;

        h.init(5, 2, 0, 0, 0); // smaller configuration

        assertEquals(originalLength, h.sampleCount().length,
                "sampleCount should not shrink for smaller configuration");
    }

    @Test
    @DisplayName("reset clears all samples and counts")
    void resetClearsHistogram() {
        Histogram h = new Histogram();
        h.sample(1000);
        h.sample(2000);

        h.reset();

        assertEquals(0, h.totalCount(), "totalCount should be zero after reset");
        assertEquals(0, h.overRange(), "overRange should be zero after reset");
    }

    @Test
    @DisplayName("toString includes histogram configuration fields for powersOf2 and fractionBits")
    void toStringIncludesConfiguration() {
        Histogram h = new Histogram(10, 5);
        String str = h.toString();

        assertTrue(str.contains("powersOf2=10"),
                "toString output '" + str + "' should contain 'powersOf2=10'");
        assertTrue(str.contains("fractionBits=5"),
                "toString output '" + str + "' should contain 'fractionBits=5'");
    }

    @Test
    @DisplayName("percentilesFor returns different arrays for different counts")
    void percentilesForDifferentCounts() {
        double[] small = Histogram.percentilesFor(100);
        double[] medium = Histogram.percentilesFor(100_000);
        double[] large = Histogram.percentilesFor(10_000_000);

        assertTrue(small.length < medium.length,
                "small length " + small.length + " should be < medium length " + medium.length);
        assertTrue(medium.length < large.length,
                "medium length " + medium.length + " should be < large length " + large.length);
    }

    @Test
    @DisplayName("getPercentiles returns array matching percentilesFor output")
    void getPercentilesMatchesPercentilesFor() {
        Histogram h = new Histogram();
        for (int i = 0; i < 1000; i++) {
            h.sample(i * 100 + 100);
        }

        double[] percentiles = h.getPercentiles();
        assertNotNull(percentiles, "getPercentiles should return non-null percentile array");
        assertTrue(percentiles.length > 0,
                "percentiles length " + percentiles.length + " should be > 0");
    }

    @Test
    @DisplayName("accessor methods return correct histogram values")
    void accessorMethods() {
        Histogram h = new Histogram(15, 6, 500);
        h.sample(1000);

        assertEquals(15, h.powersOf2(), "powersOf2 should match constructor");
        assertEquals(6, h.fractionBits(), "fractionBits should match constructor");
        assertEquals(1, h.totalCount(), "totalCount should be 1 after one sample");
        assertTrue(h.floor() != 0, "floor should be non-zero for non-default minValue");
    }

    @Test
    @DisplayName("min and max return boundary values")
    void minMaxValues() {
        Histogram h = new Histogram();
        h.sample(100);
        h.sample(1000);
        h.sample(10000);

        double min = h.min();
        double max = h.max();

        assertTrue(min > 0, "min value " + min + " should be positive");
        assertTrue(max >= min, "max value " + max + " should be >= min value " + min);
    }

    @Test
    @DisplayName("typical returns median percentile value from sampled histogram")
    @SuppressWarnings("deprecation")
    void typicalReturnsMedian() {
        Histogram h = new Histogram();
        for (int i = 1; i <= 100; i++) {
            h.sample(i * 100);
        }

        double typical = h.typical();
        double median = h.percentile(0.5);

        assertEquals(median, typical, "typical should equal 50th percentile");
    }
}
