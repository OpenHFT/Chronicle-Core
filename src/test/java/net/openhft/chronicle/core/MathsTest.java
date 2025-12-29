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

    @DisplayName("round1 scan meets expected iteration count")
    @Test
    void round1scan() {
        final double factor = 1e1;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round1);
        iterations += roundUp(factor, Maths::round1up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 1));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 1));
        assertEquals(2 * COUNT, iterations, "round1 scan should execute expected number of rounding iterations with 1 decimal place precision");
    }

    @DisplayName("round2 scan meets expected iteration count")
    @Test
    void round2scan() {
        final double factor = 1e2;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round2);
        iterations += roundUp(factor, Maths::round2up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 2));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 2));
        assertEquals(2 * COUNT, iterations, "round2 scan should execute expected number of rounding iterations with 2 decimal place precision");
    }

    @DisplayName("round3 scan meets expected iteration count")
    @Test
    void round3scan() {
        final double factor = 1e3;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round3);
        iterations += roundUp(factor, Maths::round3up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 3));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 3));
        assertEquals(2 * COUNT, iterations, "round3 scan should execute expected number of rounding iterations with 3 decimal place precision");
    }

    @DisplayName("round4 scan meets expected iteration count")
    @Test
    void round4scan() {
        final double factor = 1e4;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round4);
        iterations += roundUp(factor, Maths::round4up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 4));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 4));
        assertEquals(2 * COUNT, iterations, "round4 scan should execute expected number of rounding iterations with 4 decimal place precision");
    }

    @DisplayName("round5 scan meets expected iteration count")
    @Test
    void round5scan() {
        final double factor = 1e5;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round5);
        iterations += roundUp(factor, Maths::round5up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 5));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 5));
        assertEquals(2 * COUNT, iterations, "round5 scan should execute expected number of rounding iterations with 5 decimal place precision");
    }

    @DisplayName("round6 scan meets expected iteration count")
    @Test
    void round6scan() {
        final double factor = 1e6;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round6);
        iterations += roundUp(factor, Maths::round6up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 6));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 6));
        assertEquals(2 * COUNT, iterations, "round6 scan should execute expected number of rounding iterations with 6 decimal place precision");
    }

    @DisplayName("round7 scan meets expected iteration count")
    @Test
    void round7scan() {
        final double factor = 1e7;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round7);
        iterations += roundUp(factor, Maths::round7up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 7));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 7));
        assertEquals(2 * COUNT, iterations, "round7 scan should execute expected number of rounding iterations with 7 decimal place precision");
    }

    @DisplayName("round8 scan meets expected iteration count")
    @Test
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

    @DisplayName("NaN handling stays consistent for rounders")
    @Test
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

    @DisplayName("digits reports length of decimal values")
    @Test
    void digits() {
        assertEquals(1, Maths.digits(0), "single digit count for zero");
        assertEquals(1, Maths.digits(1), "single digit count for one");
        assertEquals(1, Maths.digits(9), "single digit count for maximum single digit");
        assertEquals(2, Maths.digits(10), "two digit count for minimum two digit number");
        assertEquals(2, Maths.digits(99), "two digit count for maximum two digit number");
        assertEquals(3, Maths.digits(100), "three digit count for minimum three digit number");
    }

    @DisplayName("roundN rounds values with variable precision")
    @Test
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

    @DisplayName("ceilN rounds values up by precision")
    @Test
    void ceilN() {
        assertEquals(2, Maths.ceilN(2, 0), 0.0, "ceiling of exact integer should return same value");
        assertEquals(2, Maths.ceilN(1 + err, 0), 0.0, "ceiling should round up value just above 1 to 2");
        assertEquals(1.5, Maths.ceilN(1.5, 0.3f), 0.0, "ceiling with 0.3 precision should keep exact value at 1.5");
        assertEquals(2, Maths.ceilN(1.5 + err, 0.3f), 0.0, "ceiling with 0.3 precision should round up value just above 1.5");
        assertEquals(1.2, Maths.ceilN(1.2, 1), 0.0, "ceiling with 1 decimal place should keep exact value at 1.2");
        assertEquals(1.2, Maths.ceilN(1.1 + err, 1), 0.0, "ceiling with 1 decimal place should round up value just above 1.1");
    }

    @DisplayName("floorN rounds values down by precision")
    @Test
    void floorN() {
        assertEquals(1, Maths.floorN(2 - err, 0), 0.0, "floor should round down value just below 2 to 1");
        assertEquals(2.0, Maths.floorN(2, 0), 0.0, "floor of exact integer should return same value");
        assertEquals(1, Maths.floorN(1.5 - err, 0.3f), 0.0, "floor with 0.3 precision should round down value just below 1.5");
        assertEquals(1.5, Maths.floorN(1.5, 0.3f), 0.0, "floor with 0.3 precision should keep exact value at 1.5");
        assertEquals(1.1, Maths.floorN(1.2 - err, 1), 0.0, "floor with 1 decimal place should round down value just below 1.2");
        assertEquals(1.2, Maths.floorN(1.2, 1), 0.0, "floor with 1 decimal place should keep exact value at 1.2");
    }

    @DisplayName("round1 rounds to one decimal place")
    @Test
    void round1() {
        assertEquals(1.1, Maths.round1(1.1 + 0.4999999e-1), 0.0, "rounding to 1 decimal place should round down below 0.05");
        assertEquals(1.2, Maths.round1(1.1 + 0.5e-1), 0.0, "rounding to 1 decimal place should round up at exactly 0.05");
    }

    @DisplayName("round2 rounds to two decimal places")
    @Test
    void round2() {
        assertEquals(1.1, Maths.round2(1.1 + 0.4999999e-2), 0.0, "rounding to 2 decimal places should round down below 0.005");
        assertEquals(1.1 + 1e-2, Maths.round2(1.1 + 0.5e-2), 0.0, "rounding to 2 decimal places should round up at exactly 0.005");
    }

    @DisplayName("round3 rounds to three decimal places")
    @Test
    void round3() {
        assertEquals(1.1, Maths.round3(1.1 + 0.4999999e-3), 0.0, "rounding to 3 decimal places should round down below 0.0005");
        assertEquals(1.1 + 1e-3, Maths.round3(1.1 + 0.5e-3), 0.0, "rounding to 3 decimal places should round up at exactly 0.0005");
    }

    @DisplayName("round4 rounds to four decimal places")
    @Test
    void round4() {
        assertEquals(1.1, Maths.round4(1.1 + 0.4999999e-4), 0.0, "rounding to 4 decimal places should round down below 0.00005");
        assertEquals(1.1 + 1e-4, Maths.round4(1.1 + 0.5e-4), 0.0, "rounding to 4 decimal places should round up at exactly 0.00005");
    }

    @DisplayName("round5 rounds to five decimal places")
    @Test
    void round5() {
        assertEquals(1.1, Maths.round5(1.1 + 0.4999999e-5), 0.0, "rounding to 5 decimal places should round down below 0.000005");
        assertEquals(1.10001, Maths.round5(1.1 + 0.5e-5), 0.0, "rounding to 5 decimal places should round up at exactly 0.000005");
    }

    @DisplayName("round6 rounds to six decimal places")
    @Test
    void round6() {
        assertEquals(1.1, Maths.round6(1.1 + 0.4999999e-6), 0.0, "rounding to 6 decimal places should round down below 0.0000005");
        assertEquals(1.1 + 1e-6, Maths.round6(1.1 + 0.5e-6), 0.0, "rounding to 6 decimal places should round up at exactly 0.0000005");
    }

    @DisplayName("round7 rounds to seven decimal places")
    @Test
    void round7() {
        assertEquals(1.1, Maths.round7(1.1 + 0.4999999e-7), 0.0, "rounding to 7 decimal places should round down below 0.00000005");
        assertEquals(1.1000001, Maths.round7(1.1 + 0.5e-7), 0.0, "rounding to 7 decimal places should round up at exactly 0.00000005");
    }

    @DisplayName("round8 rounds to eight decimal places")
    @Test
    void round8() {
        assertEquals(1, Maths.round8(1), 0.0, "rounding exact integer to 8 decimal places should return same value");
        assertEquals(1.1, Maths.round8(1.1 + 0.4999999e-8), 0.0, "rounding to 8 decimal places should round down below 0.000000005");
        assertEquals(1.1 + 1e-8, Maths.round8(1.1 + 0.5e-8), 0.0, "rounding to 8 decimal places should round up at exactly 0.000000005");
        assertEquals((double) Long.MAX_VALUE, Maths.round8(Long.MAX_VALUE), 0.0, "rounding Long.MAX_VALUE to 8 decimal places should preserve value");
        assertEquals(Double.NaN, Maths.round8(Double.NaN), 0.0, "rounding NaN to 8 decimal places should return NaN");
    }

    @DisplayName("floorN with extra precision is consistent")
    @Test
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

    @DisplayName("intLog2 returns exponent for power of two inputs")
    @Test
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

    @SuppressWarnings("deprecation")
    @DisplayName("rounding helpers match BigDecimal results consistently")
    @Test
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

    @DisplayName("round4 sampled coverage keeps string length bounded")
    @Test
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

    @DisplayName("divideRoundUp rounds towards positive infinity correctly")
    @Test
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

    @DisplayName("divideRoundUp rejects zero divisor with exception")
    @Test
    void divideRoundUpZeroDivisorThrows() {
        assertThrows(ArithmeticException.class, () -> {
            long result = Maths.divideRoundUp(1, 0);
            assertEquals(0L, result, "result should not be observable when divisor is zero");
        }, "divideRoundUp should throw when divisor is zero");
    }

    @DisplayName("same treats floating point zeros and NaN")
    @Test
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

    @DisplayName("hash64 stays stable for interned strings")
    @Test
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

    @DisplayName("hash64 stays stable for string inputs")
    @Test
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

    @DisplayName("floor and ceil match BigDecimal rounding")
    @Test
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

    @DisplayName("toInt8 converts within byte range safely")
    @Test
    void testToInt8() {
        assertEquals((byte) 127, Maths.toInt8(127), "maximum byte value should convert to int8 without overflow");
        assertEquals((byte) -128, Maths.toInt8(-128), "minimum byte value should convert to int8 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(128), "toInt8 should reject values above 127");
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(-129), "toInt8 should reject values below -128");
    }

    @DisplayName("toInt16 converts within short range safely")
    @Test
    void testToInt16() {
        assertEquals((short) 32767, Maths.toInt16(32767), "maximum short value should convert to int16 without overflow");
        assertEquals((short) -32768, Maths.toInt16(-32768), "minimum short value should convert to int16 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(32768), "toInt16 should reject values above 32767");
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(-32769), "toInt16 should reject values below -32768");
    }

    @DisplayName("toInt32 converts within int range safely")
    @Test
    void testToInt32() {
        assertEquals(2147483647, Maths.toInt32(2147483647L), "maximum int value should convert to int32 without overflow");
        assertEquals(-2147483648, Maths.toInt32(-2147483648L), "minimum int value should convert to int32 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(2147483648L), "toInt32 should reject values above Integer.MAX_VALUE");
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(-2147483649L), "toInt32 should reject values below Integer.MIN_VALUE");
    }

    @DisplayName("toUInt8 converts within unsigned byte range")
    @Test
    void testToUInt8() {
        assertEquals((short) 255, Maths.toUInt8(255), "maximum unsigned byte value should convert to uint8 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(256), "toUInt8 should reject values above 255");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(-1), "toUInt8 should reject negative values");
    }

    @DisplayName("toUInt16 converts within unsigned short range")
    @Test
    void testToUInt16() {
        assertEquals(65535, Maths.toUInt16(65535), "maximum unsigned short value should convert to uint16 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(65536), "toUInt16 should reject values above 65535");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(-1), "toUInt16 should reject negative values");
    }

    @DisplayName("toUInt31 converts within 31-bit range safely")
    @Test
    void testToUInt31() {
        assertEquals(2147483647, Maths.toUInt31(2147483647L), "maximum 31-bit unsigned value should convert to uint31 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(2147483648L), "toUInt31 should reject values above 2^31-1");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(-1), "toUInt31 should reject negative values");
    }

    @DisplayName("toUInt32 converts within unsigned int range")
    @Test
    void testToUInt32() {
        assertEquals(4294967295L, Maths.toUInt32(4294967295L), "maximum unsigned int value should convert to uint32 without overflow");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(4294967296L), "toUInt32 should reject values above 2^32-1");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(-1), "toUInt32 should reject negative values");
    }

    @DisplayName("hash64 differs for distinct input values")
    @Test
    void testHash64() {
        long hashValue1 = Maths.hash64(123456789L);
        long hashValue2 = Maths.hash64(987654321L);
        assertNotEquals(hashValue1, hashValue2, "hash64 should produce different hashes for different input values");
    }

    @DisplayName("tens computes powers of ten correctly")
    @Test
    void testTens() {
        assertEquals(100, Maths.tens(2), "tens(2) should return 10^2");
        assertEquals(1, Maths.tens(0), "tens(0) should return 10^0 which is 1");
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(-1), "tens should reject negative exponent");
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(19), "tens should reject exponent above supported range");
    }

    @DisplayName("hash overloads vary with inputs properly")
    @Test
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

    @DisplayName("asDouble converts mantissa and scale correctly")
    @Test
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

    @DisplayName("nextPower2 for int values rounds up")
    @Test
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

    @DisplayName("nextPower2 for long values rounds up")
    @Test
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

    @DisplayName("nextPower2 rejects invalid int min values")
    @Test
    void testNextPower2IntInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(10, 7),
                "nextPower2 should reject non-power-of-two minimum for int");
    }

    @DisplayName("nextPower2 rejects invalid long min values")
    @Test
    void testNextPower2LongInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(20L, 9L),
                "nextPower2 should reject non-power-of-two minimum for long");
    }

    @DisplayName("isPowerOf2 identifies powers of two correctly")
    @Test
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

    @DisplayName("nextPower2 handles int edge cases correctly")
    @Test
    void testEdgeCasesInt() {
        // Test when n is negative
        assertEquals(16, Maths.nextPower2(-5, 16), "int edge case: negative value -5 should return min 16");
        // Test when min is greater than n and is the next power of two
        assertEquals(32, Maths.nextPower2(17, 32), "int edge case: value 17 below min 32 should return min");
    }

    @DisplayName("nextPower2 handles long edge cases correctly")
    @Test
    void testEdgeCasesLong() {
        // Test when n is negative
        assertEquals(64L, Maths.nextPower2(-10L, 64L), "long edge case: negative value -10 should return min 64");
        // Test when min is greater than n and is the next power of two
        assertEquals(128L, Maths.nextPower2(65L, 128L), "long edge case: value 65 below min 128 should return min");
    }

    @FunctionalInterface
    public interface Rounder {
        double round(double d);
    }
}
