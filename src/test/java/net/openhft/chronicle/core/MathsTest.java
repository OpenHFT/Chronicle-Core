/*
 * Copyright 2016 higherfrequencytrading.com
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

import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.DoubleStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 10:31
 */
public class MathsTest {

    private ThreadDump threadDump;

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    public void testIntLog2() {
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
        Random rand = new Random(1);
        for (int i = 0; i < 1000; i++) {
            double d = Math.pow(1e18, rand.nextDouble()) / 1e6;
            BigDecimal bd = new BigDecimal(d);
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
        double[] ds = new double[17];
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
    @Ignore("Long running, avg score = 6879")
    public void testRandomness() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            StringBuilder sb = new StringBuilder();
            byte[] init = new byte[hashs.length / 64];
            new SecureRandom().nextBytes(init);
            for (int i = 0; i < hashs.length; i++) {
                sb.setLength(0);
                sb.append(t).append('-').append(i);
                long start = System.nanoTime();
                hashs[i] = Maths.hash64(sb);
                time += System.nanoTime() - start;
                timeCount++;
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC < 18) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
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
}
