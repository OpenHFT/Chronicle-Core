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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.pool.StringInterner;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 10:31
 */
public class MathsTest {
    static final double err = 5.1e-9;
    public static final int COUNT = Jvm.isArm() ? 2_000_000 : 10_000_000;
    private ThreadDump threadDump;

    @Test
    public void round1scan() {
        final double factor = 1e1;
        roundEither(factor, Maths::round1);
        roundUp(factor, Maths::round1up);
        roundEither(factor, d -> Maths.roundNup(d, 1));
        roundUp(factor, d -> Maths.roundNup(d, 1));
    }

    @Test
    public void round2scan() {
        final double factor = 1e2;
        roundEither(factor, Maths::round2);
        roundUp(factor, Maths::round2up);
        roundEither(factor, d -> Maths.roundNup(d, 2));
        roundUp(factor, d -> Maths.roundNup(d, 2));
    }

    @Test
    public void round3scan() {
        final double factor = 1e3;
        roundEither(factor, Maths::round3);
        roundUp(factor, Maths::round3up);
        roundEither(factor, d -> Maths.roundNup(d, 3));
        roundUp(factor, d -> Maths.roundNup(d, 3));
    }

    @Test
    public void round4scan() {
        final double factor = 1e4;
        roundEither(factor, Maths::round4);
        roundUp(factor, Maths::round4up);
        roundEither(factor, d -> Maths.roundNup(d, 4));
        roundUp(factor, d -> Maths.roundNup(d, 4));
    }

    @Test
    public void round5scan() {
        final double factor = 1e5;
        roundEither(factor, Maths::round5);
        roundUp(factor, Maths::round5up);
        roundEither(factor, d -> Maths.roundNup(d, 5));
        roundUp(factor, d -> Maths.roundNup(d, 5));
    }

    @Test
    public void round6scan() {
        final double factor = 1e6;
        roundEither(factor, Maths::round6);
        roundUp(factor, Maths::round6up);
        roundEither(factor, d -> Maths.roundNup(d, 6));
        roundUp(factor, d -> Maths.roundNup(d, 6));
    }

    @Test
    public void round7scan() {
        final double factor = 1e7;
        roundEither(factor, Maths::round7);
        roundUp(factor, Maths::round7up);
        roundEither(factor, d -> Maths.roundNup(d, 7));
        roundUp(factor, d -> Maths.roundNup(d, 7));
    }

    @Test
    public void round8scan() {
        final double factor = 1e8;
        roundEither(factor, Maths::round8);
        roundUp(factor, Maths::round8up);
        roundEither(factor, d -> Maths.roundNup(d, 8));
        roundUp(factor, d -> Maths.roundNup(d, 8));
    }

    public void roundEither(double factor, Rounder rounder) {
        final double factor2 = 2 * factor;
        for (int i = 1; i < COUNT; i += 2) {
            double dm = i / factor2;
            final double ulp = Math.ulp(dm);
            double d = dm + ulp;
            double d0 = dm - ulp * 2;

            double e = (i + 1) / factor2;
            double e0 = (i - 1) / factor2;
            final String msg = "i: " + i;
            assertEquals(msg, e, rounder.round(d), 0);
            assertEquals(msg, e0, rounder.round(d0), 0);
        }
    }

    public void roundUp(double factor, Rounder rounder) {
        final double factor2 = 2 * factor;
        for (int i = 1; i < COUNT; i += 2) {
            double d = i / factor2;
            double d0 = d - Math.ulp(d) * 2;

            double e = (i + 1) / factor2;
            double e0 = (i - 1) / factor2;
            final String msg = "i: " + i;
            assertEquals(msg, e, rounder.round(d), 0);
            assertEquals(msg, e0, rounder.round(d0), 0);
        }
    }

    @Test
    public void nanTest() {
        Stream.<Rounder>of(
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
                )
                .mapToDouble(rounder -> rounder.round(Double.NaN))
                .forEach(d -> assertTrue(Double.isNaN(d)));
    }

