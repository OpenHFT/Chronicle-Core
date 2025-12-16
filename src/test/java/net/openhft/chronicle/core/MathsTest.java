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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 10:31
 */
public class MathsTest extends CoreTestCommon {
    private static final double err = 5.1e-9;
    private static final int COUNT = Jvm.isArm() ? 500_000 : 3_000_000;
    private static final Random TEST_RANDOM = new Random(1);
    private ThreadDump threadDump;

    @Test
    public void round1scan() {
        final double factor = 1e1;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round1);
        iterations += roundUp(factor, Maths::round1up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 1));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 1));
        assertEquals(2 * COUNT, iterations, "round1scan: iterations");
    }

    @Test
    public void round2scan() {
        final double factor = 1e2;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round2);
        iterations += roundUp(factor, Maths::round2up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 2));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 2));
        assertEquals(2 * COUNT, iterations, "round2scan: iterations");
    }

    @Test
    public void round3scan() {
        final double factor = 1e3;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round3);
        iterations += roundUp(factor, Maths::round3up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 3));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 3));
        assertEquals(2 * COUNT, iterations, "round3scan: iterations");
    }

    @Test
    public void round4scan() {
        final double factor = 1e4;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round4);
        iterations += roundUp(factor, Maths::round4up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 4));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 4));
        assertEquals(2 * COUNT, iterations, "round4scan: iterations");
    }

    @Test
    public void round5scan() {
        final double factor = 1e5;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round5);
        iterations += roundUp(factor, Maths::round5up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 5));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 5));
        assertEquals(2 * COUNT, iterations, "round5scan: iterations");
    }

    @Test
    public void round6scan() {
        final double factor = 1e6;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round6);
        iterations += roundUp(factor, Maths::round6up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 6));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 6));
        assertEquals(2 * COUNT, iterations, "round6scan: iterations");
    }

    @Test
    public void round7scan() {
        final double factor = 1e7;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round7);
        iterations += roundUp(factor, Maths::round7up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 7));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 7));
        assertEquals(2 * COUNT, iterations, "round7scan: iterations");
    }

    @Test
    public void round8scan() {
        final double factor = 1e8;
        int iterations = 0;
        iterations += roundEither(factor, Maths::round8);
        iterations += roundUp(factor, Maths::round8up);
        iterations += roundEither(factor, d -> Maths.roundNup(d, 8));
        iterations += roundUp(factor, d -> Maths.roundNup(d, 8));
        assertEquals(2 * COUNT, iterations, "round8scan: iterations");
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
    public void nanTest() {
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
            assertTrue(Double.isNaN(actual), "nanTest: rounder#" + i);
        }
    }

    @Test
    public void digits() {
        assertEquals(1, Maths.digits(0), "digits: L188");
        assertEquals(1, Maths.digits(1), "digits: L189");
        assertEquals(1, Maths.digits(9), "digits: L190");
        assertEquals(2, Maths.digits(10), "digits: L191");
        assertEquals(2, Maths.digits(99), "digits: L192");
        assertEquals(3, Maths.digits(100), "digits: L193");
    }

    @Test
    public void roundN() {
        assertEquals(1.5, Maths.roundN(1 + 0.25, 0.3f), 0.0, "roundN: L198");
        assertEquals(1, Maths.roundN(1 + 0.4999999, 0), 0.0, "roundN: L199");
        assertEquals(2.0, Maths.roundN(1 + 0.5, 0), 0.0, "roundN: L200");
        assertEquals(1, Maths.roundN(1 + 0.24999999, 0.3), 0.0, "roundN: L201");
        assertEquals(1.25, Maths.roundN(1 + 0.24999999, 0.6), 0.0, "roundN: L202");
        assertEquals(1.5, Maths.roundN(1 + 0.375, 0.6), 0.0, "roundN: L203");
        assertEquals(1.5, Maths.roundN(1 + 0.624, 0.6), 0.0, "roundN: L204");
        assertEquals(1.0, Maths.roundN(1.09999999999, 0.7), 0.0, "roundN: L205");
        assertEquals(1.2, Maths.roundN(1.10000000001, 0.7), 0.0, "roundN: L206");
        assertEquals(1.2, Maths.roundN(1.29999999999, 0.7), 0.0, "roundN: L207");
        assertEquals(1.4, Maths.roundN(1.30000000001, 0.7), 0.0, "roundN: L208");
        assertEquals(1.1, Maths.roundN(1.1 + 0.4999999e-1, 1), 0.0, "roundN: L209");
        assertEquals(1.2, Maths.roundN(1.1 + 0.5e-1, 1), 0.0, "roundN: L210");

        assertEquals(1.11115, Maths.roundN(1.1111 + 0.74999999e-4, 4.3), 0.0, "roundN: L212");
        assertEquals(1.1112, Maths.roundN(1.1111 + 0.75e-4, 4.3), 0.0, "roundN: L213");
    }

    @Test
    public void ceilN() {
        assertEquals(2, Maths.ceilN(2, 0), 0.0, "ceilN: L218");
        assertEquals(2, Maths.ceilN(1 + err, 0), 0.0, "ceilN: L219");
        assertEquals(1.5, Maths.ceilN(1.5, 0.3f), 0.0, "ceilN: L220");
        assertEquals(2, Maths.ceilN(1.5 + err, 0.3f), 0.0, "ceilN: L221");
        assertEquals(1.2, Maths.ceilN(1.2, 1), 0.0, "ceilN: L222");
        assertEquals(1.2, Maths.ceilN(1.1 + err, 1), 0.0, "ceilN: L223");
    }

    @Test
    public void floorN() {
        assertEquals(1, Maths.floorN(2 - err, 0), 0.0, "floorN: L228");
        assertEquals(2.0, Maths.floorN(2, 0), 0.0, "floorN: L229");
        assertEquals(1, Maths.floorN(1.5 - err, 0.3f), 0.0, "floorN: L230");
        assertEquals(1.5, Maths.floorN(1.5, 0.3f), 0.0, "floorN: L231");
        assertEquals(1.1, Maths.floorN(1.2 - err, 1), 0.0, "floorN: L232");
        assertEquals(1.2, Maths.floorN(1.2, 1), 0.0, "floorN: L233");
    }

    @Test
    public void round1() {
        assertEquals(1.1, Maths.round1(1.1 + 0.4999999e-1), 0.0, "round1: L238");
        assertEquals(1.2, Maths.round1(1.1 + 0.5e-1), 0.0, "round1: L239");
    }

    @Test
    public void round2() {
        assertEquals(1.1, Maths.round2(1.1 + 0.4999999e-2), 0.0, "round2: L244");
        assertEquals(1.1 + 1e-2, Maths.round2(1.1 + 0.5e-2), 0.0, "round2: L245");
    }

    @Test
    public void round3() {
        assertEquals(1.1, Maths.round3(1.1 + 0.4999999e-3), 0.0, "round3: L250");
        assertEquals(1.1 + 1e-3, Maths.round3(1.1 + 0.5e-3), 0.0, "round3: L251");
    }

    @Test
    public void round4() {
        assertEquals(1.1, Maths.round4(1.1 + 0.4999999e-4), 0.0, "round4: L256");
        assertEquals(1.1 + 1e-4, Maths.round4(1.1 + 0.5e-4), 0.0, "round4: L257");
    }

    @Test
    public void round5() {
        assertEquals(1.1, Maths.round5(1.1 + 0.4999999e-5), 0.0, "round5: L262");
        assertEquals(1.10001, Maths.round5(1.1 + 0.5e-5), 0.0, "round5: L263");
    }

    @Test
    public void round6() {
        assertEquals(1.1, Maths.round6(1.1 + 0.4999999e-6), 0.0, "round6: L268");
        assertEquals(1.1 + 1e-6, Maths.round6(1.1 + 0.5e-6), 0.0, "round6: L269");
    }

    @Test
    public void round7() {
        assertEquals(1.1, Maths.round7(1.1 + 0.4999999e-7), 0.0, "round7: L274");
        assertEquals(1.1000001, Maths.round7(1.1 + 0.5e-7), 0.0, "round7: L275");
    }

    @Test
    public void round8() {
        assertEquals(1, Maths.round8(1), 0.0, "round8: L280");
        assertEquals(1.1, Maths.round8(1.1 + 0.4999999e-8), 0.0, "round8: L281");
        assertEquals(1.1 + 1e-8, Maths.round8(1.1 + 0.5e-8), 0.0, "round8: L282");
        assertEquals((double) Long.MAX_VALUE, Maths.round8(Long.MAX_VALUE), 0.0, "round8: L283");
        assertEquals(Double.NaN, Maths.round8(Double.NaN), 0.0, "round8: L284");
    }

    @Test
    public void floorNX() {
        assertEquals(1.14563, Maths.floorN(1.14563, 5), 0, "floorNX: L289");
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
    public void testIntLog2() throws IllegalArgumentException {
        for (int i = 0; i < 63; i++) {
            long l = 1L << i;
            assertEquals(i, Maths.intLog2(l), "testIntLog2: L308");
            if (i > 0)
                assertEquals(i - 1, Maths.intLog2(l - 1), "testIntLog2: L310");
        }
        assertEquals(62, Maths.intLog2(Long.MAX_VALUE), "testIntLog2: L312");

        assertThrows(IllegalArgumentException.class, () -> Maths.intLog2(0));
        for (int i = 0; i < 64; i++) {
            long l = -1L << i;
            assertThrows(IllegalArgumentException.class, () -> Maths.intLog2(l));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRounding() {
        @NotNull Random rand = TEST_RANDOM;
        for (int i = 0; i < 1000; i++) {
            double d = Math.pow(1e18, rand.nextDouble()) / 1e6;
            @NotNull BigDecimal bd = BigDecimal.valueOf(d);
            assertEquals(bd.setScale(2, RoundingMode.HALF_UP).doubleValue(), Maths.round2(d), 5e-2, "testRounding: L328");
            assertEquals(bd.setScale(4, RoundingMode.HALF_UP).doubleValue(), Maths.round4(d), 5e-4, "testRounding: L329");
            assertEquals(bd.setScale(6, RoundingMode.HALF_UP).doubleValue(), Maths.round6(d), 5e-6, "testRounding: L330");
            if (d < 1e8)
                assertEquals(bd.setScale(8, RoundingMode.HALF_UP).doubleValue(), Maths.round8(d), 5e-8, "testRounding: L332");
        }
    }

    @Test
    @Disabled("Long running")
    public void longRunningRound() {
        @NotNull double[] ds = new double[17];
        ds[0] = 1e-4;
        for (int i = 1; i < ds.length; i++)
            ds[i] = 2 * ds[i - 1];

        AtomicReference<Double> bad = new AtomicReference<>();
        DoubleStream.of(ds)
                .parallel()
                .forEach(x -> {
                    for (double d = x; d <= 2 * x && d < 10; d += Math.ulp(d)) {
                        if (Double.toString(Maths.round4(d)).length() > 6) {
                            bad.compareAndSet(null, d);
                            return;
                        }
                    }
                });
        assertNull(bad.get(), "longRunningRound: round4 string length should not exceed 6");
    }

    @Test
    public void testDivideRoundUp() {
        assertEquals(2, Maths.divideRoundUp(10, 5), "testDivideRoundUp: L360");
        assertEquals(3, Maths.divideRoundUp(11, 5), "testDivideRoundUp: L361");

        assertEquals(-2, Maths.divideRoundUp(-10, 5), "testDivideRoundUp: L363");
        assertEquals(-2, Maths.divideRoundUp(10, -5), "testDivideRoundUp: L364");
        assertEquals(2, Maths.divideRoundUp(-10, -5), "testDivideRoundUp: L365");

        assertEquals(-3, Maths.divideRoundUp(-11, 5), "testDivideRoundUp: L367");
        assertEquals(-3, Maths.divideRoundUp(11, -5), "testDivideRoundUp: L368");
        assertEquals(3, Maths.divideRoundUp(-11, -5), "testDivideRoundUp: L369");
    }

    @Test
    public void divideRoundUpZeroDivisorThrows() {
        assertThrows(ArithmeticException.class, () -> Maths.divideRoundUp(1, 0));
    }

    @Test
    public void sameFloating() {
        assertTrue(Maths.same(1.0, 1.0), "sameFloating: L379");
        assertTrue(Maths.same(1.0f, 1.0f), "sameFloating: L380");
        assertTrue(Maths.same(0.0, -0.0), "sameFloating: L381");
        assertTrue(Maths.same(0.0f, -0.0f), "sameFloating: L382");
        assertTrue(Maths.same(-0.0, 0.0), "sameFloating: L383");
        assertTrue(Maths.same(-0.0f, 0.0f), "sameFloating: L384");
        assertTrue(Maths.same(Double.NaN, Double.NaN), "sameFloating: L385");
        assertTrue(Maths.same(Float.NaN, Float.NaN), "sameFloating: L386");

        assertFalse(Maths.same(1.0, 2.0), "sameFloating: L388");
        assertFalse(Maths.same(1.0f, 2.0f), "sameFloating: L389");
        assertFalse(Maths.same(3.0, 2.0), "sameFloating: L390");
        assertFalse(Maths.same(3.0f, 2.0f), "sameFloating: L391");
        assertFalse(Maths.same(1, Double.NaN), "sameFloating: L392");
        assertFalse(Maths.same(1, Float.NaN), "sameFloating: L393");
        assertFalse(Maths.same(Double.NaN, 1), "sameFloating: L394");
        assertFalse(Maths.same(Float.NaN, 1), "sameFloating: L395");
    }

    @Test
    public void testHashStringBuilderFromInterner() {
        @NotNull StringInterner interner = new StringInterner(16);

        @NotNull final CharSequence csToHash = "557";
        @NotNull final StringBuilder sb = new StringBuilder(csToHash);

        long hash = Maths.hash64(sb);

        @Nullable String intern = interner.intern(csToHash);
        StringUtils.set(sb, intern);
        final long actual = Maths.hash64(sb);
        assertEquals(hash, actual, "testHashStringBuilderFromInterner: L410");
        // overflowing the interner?
        StringUtils.set(sb, "xxxx");

        @Nullable String intern2 = interner.intern(csToHash);
        StringUtils.set(sb, intern2);
        final long actual2 = Maths.hash64(sb);
        assertEquals(hash, actual2, "testHashStringBuilderFromInterner: L417");
    }

    @Test
    public void testHash64ForString() {
        // Empty
        String e1 = "";
        long eh1 = Maths.hash64(e1);

        assertEquals(0, eh1, "testHash64ForString: L426");

        // ASCII & Equality test
        String a1 = "Test";
        long ah1 = Maths.hash64(a1);

        String a2 = "T" + "e" + "st";
        long ah2 = Maths.hash64(a2);

        assertEquals(ah1, ah2, "testHash64ForString: L435");

        // UTF8 & Equality test
        String u1 = "\u20AC";
        long uh1 = Maths.hash64(u1);
        assertEquals(1177128352603971756L, uh1, "testHash64ForString: L440");

        String u2 = "\u20AC\u20AC".substring(0, 1);
        long uh2 = Maths.hash64(u2);

        assertEquals(uh1, uh2, "testHash64ForString: L445");

        // Mixed
        StringBuilder mixedSb = new StringBuilder().append('\u20AC');
        mixedSb.setLength(0);
        mixedSb.append('X');

        assertEquals(Maths.hash64("X"), Maths.hash64(mixedSb.toString()), "testHash64ForString: L452");

        // UT8 & Not-equal hashes
        assertNotEquals(Maths.hash64("\u0394"), Maths.hash64("\u0393"));
    }

    @Test
    public void floorNceilN() {
        double d = 64.0915946999999;
        BigDecimal bd = BigDecimal.valueOf(d);
        for (int i = 0; i < 19; i++) {
            double ceil0 = bd.setScale(i, RoundingMode.CEILING).doubleValue();
            double floor0 = bd.setScale(i, RoundingMode.FLOOR).doubleValue();
            double ceil = Maths.ceilN(d, i);
            double floor = Maths.floorN(d, i);
            assertEquals(ceil0, ceil, 0, "i: " + i);
            assertEquals(floor0, floor, 0, "i: " + i);
        }
    }

    @Test
    public void testToInt8() {
        assertEquals((byte) 127, Maths.toInt8(127), "testToInt8: L474");
        assertEquals((byte) -128, Maths.toInt8(-128), "testToInt8: L475");
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(128));
        assertThrows(ArithmeticException.class, () -> Maths.toInt8(-129));
    }

    @Test
    public void testToInt16() {
        assertEquals((short) 32767, Maths.toInt16(32767), "testToInt16: L482");
        assertEquals((short) -32768, Maths.toInt16(-32768), "testToInt16: L483");
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(32768));
        assertThrows(ArithmeticException.class, () -> Maths.toInt16(-32769));
    }

    @Test
    public void testToInt32() {
        assertEquals(2147483647, Maths.toInt32(2147483647L), "testToInt32: L490");
        assertEquals(-2147483648, Maths.toInt32(-2147483648L), "testToInt32: L491");
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(2147483648L));
        assertThrows(ArithmeticException.class, () -> Maths.toInt32(-2147483649L));
    }

    @Test
    public void testToUInt8() {
        assertEquals((short) 255, Maths.toUInt8(255), "testToUInt8: L498");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(256));
        assertThrows(ArithmeticException.class, () -> Maths.toUInt8(-1));
    }

    @Test
    public void testToUInt16() {
        assertEquals(65535, Maths.toUInt16(65535), "testToUInt16: L505");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(65536));
        assertThrows(ArithmeticException.class, () -> Maths.toUInt16(-1));
    }

    @Test
    public void testToUInt31() {
        assertEquals(2147483647, Maths.toUInt31(2147483647L), "testToUInt31: L512");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(2147483648L));
        assertThrows(ArithmeticException.class, () -> Maths.toUInt31(-1));
    }

    @Test
    public void testToUInt32() {
        assertEquals(4294967295L, Maths.toUInt32(4294967295L), "testToUInt32: L519");
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(4294967296L));
        assertThrows(ArithmeticException.class, () -> Maths.toUInt32(-1));
    }

    @Test
    public void testHash64() {
        long hashValue1 = Maths.hash64(123456789L);
        long hashValue2 = Maths.hash64(987654321L);
        assertNotEquals(hashValue1, hashValue2);
    }

    @Test
    public void testTens() {
        assertEquals(100, Maths.tens(2), "testTens: L533");
        assertEquals(1, Maths.tens(0), "testTens: L534");
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(-1));
        assertThrows(IllegalArgumentException.class, () -> Maths.tens(19));
    }

    @Test
    public void testHashMethods() {
        Object o1 = "test1";
        Object o2 = "test2";
        Object o3 = "test3";
        Object o4 = "test4";
        int hash1 = Maths.hash(o1);
        int hash2 = Maths.hash(o1, o2);
        int hash3 = Maths.hash(o1, o2, o3);
        int hash4 = Maths.hash(o1, o2, o3, o4);

        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash3, hash4);
        Object o5 = "test5";
        assertNotEquals(hash4, Maths.hash(o1, o2, o3, o4, o5));
    }

    @Test
    public void asDouble() {
        assertEquals(0.00017853, Maths.asDouble(17853, 0, false, 8), 0.0, "asDouble: L559");
        assertEquals(0.00035706, Maths.asDouble(35706, 0, false, 8), 0.0, "asDouble: L560");

        assertEquals(1.475344805371041E-8, Maths.asDouble(1475344805371041L, 0, false, 23), 0.0, "asDouble: L562");
        assertEquals(1.000000000000003E12, Maths.asDouble(1000000000000003L, 0, false, 3), 0.0, "asDouble: L563");
        assertEquals(-1.453448689138e11, Maths.asDouble(1453448689138L, 0, true, 1), 0.0, "asDouble: L564");
        assertEquals(999999999999.994, Maths.asDouble(999999999999994L, 0, false, 3), 0.0, "asDouble: L565");
        assertEquals(-1.16823E70, Maths.asDouble(116823, 0, true, -65), 0.0, "asDouble: L566");
        assertEquals(12.345, Maths.asDouble(12345, 0, false, 3), 0.0, "asDouble: L567");
        assertEquals(1e-5, Maths.asDouble(100000000000L, 0, false, 16), 0.0, "asDouble: L568");
        assertEquals(1.4753448053710411E-8, Maths.asDouble(14753448053710411L, 0, false, 24), 0.0, "asDouble: L569");
        assertEquals(1.720578937592997e-8, Maths.asDouble(1720578937592997L, 0, false, 23), 0.0, "asDouble: L570");
        //
        assertEquals(-12.345, Maths.asDouble(12345, 0, true, 3), 0.0, "asDouble: L572");
        assertEquals(98760.0, Maths.asDouble(12345, 3, false, 0), 0.0, "asDouble: L573");
        assertEquals(1543.125, Maths.asDouble(12345, -3, false, 0), 0.0, "asDouble: L574");
        assertEquals(1.2345E-26, Maths.asDouble(12345, 0, false, 30), 0.0, "asDouble: L575");
        assertEquals(123450000000000000L, Maths.asDouble(1234500000000000000L, 0, false, 1), 0.0, "asDouble: L576");
        assertEquals(1234500, Maths.asDouble(12345, 0, false, -2), 0.0, "asDouble: L577");
        assertEquals(1.23E30, Maths.asDouble(123, 0, false, -28), 0.0, "asDouble: L578");
    }

    @Test
    public void testNextPower2Int() {
        // Test cases where n is less than min
        assertEquals(8, Maths.nextPower2(3, 8), "testNextPower2Int: L584");
        assertEquals(16, Maths.nextPower2(5, 16), "testNextPower2Int: L585");

        // Test cases where n is equal to min
        assertEquals(8, Maths.nextPower2(8, 8), "testNextPower2Int: L588");
        assertEquals(16, Maths.nextPower2(16, 16), "testNextPower2Int: L589");

        // Test cases where n is already a power of two
        assertEquals(32, Maths.nextPower2(32, 16), "testNextPower2Int: L592");
        assertEquals(64, Maths.nextPower2(64, 32), "testNextPower2Int: L593");

        // Test cases where n is not a power of two
        assertEquals(128, Maths.nextPower2(70, 16), "testNextPower2Int: L596");
        assertEquals(256, Maths.nextPower2(130, 64), "testNextPower2Int: L597");

        // Test maximum int value
        assertEquals(1 << 30, Maths.nextPower2(Integer.MAX_VALUE, 1), "testNextPower2Int: L600");

        // Test minimum n and min
        assertEquals(1, Maths.nextPower2(0, 1), "testNextPower2Int: L603");
        assertEquals(1, Maths.nextPower2(1, 1), "testNextPower2Int: L604");
    }

    @Test
    public void testNextPower2Long() {
        // Test cases where n is less than min
        assertEquals(16L, Maths.nextPower2(9L, 16L), "testNextPower2Long: L610");
        assertEquals(32L, Maths.nextPower2(17L, 32L), "testNextPower2Long: L611");

        // Test cases where n is equal to min
        assertEquals(64L, Maths.nextPower2(64L, 64L), "testNextPower2Long: L614");
        assertEquals(128L, Maths.nextPower2(128L, 128L), "testNextPower2Long: L615");

        // Test cases where n is already a power of two
        assertEquals(256L, Maths.nextPower2(256L, 128L), "testNextPower2Long: L618");
        assertEquals(512L, Maths.nextPower2(512L, 256L), "testNextPower2Long: L619");

        // Test cases where n is not a power of two
        assertEquals(1024L, Maths.nextPower2(777L, 256L), "testNextPower2Long: L622");
        assertEquals(2048L, Maths.nextPower2(1300L, 1024L), "testNextPower2Long: L623");

        // Test large values
        assertEquals(1L << 62, Maths.nextPower2(Long.MAX_VALUE, 1L), "testNextPower2Long: L626");

        // Test minimum n and min
        assertEquals(1L, Maths.nextPower2(0L, 1L), "testNextPower2Long: L629");
        assertEquals(1L, Maths.nextPower2(1L, 1L), "testNextPower2Long: L630");
    }

    @Test
    public void testNextPower2IntInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(10, 7));
    }

    @Test
    public void testNextPower2LongInvalidMin() {
        // min is not a power of two
        assertThrows(IllegalArgumentException.class, () -> Maths.nextPower2(20L, 9L));
    }

    @Test
    public void testIsPowerOf2() {
        assertTrue(Maths.isPowerOf2(1), "testIsPowerOf2: L647");
        assertTrue(Maths.isPowerOf2(2), "testIsPowerOf2: L648");
        assertTrue(Maths.isPowerOf2(4), "testIsPowerOf2: L649");
        assertTrue(Maths.isPowerOf2(8), "testIsPowerOf2: L650");
        assertTrue(Maths.isPowerOf2(16), "testIsPowerOf2: L651");
        assertTrue(Maths.isPowerOf2(32), "testIsPowerOf2: L652");
        assertTrue(Maths.isPowerOf2(64), "testIsPowerOf2: L653");
        assertTrue(Maths.isPowerOf2(128), "testIsPowerOf2: L654");
        assertTrue(Maths.isPowerOf2(256), "testIsPowerOf2: L655");
        assertTrue(Maths.isPowerOf2(512), "testIsPowerOf2: L656");
        assertTrue(Maths.isPowerOf2(1024), "testIsPowerOf2: L657");

        assertFalse(Maths.isPowerOf2(0), "testIsPowerOf2: L659");
        assertFalse(Maths.isPowerOf2(3), "testIsPowerOf2: L660");
        assertFalse(Maths.isPowerOf2(5), "testIsPowerOf2: L661");
        assertFalse(Maths.isPowerOf2(6), "testIsPowerOf2: L662");
        assertFalse(Maths.isPowerOf2(7), "testIsPowerOf2: L663");
        assertFalse(Maths.isPowerOf2(9), "testIsPowerOf2: L664");
        assertFalse(Maths.isPowerOf2(10), "testIsPowerOf2: L665");
        assertFalse(Maths.isPowerOf2(12), "testIsPowerOf2: L666");
        assertFalse(Maths.isPowerOf2(15), "testIsPowerOf2: L667");
        assertFalse(Maths.isPowerOf2(18), "testIsPowerOf2: L668");
        assertFalse(Maths.isPowerOf2(20), "testIsPowerOf2: L669");
    }

    @Test
    public void testEdgeCasesInt() {
        // Test when n is negative
        assertEquals(16, Maths.nextPower2(-5, 16), "testEdgeCasesInt: L675");
        // Test when min is greater than n and is the next power of two
        assertEquals(32, Maths.nextPower2(17, 32), "testEdgeCasesInt: L677");
    }

    @Test
    public void testEdgeCasesLong() {
        // Test when n is negative
        assertEquals(64L, Maths.nextPower2(-10L, 64L), "testEdgeCasesLong: L683");
        // Test when min is greater than n and is the next power of two
        assertEquals(128L, Maths.nextPower2(65L, 128L), "testEdgeCasesLong: L685");
    }

    @FunctionalInterface
    public interface Rounder {
        double round(double d);
    }
}
