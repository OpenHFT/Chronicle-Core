/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.pool.StringInterner;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 10:31
 */
@SuppressWarnings("deprecation")
class MathsTest extends CoreTestCommon {
    private static final double err = 5.1e-9;
    private static final int COUNT = Jvm.isArm() ? 500_000 : 3_000_000;
    private static final Random TEST_RANDOM = new Random(1);
    private ThreadDump threadDump;

    @Test
    @DisplayName("round1 scan meets expected iteration count")
    void round1scan() {
        final double factor = 1e1;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round1);
        iterations += roundUp(factor, Maths::round1up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 1));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 1));
        assertEquals(2 * COUNT, iterations, "round1 scan should execute expected number of rounding iterations with 1 decimal place precision");
    }

    @Test
    @DisplayName("round2 scan meets expected iteration count")
    void round2scan() {
        final double factor = 1e2;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round2);
        iterations += roundUp(factor, Maths::round2up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 2));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 2));
        assertEquals(2 * COUNT, iterations, "round2 scan should execute expected number of rounding iterations with 2 decimal place precision");
    }

    @Test
    @DisplayName("round3 scan meets expected iteration count")
    void round3scan() {
        final double factor = 1e3;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round3);
        iterations += roundUp(factor, Maths::round3up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 3));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 3));
        assertEquals(2 * COUNT, iterations, "round3 scan should execute expected number of rounding iterations with 3 decimal place precision");
    }

    @Test
    @DisplayName("round4 scan meets expected iteration count")
    void round4scan() {
        final double factor = 1e4;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round4);
        iterations += roundUp(factor, Maths::round4up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 4));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 4));
        assertEquals(2 * COUNT, iterations, "round4 scan should execute expected number of rounding iterations with 4 decimal place precision");
    }

    @Test
    @DisplayName("round5 scan meets expected iteration count")
    void round5scan() {
        final double factor = 1e5;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round5);
        iterations += roundUp(factor, Maths::round5up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 5));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 5));
        assertEquals(2 * COUNT, iterations, "round5 scan should execute expected number of rounding iterations with 5 decimal place precision");
    }

    @Test
    @DisplayName("round6 scan meets expected iteration count")
    void round6scan() {
        final double factor = 1e6;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round6);
        iterations += roundUp(factor, Maths::round6up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 6));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 6));
        assertEquals(2 * COUNT, iterations, "round6 scan should execute expected number of rounding iterations with 6 decimal place precision");
    }

    @Test
    @DisplayName("round7 scan meets expected iteration count")
    void round7scan() {
        final double factor = 1e7;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round7);
        iterations += roundUp(factor, Maths::round7up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 7));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 7));
        assertEquals(2 * COUNT, iterations, "round7 scan should execute expected number of rounding iterations with 7 decimal place precision");
    }

    @Test
    @DisplayName("round8 scan meets expected iteration count")
    void round8scan() {
        final double factor = 1e8;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round8);
        iterations += roundUp(factor, Maths::round8up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 8));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 8));
        assertEquals(2 * COUNT, iterations, "round8 scan should execute expected number of rounding iterations with 8 decimal place precision");
    }

    private int roundEither(double factor, Rounder rounder) {
        final double factor2 = 2 * factor;
        int iterations = 0;
        for (int i = 1; i < COUNT; i += 2) {
            double dm = i / factor2;
            final double ulp = Math.ulp(dm);
            double d = dm + ulp;
            double d0 = dm - ulp * 2;

            double e = (i + 1) / factor2;
            double e0 = (i - 1) / factor2;
            final String msg = "i: " + i;
            assertEquals(e, rounder.round(d), 0, msg);
            assertEquals(e0, rounder.round(d0), 0, msg);
            iterations++;
        }
        return iterations;
    }

    private int roundUp(double factor, Rounder rounder) {
        final double factor2 = 2 * factor;
        int iterations = 0;
        for (int i = 1; i < COUNT; i += 2) {
            double d = i / factor2;
            double d0 = d - Math.ulp(d) * 2;

            double e = (i + 1) / factor2;
            double e0 = (i - 1) / factor2;
            final String msg = "i: " + i;
            assertEquals(e, rounder.round(d), 0, msg);
            assertEquals(e0, rounder.round(d0), 0, msg);
            iterations++;
        }
        return iterations;
    }

    @Test
    @DisplayName("NaN handling stays consistent for rounders")
    void nanTest() {
        Rounder[] rounders = {
                Maths::round1,
                Maths::round1up,
                Maths::round2,
                Maths::round2up,
                Maths::round3,
                Maths::round3up,
                Maths::round4,
                Maths::round4up,
                Maths::round5,
                Maths::round5up,
                Maths::round6,
                Maths::round6up,
                Maths::round7,
                Maths::round7up,
                Maths::round8,
                Maths::round8up
        };
        for (int i = 0; i < rounders.length; i++) {
            Rounder rounder = rounders[i];
            double actual = rounder.round(Double.NaN);
            assertTrue(Double.isNaN(actual), "rounding function should preserve NaN values for rounder#" + i);
        }
    }

    @Test
    @DisplayName("digits reports length of decimal values")
    void digits() {
        assertEquals(1, Maths.digits(0), "single digit count for zero");
        assertEquals(1, Maths.digits(1), "single digit count for one");
        assertEquals(1, Maths.digits(9), "single digit count for maximum single digit");
        assertEquals(2, Maths.digits(10), "two digit count for minimum two digit number");
        assertEquals(2, Maths.digits(99), "two digit count for maximum two digit number");
        assertEquals(3, Maths.digits(100), "three digit count for minimum three digit number");
    }

    @Test
    @DisplayName("roundN rounds values with variable precision")
    void roundN() {
        assertEquals(1.5, Maths.roundN(1 + 0.25, 0.3f), 0.0, "rounding with 0.3 precision should round 1.25 up to 1.5");
        assertEquals(1, Maths.roundN(1 + 0.4999999, 0), 0.0, "rounding to integer precision should round down below 0.5");
        assertEquals(2.0, Maths.roundN(1 + 0.5, 0), 0.0, "rounding to integer precision should round up at exactly 0.5");
        assertEquals(1, Maths.roundN(1 + 0.24999999, 0.3), 0.0, "rounding with 0.3 precision should round down below threshold");
        assertEquals(1.25, Maths.roundN(1 + 0.24999999, 0.6), 0.0, "rounding with 0.6 precision should round to nearest 0.25");
        assertEquals(1.5, Maths.roundN(1 + 0.375, 0.6), 0.0, "rounding with 0.6 precision should round 1.375 to 1.5");
        assertEquals(1.5, Maths.roundN(1 + 0.624, 0.6), 0.0, "rounding with 0.6 precision should round 1.624 to 1.5");
        assertEquals(1.0, Maths.roundN(1.09999999999, 0.7), 0.0, "rounding with 0.7 precision should round down just below 1.1");
        assertEquals(1.2, Maths.roundN(1.10000000001, 0.7), 0.0, "rounding with 0.7 precision should round just above 1.1 to 1.2");
        assertEquals(1.2, Maths.roundN(1.29999999999, 0.7), 0.0, "rounding with 0.7 precision should keep value at 1.2");
        assertEquals(1.4, Maths.roundN(1.30000000001, 0.7), 0.0, "rounding with 0.7 precision should round just above 1.3 to 1.4");
        assertEquals(1.1, Maths.roundN(1.1 + 0.4999999e-1, 1), 0.0, "rounding with 1 decimal place should round down below 0.05");
        assertEquals(1.2, Maths.roundN(1.1 + 0.5e-1, 1), 0.0, "rounding with 1 decimal place should round up at exactly 0.05");

        assertEquals(1.11115, Maths.roundN(1.1111 + 0.74999999e-4, 4.3), 0.0, "rounding with 4.3 precision should round down below 0.000075");
        assertEquals(1.1112, Maths.roundN(1.1111 + 0.75e-4, 4.3), 0.0, "rounding with 4.3 precision should round up at exactly 0.000075");
    }

    @Test
    @DisplayName("roundingFactor applies half-step scaling rules")
    void roundingFactorHandlesHalfStepPrecision() {
        assertEquals(100L, Maths.roundingFactor(2.0), "roundingFactor should return base tens for integer precision");
        assertEquals(100L, Maths.roundingFactor(2.1), "roundingFactor should return base tens for low fractional precision");
        assertEquals(200L, Maths.roundingFactor(2.3), "roundingFactor should return 2x for mid fractional precision");
        assertEquals(400L, Maths.roundingFactor(2.6), "roundingFactor should return 4x for 0.6 precision");
        assertEquals(500L, Maths.roundingFactor(2.8), "roundingFactor should return 5x for 0.8 precision");
        assertEquals(800L, Maths.roundingFactor(2.9), "roundingFactor should return 8x for 0.9 precision");
        assertEquals(1000L, Maths.roundingFactor(2.99), "roundingFactor should return 10x for high fractional precision");
    }

    @Test
    @DisplayName("roundNup handles negative and large values")
    void roundNupHandlesNegativeAndLargeValues() {
        double positive = Maths.roundNup(1.234, 2);
        double negative = Maths.roundNup(-1.234, 2);
        assertEquals(-positive, negative, 0.0, "roundNup should mirror negative values");

        double limit = (1L << 52) / (double) Maths.roundingFactor(2);
        double large = limit + 1;
        assertEquals(large, Maths.roundNup(large, 2), "roundNup should return large values unchanged");
    }

    @Test
    @DisplayName("ceilN rounds values up by precision")
    void ceilN() {
        assertEquals(2, Maths.ceilN(2, 0), 0.0, "ceiling of exact integer should return same value");
        assertEquals(2, Maths.ceilN(1 + err, 0), 0.0, "ceiling should round up value just above 1 to 2");
        assertEquals(1.5, Maths.ceilN(1.5, 0.3f), 0.0, "ceiling with 0.3 precision should keep exact value at 1.5");
        assertEquals(2, Maths.ceilN(1.5 + err, 0.3f), 0.0, "ceiling with 0.3 precision should round up value just above 1.5");
        assertEquals(1.2, Maths.ceilN(1.2, 1), 0.0, "ceiling with 1 decimal place should keep exact value at 1.2");
        assertEquals(1.2, Maths.ceilN(1.1 + err, 1), 0.0, "ceiling with 1 decimal place should round up value just above 1.1");
    }

    @Test
    @DisplayName("floorN rounds values down by precision")
    void floorN() {
        assertEquals(1, Maths.floorN(2 - err, 0), 0.0, "floor should round down value just below 2 to 1");
        assertEquals(2.0, Maths.floorN(2, 0), 0.0, "floor of exact integer should return same value");
        assertEquals(1, Maths.floorN(1.5 - err, 0.3f), 0.0, "floor with 0.3 precision should round down value just below 1.5");
        assertEquals(1.5, Maths.floorN(1.5, 0.3f), 0.0, "floor with 0.3 precision should keep exact value at 1.5");
        assertEquals(1.1, Maths.floorN(1.2 - err, 1), 0.0, "floor with 1 decimal place should round down value just below 1.2");
        assertEquals(1.2, Maths.floorN(1.2, 1), 0.0, "floor with 1 decimal place should keep exact value at 1.2");
    }

    @Test
    @DisplayName("round1 rounds to one decimal place")
    void round1() {
        assertEquals(1.1, Maths.round1(1.1 + 0.4999999e-1), 0.0, "rounding to 1 decimal place should round down below 0.05");
        assertEquals(1.2, Maths.round1(1.1 + 0.5e-1), 0.0, "rounding to 1 decimal place should round up at exactly 0.05");
    }

    @Test
    @DisplayName("round2 rounds to two decimal places")
    void round2() {
        assertEquals(1.1, Maths.round2(1.1 + 0.4999999e-2), 0.0, "rounding to 2 decimal places should round down below 0.005");
        assertEquals(1.1 + 1e-2, Maths.round2(1.1 + 0.5e-2), 0.0, "rounding to 2 decimal places should round up at exactly 0.005");
    }

    @Test
    @DisplayName("round3 rounds to three decimal places")
    void round3() {
        assertEquals(1.1, Maths.round3(1.1 + 0.4999999e-3), 0.0, "rounding to 3 decimal places should round down below 0.0005");
        assertEquals(1.1 + 1e-3, Maths.round3(1.1 + 0.5e-3), 0.0, "rounding to 3 decimal places should round up at exactly 0.0005");
    }

    @Test
    @DisplayName("round4 rounds to four decimal places")
    void round4() {
        assertEquals(1.1, Maths.round4(1.1 + 0.4999999e-4), 0.0, "rounding to 4 decimal places should round down below 0.00005");
        assertEquals(1.1 + 1e-4, Maths.round4(1.1 + 0.5e-4), 0.0, "rounding to 4 decimal places should round up at exactly 0.00005");
    }

    @Test
    @DisplayName("round5 rounds to five decimal places")
    void round5() {
        assertEquals(1.1, Maths.round5(1.1 + 0.4999999e-5), 0.0, "rounding to 5 decimal places should round down below 0.000005");
        assertEquals(1.10001, Maths.round5(1.1 + 0.5e-5), 0.0, "rounding to 5 decimal places should round up at exactly 0.000005");
    }

    @Test
    @DisplayName("round6 rounds to six decimal places")
    void round6() {
        assertEquals(1.1, Maths.round6(1.1 + 0.4999999e-6), 0.0, "rounding to 6 decimal places should round down below 0.0000005");
        assertEquals(1.1 + 1e-6, Maths.round6(1.1 + 0.5e-6), 0.0, "rounding to 6 decimal places should round up at exactly 0.0000005");
    }

    @Test
    @DisplayName("round7 rounds to seven decimal places")
    void round7() {
        assertEquals(1.1, Maths.round7(1.1 + 0.4999999e-7), 0.0, "rounding to 7 decimal places should round down below 0.00000005");
        assertEquals(1.1000001, Maths.round7(1.1 + 0.5e-7), 0.0, "rounding to 7 decimal places should round up at exactly 0.00000005");
    }

    @Test
    @DisplayName("round8 rounds to eight decimal places")
    void round8() {
        assertEquals(1, Maths.round8(1), 0.0, "rounding exact integer to 8 decimal places should return same value");
        assertEquals(1.1, Maths.round8(1.1 + 0.4999999e-8), 0.0, "rounding to 8 decimal places should round down below 0.000000005");
        assertEquals(1.1 + 1e-8, Maths.round8(1.1 + 0.5e-8), 0.0, "rounding to 8 decimal places should round up at exactly 0.000000005");
        assertEquals((double) Long.MAX_VALUE, Maths.round8(Long.MAX_VALUE), 0.0, "rounding Long.MAX_VALUE to 8 decimal places should preserve value");
        assertEquals(Double.NaN, Maths.round8(Double.NaN), 0.0, "rounding NaN to 8 decimal places should return NaN");
    }

    @Test
    @DisplayName("floorN with extra precision is consistent")
    void floorNX() {
        assertEquals(1.14563, Maths.floorN(1.14563, 5), 0, "floor with 5 decimal places should keep exact value when no fractional remainder");
    }

    @Override
    @BeforeEach
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @Override
    @AfterEach
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    @DisplayName("intLog2 returns exponent for power of two inputs")
    void testIntLog2() throws IllegalArgumentException {
        for (int i = 0; i < 63; i++) {
            long l = 1L << i;
            assertEquals(i, Maths.intLog2(l), "intLog2 of power of 2 should return exponent at i=" + i);
            if (i > 0)
                assertEquals(i - 1, Maths.intLog2(l - 1), "intLog2 should floor to previous power of 2 for non-power values at i=" + i);
        }
        assertEquals(62, Maths.intLog2(Long.MAX_VALUE), "intLog2 of Long.MAX_VALUE should return 62");

        assertThrows(IllegalArgumentException.class, () -> Maths.intLog2(0), "intLog2 should reject zero input");
        for (int i = 0; i < 64; i++) {
            long l = -1L << i;
            assertThrows(IllegalArgumentException.class, () -> Maths.intLog2(l), "intLog2 should reject negative input at i=" + i);
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("rounding helpers match BigDecimal results consistently")
    void testRounding() {
        @NotNull Random rand = TEST_RANDOM;
        for (int i = 0; i < 1000; i++) {
            double d = Math.pow(1e18, rand.nextDouble()) / 1e6;
            @NotNull BigDecimal bd = BigDecimal.valueOf(d);
            assertEquals(bd.setScale(2, RoundingMode.HALF_UP).doubleValue(), Maths.round2(d), 5e-2, "round2 should match BigDecimal HALF_UP rounding to 2 places at sample " + i);
            assertEquals(bd.setScale(4, RoundingMode.HALF_UP).doubleValue(), Maths.round4(d), 5e-4, "round4 should match BigDecimal HALF_UP rounding to 4 places at sample " + i);
            assertEquals(bd.setScale(6, RoundingMode.HALF_UP).doubleValue(), Maths.round6(d), 5e-6, "round6 should match BigDecimal HALF_UP rounding to 6 places at sample " + i);
            if (d < 1e8)
                assertEquals(bd.setScale(8, RoundingMode.HALF_UP).doubleValue(), Maths.round8(d), 5e-8, "round8 should match BigDecimal HALF_UP rounding to 8 places for values below 1e8 at sample " + i);
        }
    }

    @Test
    @DisplayName("round4 sampled coverage keeps string length bounded")
    void round4SampledCoverage() {
        @NotNull double[] ds = new double[17];
        ds[0] = 1e-4;
        for (int i = 1; i < ds.length; i++) {
            ds[i] = 2 * ds[i - 1];
        }

        for (double x : ds) {
            double max = Math.min(2 * x, 10);
            for (int i = 0; i <= 100; i++) {
                double d = x + (max - x) * i / 100.0;
                String rounded = Double.toString(Maths.round4(d));
                assertTrue(rounded.length() <= 6, "round4 output string representation should not exceed 6 characters for x=" + x + ", i=" + i + ", d=" + d);
            }
        }
    }

    @Test
    @DisplayName("divideRoundUp rounds towards positive infinity correctly")
    void testDivideRoundUp() {
        assertEquals(2, Maths.divideRoundUp(10, 5), "exact division should return quotient without rounding");
        assertEquals(3, Maths.divideRoundUp(11, 5), "division with remainder should round up towards positive infinity");

        assertEquals(-2, Maths.divideRoundUp(-10, 5), "exact negative division should return quotient without rounding");
        assertEquals(-2, Maths.divideRoundUp(10, -5), "division by negative divisor should handle sign correctly");
        assertEquals(2, Maths.divideRoundUp(-10, -5), "division of two negatives should produce positive result");

        assertEquals(-3, Maths.divideRoundUp(-11, 5), "negative dividend with remainder should round up towards positive infinity");
        assertEquals(-3, Maths.divideRoundUp(11, -5), "positive dividend with negative divisor should round up towards positive infinity");
        assertEquals(3, Maths.divideRoundUp(-11, -5), "two negatives with remainder should round up towards positive infinity");
    }

    @Test
    @DisplayName("divideRoundUp rejects zero divisor with exception")
    void divideRoundUpZeroDivisorThrows() {
        assertThrows(ArithmeticException.class, () -> {
            long result = Maths.divideRoundUp(1, 0);
            assertEquals(0L, result, "result should not be observable when divisor is zero");
        }, "divideRoundUp should throw when divisor is zero");
    }

    @Test
    @DisplayName("divideRoundUp handles zero dividend boundary cases")
    void divideRoundUpZeroDividend() {
        assertEquals(0, Maths.divideRoundUp(0, 5), "zero dividend with positive divisor should return 0");
        assertEquals(0, Maths.divideRoundUp(0, -5), "zero dividend with negative divisor should return 0");
        assertEquals(0, Maths.divideRoundUp(0, 1), "zero dividend with unit divisor should return 0");
    }

    @Test
    @DisplayName("divideRoundUp handles unit values boundary cases")
    void divideRoundUpUnitValues() {
        assertEquals(1, Maths.divideRoundUp(1, 1), "1 divided by 1 should be 1");
        assertEquals(-1, Maths.divideRoundUp(-1, 1), "-1 divided by 1 should be -1");
        assertEquals(-1, Maths.divideRoundUp(1, -1), "1 divided by -1 should be -1");
        assertEquals(1, Maths.divideRoundUp(-1, -1), "-1 divided by -1 should be 1");
    }

    @Test
    @DisplayName("same treats floating point zeros and NaN")
    void sameFloating() {
        assertTrue(Maths.same(1.0, 1.0), "identical double values should be considered same");
        assertTrue(Maths.same(1.0f, 1.0f), "identical float values should be considered same");
        assertTrue(Maths.same(0.0, -0.0), "positive and negative zero doubles should be considered same");
        assertTrue(Maths.same(0.0f, -0.0f), "positive and negative zero floats should be considered same");
        assertTrue(Maths.same(-0.0, 0.0), "negative and positive zero doubles should be considered same");
        assertTrue(Maths.same(-0.0f, 0.0f), "negative and positive zero floats should be considered same");
        assertTrue(Maths.same(Double.NaN, Double.NaN), "NaN double values should be considered same to themselves");
        assertTrue(Maths.same(Float.NaN, Float.NaN), "NaN float values should be considered same to themselves");

        assertFalse(Maths.same(1.0, 2.0), "different double values should not be considered same");
        assertFalse(Maths.same(1.0f, 2.0f), "different float values should not be considered same");
        assertFalse(Maths.same(3.0, 2.0), "distinct double values should not be considered same");
        assertFalse(Maths.same(3.0f, 2.0f), "distinct float values should not be considered same");
        assertFalse(Maths.same(1, Double.NaN), "finite double should not be same as NaN");
        assertFalse(Maths.same(1, Float.NaN), "finite float should not be same as NaN");
        assertFalse(Maths.same(Double.NaN, 1), "NaN double should not be same as finite value");
        assertFalse(Maths.same(Float.NaN, 1), "NaN float should not be same as finite value");
    }

    @Test
    @DisplayName("hash64 stays stable for interned strings")
    void testHashStringBuilderFromInterner() {
        @NotNull StringInterner interner = new StringInterner(16);

        @NotNull final CharSequence csToHash = "557";
        @NotNull final StringBuilder sb = new StringBuilder(csToHash);

        long hash = Maths.hash64(sb);

        @Nullable String intern = interner.intern(csToHash);
        StringUtils.set(sb, intern);
        final long actual = Maths.hash64(sb);
        assertEquals(hash, actual, "hash64 should be stable across interned strings with same content");
        // overflowing the interner?
        StringUtils.set(sb, "xxxx");

        @Nullable String intern2 = interner.intern(csToHash);
        StringUtils.set(sb, intern2);
        final long actual2 = Maths.hash64(sb);
        assertEquals(hash, actual2, "hash64 should remain consistent after interner overflow");
    }

    @Test
    @DisplayName("hash64 stays stable for string inputs")
    void testHash64ForString() {
        // Empty
        String e1 = "";
        long eh1 = Maths.hash64(e1);

        assertEquals(0, eh1, "hash64 of empty string should be zero");

        // ASCII & Equality test
        String a1 = "Test";
        long ah1 = Maths.hash64(a1);

        String a2 = "T" + "e" + "st";
        long ah2 = Maths.hash64(a2);

        assertEquals(ah1, ah2, "hash64 should produce same result for equivalent strings");

        // UTF8 & Equality test
        String u1 = "\u20AC";
        long uh1 = Maths.hash64(u1);
        assertEquals(1177128352603971756L, uh1, "hash64 of Euro symbol should match known value");

        String u2 = "\u20AC\u20AC".substring(0, 1);
        long uh2 = Maths.hash64(u2);

        assertEquals(uh1, uh2, "hash64 should be consistent for same Unicode character from different sources");

        // Mixed
        StringBuilder mixedSb = new StringBuilder().append('\u20AC');
        mixedSb.setLength(0);
        mixedSb.append('X');

        assertEquals(Maths.hash64("X"), Maths.hash64(mixedSb.toString()), "hash64 should produce same result for string regardless of StringBuilder's prior content");

        // UT8 & Not-equal hashes
        assertNotEquals(Maths.hash64("\u0394"), Maths.hash64("\u0393"),
                "hash64 should differ for distinct Unicode code points");
    }

    @Test
    @DisplayName("hash64 handles null and non-string CharSequence inputs")
    void hash64HandlesNullAndCustomCharSequence() {
        assertEquals(0, Maths.hash64((CharSequence) null),
                "hash64 should return zero for null CharSequence");
        assertEquals(0, Maths.hash64((CharSequence) new StringBuilder()),
                "hash64 should return zero for empty CharSequence");

        CharSequence sb = new StringBuilder("abc");
        long first = Maths.hash64(sb);
        long second = Maths.hash64(new StringBuilder("abc"));
        assertEquals(first, second, "hash64 should be consistent for non-string CharSequence content");

        assertEquals(Maths.hash64("abc"), Maths.hash64((CharSequence) "abc"),
                "hash64 should match when CharSequence is a String instance");
    }

    @Test
    @DisplayName("floor and ceil match BigDecimal rounding")
    void floorNceilN() {
        double d = 64.0915946999999;
        BigDecimal bd = BigDecimal.valueOf(d);
        for (int i = 0; i < 19; i++) {
            double ceil0 = bd.setScale(i, RoundingMode.CEILING).doubleValue();
            double floor0 = bd.setScale(i, RoundingMode.FLOOR).doubleValue();
            double ceil = Maths.ceilN(d, i);
            double floor = Maths.floorN(d, i);
            assertEquals(ceil0, ceil, 0, "ceilN should match BigDecimal CEILING rounding for " + i + " decimal places");
            assertEquals(floor0, floor, 0, "floorN should match BigDecimal FLOOR rounding for " + i + " decimal places");
        }
    }

    @Test
    @DisplayName("toInt8 converts within byte range safely")
    void testToInt8() {
        assertEquals((byte) 127, Maths.toInt8(127), "maximum byte value should convert to int8 without overflow");
        assertEquals((byte) -128, Maths.toInt8(-128), "minimum byte value should convert to int8 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(128), "toInt8 should reject values above 127");
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(-129), "toInt8 should reject values below -128");
    }

    @Test
    @DisplayName("toInt16 converts within short range safely")
    void testToInt16() {
        assertEquals((short) 32767, Maths.toInt16(32767), "maximum short value should convert to int16 without overflow");
        assertEquals((short) -32768, Maths.toInt16(-32768), "minimum short value should convert to int16 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(32768), "toInt16 should reject values above 32767");
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(-32769), "toInt16 should reject values below -32768");
    }

    @Test
    @DisplayName("toInt32 converts within int range safely")
    void testToInt32() {
        assertEquals(2147483647, Maths.toInt32(2147483647L), "maximum int value should convert to int32 without overflow");
        assertEquals(-2147483648, Maths.toInt32(-2147483648L), "minimum int value should convert to int32 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(2147483648L), "toInt32 should reject values above Integer.MAX_VALUE");
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(-2147483649L), "toInt32 should reject values below Integer.MIN_VALUE");
    }

    @Test
    @DisplayName("toUInt8 converts within unsigned byte range")
    void testToUInt8() {
        assertEquals((short) 255, Maths.toUInt8(255), "maximum unsigned byte value should convert to uint8 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(256), "toUInt8 should reject values above 255");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(-1), "toUInt8 should reject negative values");
    }

    @Test
    @DisplayName("toUInt16 converts within unsigned short range")
    void testToUInt16() {
        assertEquals(65535, Maths.toUInt16(65535), "maximum unsigned short value should convert to uint16 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(65536), "toUInt16 should reject values above 65535");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(-1), "toUInt16 should reject negative values");
    }

    @Test
    @DisplayName("toUInt31 converts within 31-bit range safely")
    void testToUInt31() {
        assertEquals(2147483647, Maths.toUInt31(2147483647L), "maximum 31-bit unsigned value should convert to uint31 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(2147483648L), "toUInt31 should reject values above 2^31-1");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(-1), "toUInt31 should reject negative values");
    }

    @Test
    @DisplayName("toUInt32 converts within unsigned int range")
    void testToUInt32() {
        assertEquals(4294967295L, Maths.toUInt32(4294967295L), "maximum unsigned int value should convert to uint32 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(4294967296L), "toUInt32 should reject values above 2^32-1");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(-1), "toUInt32 should reject negative values");
    }

    @Test
    @DisplayName("hash64 differs for distinct input values")
    void testHash64() {
        long hashValue1 = Maths.hash64(123456789L);
        long hashValue2 = Maths.hash64(987654321L);
        assertNotEquals(hashValue1, hashValue2, "hash64 should produce different hashes for different input values");
    }

    @Test
    @DisplayName("tens computes powers of ten correctly")
    void testTens() {
        assertEquals(100, Maths.tens(2), "tens(2) should return 10^2");
        assertEquals(1, Maths.tens(0), "tens(0) should return 10^0 which is 1");
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(-1), "tens should reject negative exponent");
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(19), "tens should reject exponent above supported range");
    }

    @Test
    @DisplayName("hash overloads vary with inputs properly")
    void testHashMethods() {
        Object o1 = "test1";
        Object o2 = "test2";
        Object o3 = "test3";
        Object o4 = "test4";
        int hash1 = Maths.hash(o1);
        int hash2 = Maths.hash(o1, o2);
        int hash3 = Maths.hash(o1, o2, o3);
        int hash4 = Maths.hash(o1, o2, o3, o4);

        assertNotEquals(hash1, hash2, "hash of one argument should differ from hash of two arguments");
        assertNotEquals(hash2, hash3, "hash of two arguments should differ from hash of three arguments");
        assertNotEquals(hash3, hash4, "hash of three arguments should differ from hash of four arguments");
        Object o5 = "test5";
        assertNotEquals(hash4, Maths.hash(o1, o2, o3, o4, o5),
                "hash of four arguments should differ from hash of five arguments");
    }

    @Test
    @DisplayName("asDouble converts mantissa and scale correctly")
    void asDouble() {
        assertEquals(0.00017853, Maths.asDouble(17853, 0, false, 8), 0.0, "converting mantissa 17853 with scale 8 should produce correct decimal");
        assertEquals(0.00035706, Maths.asDouble(35706, 0, false, 8), 0.0, "converting mantissa 35706 with scale 8 should produce correct decimal");

        assertEquals(1.475344805371041E-8, Maths.asDouble(1475344805371041L, 0, false, 23), 0.0, "converting large mantissa with scale 23 should handle precision correctly");
        assertEquals(1.000000000000003E12, Maths.asDouble(1000000000000003L, 0, false, 3), 0.0, "converting large mantissa with scale 3 should produce trillion-scale value");
        assertEquals(-1.453448689138e11, Maths.asDouble(1453448689138L, 0, true, 1), 0.0, "negative flag should produce negative result");
        assertEquals(999999999999.994, Maths.asDouble(999999999999994L, 0, false, 3), 0.0, "converting near-trillion mantissa with scale 3 should maintain precision");
        assertEquals(-1.16823E70, Maths.asDouble(116823, 0, true, -65), 0.0, "negative scale should multiply by powers of 10 to create very large value");
        assertEquals(12.345, Maths.asDouble(12345, 0, false, 3), 0.0, "converting 12345 with scale 3 should produce 12.345");
        assertEquals(1e-5, Maths.asDouble(100000000000L, 0, false, 16), 0.0, "large mantissa with large scale should produce small decimal");
        assertEquals(1.4753448053710411E-8, Maths.asDouble(14753448053710411L, 0, false, 24), 0.0, "very large mantissa with scale 24 should handle extended precision");
        assertEquals(1.720578937592997e-8, Maths.asDouble(1720578937592997L, 0, false, 23), 0.0, "large mantissa with scale 23 should produce small scientific notation value");
        //
        assertEquals(-12.345, Maths.asDouble(12345, 0, true, 3), 0.0, "negative flag should negate result");
        assertEquals(98760.0, Maths.asDouble(12345, 3, false, 0), 0.0, "positive exponent should multiply mantissa by powers of 10");
        assertEquals(1543.125, Maths.asDouble(12345, -3, false, 0), 0.0, "negative exponent should divide mantissa by powers of 10");
        assertEquals(1.2345E-26, Maths.asDouble(12345, 0, false, 30), 0.0, "very large scale should produce very small scientific notation value");
        assertEquals(123450000000000000L, Maths.asDouble(1234500000000000000L, 0, false, 1), 0.0, "large mantissa with scale 1 should divide by 10");
        assertEquals(1234500, Maths.asDouble(12345, 0, false, -2), 0.0, "negative scale -2 should multiply by 100");
        assertEquals(1.23E30, Maths.asDouble(123, 0, false, -28), 0.0, "large negative scale should produce very large scientific notation value");
    }

    @Test
    @DisplayName("nextPower2 for int values rounds up")
    void testNextPower2Int() {
        // Test cases where n is less than min
        assertEquals(8, Maths.nextPower2(3, 8), "int: value 3 below min 8 should return min");
        assertEquals(16, Maths.nextPower2(5, 16), "int: value 5 below min 16 should return min");

        // Test cases where n is equal to min
        assertEquals(8, Maths.nextPower2(8, 8), "int: value 8 equals min 8 should return that power");
        assertEquals(16, Maths.nextPower2(16, 16), "int: value 16 equals min 16 should return that power");

        // Test cases where n is already a power of two
        assertEquals(32, Maths.nextPower2(32, 16), "int: value 32 already power of 2 should return unchanged");
        assertEquals(64, Maths.nextPower2(64, 32), "int: value 64 already power of 2 should return unchanged");

        // Test cases where n is not a power of two
        assertEquals(128, Maths.nextPower2(70, 16), "int: value 70 should round up to next power of 2 (128)");
        assertEquals(256, Maths.nextPower2(130, 64), "int: value 130 should round up to next power of 2 (256)");

        // Test maximum int value
        assertEquals(1 << 30, Maths.nextPower2(Integer.MAX_VALUE, 1), "Integer.MAX_VALUE should round to largest representable power of 2");

        // Test minimum n and min
        assertEquals(1, Maths.nextPower2(0, 1), "int: zero should round up to minimum power of 2");
        assertEquals(1, Maths.nextPower2(1, 1), "int: one is already power of 2 and should return unchanged");
    }

    @Test
    @DisplayName("nextPower2 for long values rounds up")
    void testNextPower2Long() {
        // Test cases where n is less than min
        assertEquals(16L, Maths.nextPower2(9L, 16L), "long: value 9 below min 16 should return min");
        assertEquals(32L, Maths.nextPower2(17L, 32L), "long: value 17 below min 32 should return min");

        // Test cases where n is equal to min
        assertEquals(64L, Maths.nextPower2(64L, 64L), "long: value 64 equals min 64 should return that power");
        assertEquals(128L, Maths.nextPower2(128L, 128L), "long: value 128 equals min 128 should return that power");

        // Test cases where n is already a power of two
        assertEquals(256L, Maths.nextPower2(256L, 128L), "long: value 256 already power of 2 should return unchanged");
        assertEquals(512L, Maths.nextPower2(512L, 256L), "long: value 512 already power of 2 should return unchanged");

        // Test cases where n is not a power of two
        assertEquals(1024L, Maths.nextPower2(777L, 256L), "long: value 777 should round up to next power of 2 (1024)");
        assertEquals(2048L, Maths.nextPower2(1300L, 1024L), "long: value 1300 should round up to next power of 2 (2048)");

        // Test large values
        assertEquals(1L << 62, Maths.nextPower2(Long.MAX_VALUE, 1L), "Long.MAX_VALUE should round to largest representable power of 2");

        // Test minimum n and min
        assertEquals(1L, Maths.nextPower2(0L, 1L), "long: zero should round up to minimum power of 2");
        assertEquals(1L, Maths.nextPower2(1L, 1L), "long: one is already power of 2 and should return unchanged");
    }

    @Test
    @DisplayName("nextPower2 rejects invalid int min values")
    void testNextPower2IntInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(10, 7),
                "nextPower2 should reject non-power-of-two minimum for int");
    }

    @Test
    @DisplayName("nextPower2 rejects invalid long min values")
    void testNextPower2LongInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(20L, 9L),
                "nextPower2 should reject non-power-of-two minimum for long");
    }

    @Test
    @DisplayName("isPowerOf2 identifies powers of two correctly")
    void testIsPowerOf2() {
        assertTrue(Maths.isPowerOf2(1), "1 is 2^0 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(2), "2 is 2^1 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(4), "4 is 2^2 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(8), "8 is 2^3 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(16), "16 is 2^4 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(32), "32 is 2^5 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(64), "64 is 2^6 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(128), "128 is 2^7 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(256), "256 is 2^8 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(512), "512 is 2^9 and should be recognized as power of 2");
        assertTrue(Maths.isPowerOf2(1024), "1024 is 2^10 and should be recognized as power of 2");

        assertFalse(Maths.isPowerOf2(0), "zero is not a power of 2");
        assertFalse(Maths.isPowerOf2(3), "3 is not a power of 2");
        assertFalse(Maths.isPowerOf2(5), "5 is not a power of 2");
        assertFalse(Maths.isPowerOf2(6), "6 is not a power of 2");
        assertFalse(Maths.isPowerOf2(7), "7 is not a power of 2");
        assertFalse(Maths.isPowerOf2(9), "9 is not a power of 2");
        assertFalse(Maths.isPowerOf2(10), "10 is not a power of 2");
        assertFalse(Maths.isPowerOf2(12), "12 is not a power of 2");
        assertFalse(Maths.isPowerOf2(15), "15 is not a power of 2");
        assertFalse(Maths.isPowerOf2(18), "18 is not a power of 2");
        assertFalse(Maths.isPowerOf2(20), "20 is not a power of 2");
    }

    @Test
    @DisplayName("nextPower2 handles int edge cases correctly")
    void testEdgeCasesInt() {
        // Test when n is negative
        assertEquals(16, Maths.nextPower2(-5, 16), "int edge case: negative value -5 should return min 16");
        // Test when min is greater than n and is the next power of two
        assertEquals(32, Maths.nextPower2(17, 32), "int edge case: value 17 below min 32 should return min");
    }

    @Test
    @DisplayName("nextPower2 handles long edge cases correctly")
    void testEdgeCasesLong() {
        // Test when n is negative
        assertEquals(64L, Maths.nextPower2(-10L, 64L), "long edge case: negative value -10 should return min 64");
        // Test when min is greater than n and is the next power of two
        assertEquals(128L, Maths.nextPower2(65L, 128L), "long edge case: value 65 below min 128 should return min");
    }

    @Test
    @DisplayName("fives returns powers of five correctly")
    void testFives() {
        assertEquals(1, Maths.fives(0), "fives(0) should return 5^0 which is 1");
        assertEquals(5, Maths.fives(1), "fives(1) should return 5^1 which is 5");
        assertEquals(25, Maths.fives(2), "fives(2) should return 5^2 which is 25");
        assertEquals(125, Maths.fives(3), "fives(3) should return 5^3 which is 125");
        assertEquals(625, Maths.fives(4), "fives(4) should return 5^4 which is 625");
    }

    @Test
    @DisplayName("toInt32 with custom message includes message in exception")
    void testToInt32WithMessage() {
        assertThrows(ArithmeticException.class,
                () -> Maths.toInt32(Long.MAX_VALUE, "Value %d is out of int range"),
                "toInt32 with message should throw for overflow");

        assertEquals(100, Maths.toInt32(100L, "Value %d is out of range"),
                "toInt32 with message should return value when in range");
    }

    @Test
    @DisplayName("hash handles null object input correctly")
    void testHashNullObject() {
        assertEquals(0, Maths.hash((Object) null), "hash of null should return 0");
    }

    @Test
    @DisplayName("add combines whole and fractional parts")
    void testAdd() {
        double result = Maths.add(10L, 1L, 2L);
        assertEquals(10.5, result, 0.0001, "add(10, 1, 2) should return 10.5");

        result = Maths.add(100L, 1L, 4L);
        assertEquals(100.25, result, 0.0001, "add(100, 1, 4) should return 100.25");
    }

    @Test
    @DisplayName("roundingFactor handles fractional digits edge cases")
    void testRoundingFactorFractionalEdgeCases() {
        // Test boundary cases for the switch statement
        assertEquals(1L, Maths.roundingFactor(0.0), "roundingFactor(0.0) should return 1");
        assertEquals(1L, Maths.roundingFactor(0.1), "roundingFactor(0.1) should return 1");
        assertEquals(1L, Maths.roundingFactor(0.2), "roundingFactor(0.2) should return 1");
        assertEquals(2L, Maths.roundingFactor(0.3), "roundingFactor(0.3) should return 2");
        assertEquals(2L, Maths.roundingFactor(0.4), "roundingFactor(0.4) should return 2");
        assertEquals(2L, Maths.roundingFactor(0.5), "roundingFactor(0.5) should return 2");
        assertEquals(4L, Maths.roundingFactor(0.6), "roundingFactor(0.6) should return 4");
        assertEquals(5L, Maths.roundingFactor(0.7), "roundingFactor(0.7) should return 5");
        assertEquals(5L, Maths.roundingFactor(0.8), "roundingFactor(0.8) should return 5");
        assertEquals(8L, Maths.roundingFactor(0.9), "roundingFactor(0.9) should return 8");
    }

    @Test
    @DisplayName("agitate produces different output for different inputs")
    void testAgitate() {
        long hash1 = Maths.agitate(12345L);
        long hash2 = Maths.agitate(54321L);
        assertNotEquals(hash1, hash2, "agitate should produce different results for different inputs");

        // Verify the transformation is consistent
        assertEquals(hash1, Maths.agitate(12345L), "agitate should be deterministic");
    }

    @Test
    @DisplayName("digits handles edge cases for multi-digit numbers")
    void testDigitsEdgeCases() {
        assertEquals(1, Maths.digits(0), "digits(0) should return 1");
        assertEquals(1, Maths.digits(9), "digits(9) should return 1");
        assertEquals(10, Maths.digits(1_000_000_000L), "digits(1 billion) should return 10");
        assertEquals(19, Maths.digits(1_000_000_000_000_000_000L), "digits(10^18) should return 19");
    }

    @Test
    @DisplayName("hash64 with StringBuilder returns consistent hash value")
    void testHash64StringBuilder() {
        StringBuilder sb = new StringBuilder("test string");
        long hash1 = Maths.hash64(sb);
        long hash2 = Maths.hash64(sb);
        assertEquals(hash1, hash2, "hash64 should return consistent value for same StringBuilder");

        StringBuilder sb2 = new StringBuilder("different");
        long hash3 = Maths.hash64(sb2);
        assertNotEquals(hash1, hash3, "hash64 should return different value for different content");
    }

    @Test
    @DisplayName("round1 handles negative values with correct rounding")
    void testRound1Negative() {
        assertEquals(-1.5, Maths.round1(-1.45), 0.0, "round1 should handle negative values");
        assertEquals(-1.4, Maths.round1(-1.44), 0.0, "round1 should handle negative values below threshold");
    }

    @Test
    @DisplayName("round1 handles values beyond precision threshold")
    void testRound1LargeValues() {
        double large = (1L << 52) / 10.0 + 1;
        assertEquals(large, Maths.round1(large), 0.0, "round1 should return large values unchanged");
    }

    @Test
    @DisplayName("ceilN and floorN handle large values at precision boundary")
    void testCeilNFloorNLargeValues() {
        double largeValue = 1e16;
        assertEquals(largeValue, Maths.ceilN(largeValue, 2), "ceilN should return large values unchanged");
        assertEquals(largeValue, Maths.floorN(largeValue, 2), "floorN should return large values unchanged");
    }

    @Test
    @DisplayName("asDouble handles zero value with correct sign")
    void testAsDoubleZero() {
        assertEquals(0.0, Maths.asDouble(0L, 0, false, 0), "asDouble(0,0,false,0) should return 0.0");
        assertEquals(-0.0, Maths.asDouble(0L, 0, true, 0), "asDouble(0,0,true,0) should return -0.0");
    }

    @Test
    @DisplayName("asDouble handles large negative decimal places")
    void testAsDoubleLargeNegativeScale() {
        // decimalPlaces < -27
        double result = Maths.asDouble(1L, 0, false, -28);
        assertTrue(result > 1e27, "asDouble with scale -28 should produce very large number");
    }

    @Test
    @DisplayName("roundN with large values returns unchanged")
    void roundNLargeValuesReturnsUnchanged() {
        // Test the branch where value exceeds WHOLE_NUMBER / factor
        double wholeNumber = (double) (1L << 52);
        double largeValue = wholeNumber + 1;

        // roundN(double, int) large value branch
        assertEquals(largeValue, Maths.roundN(largeValue, 2), "roundN should return large values unchanged");
        assertEquals(-largeValue, Maths.roundN(-largeValue, 2), "roundN should return large negative values unchanged");

        // roundN(double, double) large value branch
        assertEquals(largeValue, Maths.roundN(largeValue, 2.5), "roundN with fractional digits should return large values unchanged");
    }

    @Test
    @DisplayName("round2-8 with very large values returns unchanged")
    void roundLargeValuesReturnsUnchanged() {
        double wholeNumber = (double) (1L << 52);

        // Each roundX method has a branch for large values
        double large2 = wholeNumber / 100 + 1;
        assertEquals(large2, Maths.round2(large2), "round2 should return large values unchanged");

        double large3 = wholeNumber / 1000 + 1;
        assertEquals(large3, Maths.round3(large3), "round3 should return large values unchanged");

        double large4 = wholeNumber / 10000 + 1;
        assertEquals(large4, Maths.round4(large4), "round4 should return large values unchanged");

        double large5 = wholeNumber / 100000 + 1;
        assertEquals(large5, Maths.round5(large5), "round5 should return large values unchanged");

        double large6 = wholeNumber / 1000000 + 1;
        assertEquals(large6, Maths.round6(large6), "round6 should return large values unchanged");

        double large7 = wholeNumber / 10000000 + 1;
        assertEquals(large7, Maths.round7(large7), "round7 should return large values unchanged");

        double large8 = wholeNumber / 100000000 + 1;
        assertEquals(large8, Maths.round8(large8), "round8 should return large values unchanged");
    }

    @Test
    @DisplayName("roundXup methods handle NaN inputs correctly")
    void roundUpMethodsHandleNaN() {
        // Test NaN branch in each roundXup method
        assertTrue(Double.isNaN(Maths.round2up(Double.NaN)), "round2up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round3up(Double.NaN)), "round3up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round4up(Double.NaN)), "round4up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round5up(Double.NaN)), "round5up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round6up(Double.NaN)), "round6up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round7up(Double.NaN)), "round7up should preserve NaN");
        assertTrue(Double.isNaN(Maths.round8up(Double.NaN)), "round8up should preserve NaN");
    }

    @Test
    @DisplayName("ceilN and floorN with fractional digits handle large values")
    void ceilNFloorNFractionalLargeValues() {
        double wholeNumber = (double) (1L << 52);
        double largeValue = wholeNumber + 1;

        // Test the branch where value exceeds WHOLE_NUMBER / factor
        assertEquals(largeValue, Maths.ceilN(largeValue, 2.5), "ceilN with fractional digits should return large values unchanged");
        assertEquals(largeValue, Maths.floorN(largeValue, 2.5), "floorN with fractional digits should return large values unchanged");
    }

    @Test
    @DisplayName("nextPower2 int handles overflow to max power of 2")
    void nextPower2IntOverflowToMaxPower() {
        // Test the overflow branch where n becomes negative or exceeds 2^30
        assertEquals(1 << 30, Maths.nextPower2((1 << 30) + 1, 1), "nextPower2 should return 2^30 when result would overflow");
        assertEquals(1 << 30, Maths.nextPower2(Integer.MAX_VALUE - 100, 1), "nextPower2 should return 2^30 for large values near MAX_VALUE");
    }

    @Test
    @DisplayName("nextPower2 long handles overflow to max power of 2")
    void nextPower2LongOverflowToMaxPower() {
        // Test the overflow branch where n becomes negative
        assertEquals(1L << 62, Maths.nextPower2((1L << 62) + 1, 1L), "nextPower2 should return 2^62 when result would overflow");
    }

    @Test
    @DisplayName("nextPower2 int boundary conditions for mutation testing")
    void nextPower2IntBoundaryConditions() {
        // Test n exactly equals min (kills n <= min boundary mutation)
        assertEquals(4, Maths.nextPower2(4, 4), "int: n equals min should return min");
        assertEquals(8, Maths.nextPower2(8, 8), "int: n equals min 8 should return 8");

        // Test n = min - 1 (ensures < vs <= distinction)
        assertEquals(4, Maths.nextPower2(3, 4), "int: n = min-1 should return min");
        assertEquals(8, Maths.nextPower2(7, 8), "int: n = min-1 for min 8 should return 8");

        // Test n = min + 1 (ensures > vs >= distinction)
        assertEquals(8, Maths.nextPower2(5, 4), "int: n = min+1 should round to next power");

        // Test exact power of 2 values (kills power-of-2 check mutation)
        assertEquals(2, Maths.nextPower2(2, 1), "int: 2 is power of 2, return 2");
        assertEquals(4, Maths.nextPower2(4, 1), "int: 4 is power of 2, return 4");
        assertEquals(16, Maths.nextPower2(16, 1), "int: 16 is power of 2, return 16");

        // Test n = 1 (boundary for decrement operation)
        assertEquals(1, Maths.nextPower2(1, 1), "int: 1 should return 1");

        // Test overflow boundary more precisely
        assertEquals(1 << 30, Maths.nextPower2(1 << 30, 1), "int: 2^30 is max valid power");
        assertEquals(1 << 30, Maths.nextPower2((1 << 30) - 1, 1), "int: 2^30-1 should round to 2^30");
    }

    @Test
    @DisplayName("nextPower2 long boundary conditions for mutation testing")
    void nextPower2LongBoundaryConditions() {
        // Test n exactly equals min
        assertEquals(4L, Maths.nextPower2(4L, 4L), "long: n equals min should return min");
        assertEquals(16L, Maths.nextPower2(16L, 16L), "long: n equals min 16 should return 16");

        // Test n = min - 1
        assertEquals(4L, Maths.nextPower2(3L, 4L), "long: n = min-1 should return min");

        // Test n = min + 1
        assertEquals(8L, Maths.nextPower2(5L, 4L), "long: n = min+1 should round to next power");

        // Test exact power of 2 values
        assertEquals(2L, Maths.nextPower2(2L, 1L), "long: 2 is power of 2, return 2");
        assertEquals(1024L, Maths.nextPower2(1024L, 1L), "long: 1024 is power of 2, return 1024");

        // Test n = 1
        assertEquals(1L, Maths.nextPower2(1L, 1L), "long: 1 should return 1");

        // Test overflow boundary
        assertEquals(1L << 62, Maths.nextPower2(1L << 62, 1L), "long: 2^62 is max valid power");
        assertEquals(1L << 62, Maths.nextPower2((1L << 62) - 1, 1L), "long: 2^62-1 should round to 2^62");
    }

    @Test
    @DisplayName("hash64 for String uses byte optimisation on Java 9+")
    void hash64StringBytePath() {
        // ASCII-only string should use byte path on Java 9+
        String ascii = "hello world";
        long hash1 = Maths.hash64(ascii);
        long hash2 = Maths.hash64(ascii);
        assertEquals(hash1, hash2, "hash64 should be deterministic for ASCII strings");

        // Non-ASCII string should use char path
        String unicode = "hello \u20AC world";
        long hash3 = Maths.hash64(unicode);
        assertNotEquals(hash1, hash3, "hash64 should differ for ASCII vs Unicode strings");
    }

    @Test
    @DisplayName("hash64 for StringBuilder uses byte optimisation on Java 9+")
    void hash64StringBuilderBytePath() {
        // ASCII-only StringBuilder should use byte path on Java 9+
        StringBuilder ascii = new StringBuilder("hello world");
        long hash1 = Maths.hash64(ascii);

        // Non-ASCII StringBuilder should use char path
        StringBuilder unicode = new StringBuilder("hello \u20AC world");
        long hash2 = Maths.hash64(unicode);

        assertNotEquals(hash1, hash2, "hash64 should differ for ASCII vs Unicode StringBuilders");
    }

    @Test
    @DisplayName("hash64 String rejects null with exception")
    void hash64StringRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Maths.hash64((String) null),
                "hash64(String) should throw for null input");
    }

    @FunctionalInterface
    public interface Rounder {
        double round(double d);
    }
}