    @FunctionalInterface
    public interface Rounder {
        double round(double d);
    }

    @Test
    public void digits() {
        assertEquals(1, Maths.digits(0));
        assertEquals(1, Maths.digits(1));
        assertEquals(1, Maths.digits(9));
        assertEquals(2, Maths.digits(10));
        assertEquals(2, Maths.digits(99));
        assertEquals(3, Maths.digits(100));
    }

    @Test
    public void roundN() {
        assertEquals(1.5, Maths.roundN(1 + 0.25, 0.3f), 0.0);
        assertEquals(1, Maths.roundN(1 + 0.4999999, 0), 0.0);
        assertEquals(2.0, Maths.roundN(1 + 0.5, 0), 0.0);
        assertEquals(1, Maths.roundN(1 + 0.24999999, 0.3), 0.0);
        assertEquals(1.25, Maths.roundN(1 + 0.24999999, 0.6), 0.0);
        assertEquals(1.5, Maths.roundN(1 + 0.375, 0.6), 0.0);
        assertEquals(1.5, Maths.roundN(1 + 0.624, 0.6), 0.0);
        assertEquals(1.0, Maths.roundN(1.09999999999, 0.7), 0.0);
        assertEquals(1.2, Maths.roundN(1.10000000001, 0.7), 0.0);
        assertEquals(1.2, Maths.roundN(1.29999999999, 0.7), 0.0);
        assertEquals(1.4, Maths.roundN(1.30000000001, 0.7), 0.0);
        assertEquals(1.1, Maths.roundN(1.1 + 0.4999999e-1, 1), 0.0);
        assertEquals(1.2, Maths.roundN(1.1 + 0.5e-1, 1), 0.0);

        assertEquals(1.11115, Maths.roundN(1.1111 + 0.74999999e-4, 4.3), 0.0);
        assertEquals(1.1112, Maths.roundN(1.1111 + 0.75e-4, 4.3), 0.0);
    }

    @Test
    public void ceilN() throws Exception {
        assertEquals(2, Maths.ceilN(2, 0), 0.0);
        assertEquals(2, Maths.ceilN(1 + err, 0), 0.0);
        assertEquals(1.5, Maths.ceilN(1.5, 0.3f), 0.0);
        assertEquals(2, Maths.ceilN(1.5 + err, 0.3f), 0.0);
        assertEquals(1.2, Maths.ceilN(1.2, 1), 0.0);
        assertEquals(1.2, Maths.ceilN(1.1 + err, 1), 0.0);
    }

    @Test
    public void floorN() throws Exception {
        assertEquals(1, Maths.floorN(2 - err, 0), 0.0);
        assertEquals(2.0, Maths.floorN(2, 0), 0.0);
        assertEquals(1, Maths.floorN(1.5 - err, 0.3f), 0.0);
        assertEquals(1.5, Maths.floorN(1.5, 0.3f), 0.0);
        assertEquals(1.1, Maths.floorN(1.2 - err, 1), 0.0);
        assertEquals(1.2, Maths.floorN(1.2, 1), 0.0);
    }

    @Test
    public void round1() throws Exception {
        assertEquals(1.1, Maths.round1(1.1 + 0.4999999e-1), 0.0);
        assertEquals(1.2, Maths.round1(1.1 + 0.5e-1), 0.0);
    }

    @Test
    public void round2() throws Exception {
        assertEquals(1.1, Maths.round2(1.1 + 0.4999999e-2), 0.0);
        assertEquals(1.1 + 1e-2, Maths.round2(1.1 + 0.5e-2), 0.0);
    }

    @Test
    public void round3() throws Exception {
        assertEquals(1.1, Maths.round3(1.1 + 0.4999999e-3), 0.0);
        assertEquals(1.1 + 1e-3, Maths.round3(1.1 + 0.5e-3), 0.0);
    }

    @Test
    public void round4() throws Exception {
        assertEquals(1.1, Maths.round4(1.1 + 0.4999999e-4), 0.0);
        assertEquals(1.1 + 1e-4, Maths.round4(1.1 + 0.5e-4), 0.0);
    }

    @Test
    public void round5() throws Exception {
        assertEquals(1.1, Maths.round5(1.1 + 0.4999999e-5), 0.0);
        assertEquals(1.10001, Maths.round5(1.1 + 0.5e-5), 0.0);
    }

    @Test
    public void round6() throws Exception {
        assertEquals(1.1, Maths.round6(1.1 + 0.4999999e-6), 0.0);
        assertEquals(1.1 + 1e-6, Maths.round6(1.1 + 0.5e-6), 0.0);
    }

    @Test
    public void round7() throws Exception {
        assertEquals(1.1, Maths.round7(1.1 + 0.4999999e-7), 0.0);
        assertEquals(1.1000001, Maths.round7(1.1 + 0.5e-7), 0.0);
    }

    @Test
    public void round8() throws Exception {
        assertEquals(1, Maths.round8(1), 0.0);
        assertEquals(1.1, Maths.round8(1.1 + 0.4999999e-8), 0.0);
        assertEquals(1.1 + 1e-8, Maths.round8(1.1 + 0.5e-8), 0.0);
        assertEquals((double) Long.MAX_VALUE, Maths.round8(Long.MAX_VALUE), 0.0);
        assertEquals(Double.NaN, Maths.round8(Double.NaN), 0.0);
    }

    @Test
    public void floorNX() {
        assertEquals(1.14563, Maths.floorN(1.14563, 5), 0);
    }

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    public void testIntLog2() throws IllegalArgumentException {
        for (int i = 0; i < 63; i++) {
            long l = 1L << i;
            assertEquals(i, Maths.intLog2(l));
            if (i > 0)
                assertEquals(i - 1, Maths.intLog2(l - 1));
        }
        assertEquals(62, Maths.intLog2(Long.MAX_VALUE));

        try {
            assertEquals(0, Maths.intLog2(0));
            throw new AssertionError("expected IllegalArgumentException Math.intLong2(0)");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        for (int i = 0; i < 64; i++) {
            try {
                long l = -1L << i;
                Maths.intLog2(l);
                throw new AssertionError("expected IllegalArgumentException Math.intLong2 " + l);
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }

    @Test
    public void testRounding() {
        @NotNull Random rand = new Random(1);
        for (int i = 0; i < 1000; i++) {
            double d = Math.pow(1e18, rand.nextDouble()) / 1e6;
            @NotNull BigDecimal bd = new BigDecimal(d);
            assertEquals(bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round2(d), 5e-2);
            assertEquals(bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round4(d), 5e-4);
            assertEquals(bd.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round6(d), 5e-6);
            if (d < 1e8)
                assertEquals(bd.setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue(), Maths.round8(d), 5e-8);
        }
    }

    @Test
    @Ignore("Long running")
    public void longRunningRound() {
        @NotNull double[] ds = new double[17];
        ds[0] = 1e-4;
        for (int i = 1; i < ds.length; i++)
            ds[i] = 2 * ds[i - 1];

        DoubleStream.of(ds).parallel()
                .forEach(x -> {
                    for (double d = x; d <= 2 * x && d < 10; d += Math.ulp(d))
                        if (Double.toString(Maths.round4(d)).length() > 6)
                            fail("d: " + d);
                });
    }

    @Test
    public void testDivideRoundUp() {
        assertEquals(2, Maths.divideRoundUp(10, 5));
        assertEquals(3, Maths.divideRoundUp(11, 5));

        assertEquals(-2, Maths.divideRoundUp(-10, 5));
        assertEquals(-2, Maths.divideRoundUp(10, -5));
        assertEquals(2, Maths.divideRoundUp(-10, -5));

        assertEquals(-3, Maths.divideRoundUp(-11, 5));
        assertEquals(-3, Maths.divideRoundUp(11, -5));
        assertEquals(3, Maths.divideRoundUp(-11, -5));
    }

    @Test
    public void sameFloating() {
        assertTrue(Maths.same(1.0, 1.0));
        assertTrue(Maths.same(1.0f, 1.0f));
        assertTrue(Maths.same(0.0, -0.0));
        assertTrue(Maths.same(0.0f, -0.0f));
        assertTrue(Maths.same(-0.0, 0.0));
        assertTrue(Maths.same(-0.0f, 0.0f));
        assertTrue(Maths.same(Double.NaN, Double.NaN));
        assertTrue(Maths.same(Float.NaN, Float.NaN));

        assertFalse(Maths.same(1.0, 2.0));
        assertFalse(Maths.same(1.0f, 2.0f));
        assertFalse(Maths.same(3.0, 2.0));
        assertFalse(Maths.same(3.0f, 2.0f));
        assertFalse(Maths.same(1, Double.NaN));
        assertFalse(Maths.same(1, Float.NaN));
        assertFalse(Maths.same(Double.NaN, 1));
        assertFalse(Maths.same(Float.NaN, 1));
    }

    @Test
    public void testHashStringBuilderFromInterner() throws Exception {
        @NotNull StringInterner interner = new StringInterner(16);

        @NotNull final CharSequence csToHash = "557";
        @NotNull final StringBuilder sb = new StringBuilder(csToHash);

        long hash = Maths.hash64(sb);

        @Nullable String intern = interner.intern(csToHash);
        StringUtils.set(sb, intern);
        final long actual = Maths.hash64(sb);
        assertEquals(hash, actual);
        // overflowing the interner?
        StringUtils.set(sb, "xxxx");

        @Nullable String intern2 = interner.intern(csToHash);
        StringUtils.set(sb, intern2);
        final long actual2 = Maths.hash64(sb);
        assertEquals(hash, actual2);
    }

    @Test
    public void testHash64ForString() throws Exception {
        // Empty
        String e1 = "";
        long eh1 = Maths.hash64(e1);

        assertEquals(0, eh1);

        // ASCII & Equality test
        String a1 = "Test";
        long ah1 = Maths.hash64(a1);

        String a2 = new StringBuilder().append("T").append("e").toString() + "st";
        long ah2 = Maths.hash64(a2);

        assertEquals(ah1, ah2);

        // UTF8 & Equality test
        String u1 = "€";
        long uh1 = Maths.hash64(u1);
        if (Jvm.isJava9Plus()) {
            assertEquals(-11958288497246124L, uh1);
        } else {
            assertEquals(1177128352603971756L, uh1);
        }

        String u2 = "€€".substring(0, 1);
        long uh2 = Maths.hash64(u2);

        assertEquals(uh1, uh2);

        // Mixed
        StringBuilder mixedSb = new StringBuilder().append("€");
        mixedSb.setLength(0);
        mixedSb.append("X");

        assertEquals(Maths.hash64("X"), Maths.hash64(mixedSb.toString()));

        // UT8 & Not-equal hashes
        assertNotEquals(Maths.hash64("Δ"), Maths.hash64("Γ"));
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
            assertEquals("i: " + i, ceil0, ceil, 0);
            assertEquals("i: " + i, floor0, floor, 0);
        }
    }

    @Test
    public void asDouble() {
        assertEquals(12.345, Maths.asDouble(12345, 0, false, 3), 0.00000000000001);
        assertEquals(-12.345, Maths.asDouble(12345, 0, true, 3), 0.00000000000001);
        assertEquals(98760.0, Maths.asDouble(12345, 3, false, 0), 0.00000000000001);
        assertEquals(1543.125, Maths.asDouble(12345, -3, false, 0), 0.0000000000001);
        assertEquals(1.2345E-26, Maths.asDouble(12345, 0, false, 30), 0.0000000000001);
        assertEquals(123450000000000000L, Maths.asDouble(1234500000000000000L, 0, false, 1), 0.0000000000001);
        assertEquals(1234500, Maths.asDouble(12345, 0, false, -2), 0.0000000000001);
        assertEquals(1.23E30, Maths.asDouble(123, 0, false, -28), 0.0000000000001);
    }
}
