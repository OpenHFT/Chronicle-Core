/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.cooler.CoolerTester;
import net.openhft.chronicle.core.cooler.CpuCoolers;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UnsafeTextTest extends CoreTestCommon {

    static long blackhole;

    @Test
    public void coolerAppendBase10quick() {
        long address = UNSAFE.allocateMemory(32);

        try {

            new CoolerTester(CpuCoolers.PAUSE1, CpuCoolers.BUSY1)
//                .add("noop", () -> null)
                    .add("20d", () -> {
                        blackhole = UnsafeText.appendFixed(address, -Integer.MAX_VALUE);
                        return null;
                    })
                    .runTimeMS(100)
                    .repeat(3)
                    .run();

            final String memVal = LongStream.range(address, blackhole)
                    .mapToInt(addr -> UNSAFE.getByte(null, addr))
                    .mapToObj(c -> (char) c)
                    .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                    .toString();

            assertEquals(Long.toString(-Integer.MAX_VALUE), memVal);
        } finally {
            UNSAFE.freeMemory(address);
        }

    }

    @Test
    public void testAppendDouble() {
        // TODO FIX
        // Examples for https://github.com/OpenHFT/Chronicle-Core/issues/493
        testAppendDoubleOnce(5.959231521092378E-8, "5.959231521092378E-8");
        testAppendDoubleOnce(5.954710747053357E-8, "5.954710747053357E-8");
        testAppendDoubleOnce(-4.3723721608241563E-8, "-4.3723721608241563E-8");
        testAppendDoubleOnce(3.5645738448792343E-8, "3.5645738448792343E-8");
        testAppendDoubleOnce(1.1914579369762811E-8, "1.1914579369762811E-8");

        // FIXED
        testAppendDoubleOnce(-1.4778838950354771E-9, "-1.4778838950354771E-9");
        testAppendDoubleOnce(-145344868913.80003, "-145344868913.80003");

        testAppendDoubleOnce(1.4753448053710411E-8, "1.4753448053710411E-8");
        testAppendDoubleOnce(4.731428525883379E-10, "4.731428525883379E-10");
        testAppendDoubleOnce(1e-5, "0.00001");

        testAppendDoubleOnce(5.7270847085938394E-9, "5.7270847085938394E-9");
        testAppendDoubleOnce(-3.5627763205104632E-9, "-3.5627763205104632E-9");
        testAppendDoubleOnce(3.4363211797092447E-10, "3.4363211797092447E-10");

        testAppendDoubleOnce(0.91234567890123456789, "0.9123456789012345");
        testAppendDoubleOnce(0.7205789375929972, "0.7205789375929972");
        testAppendDoubleOnce(1.7205789375929972E-8, "1.7205789375929972E-8");
        testAppendDoubleOnce(1.000000459754255, "1.000000459754255");
//        testAppendDoubleOnce(1.0000004597542552, "1.0000004597542552");
        testAppendDoubleOnce(-0.0042633243189823394, "-0.0042633243189823394");
        // too high
        testAppendDoubleOnce(4.3634067645459027E-4, "0.00043634067645459027");
        testAppendDoubleOnce(-4.8378951079402273E-4, "-0.00048378951079402273");
        testAppendDoubleOnce(3.8098893793449994E-4, "0.00038098893793449994");
        testAppendDoubleOnce(-0.0036980489197619678, "-0.0036980489197619678");

        // FIXED
        testAppendDoubleOnce(1.1777536373898703E-7, "0.00000011777536373898703");
        testAppendDoubleOnce(8.577881719106565E-8, "0.00000008577881719106565");
        testAppendDoubleOnce(1.1709707236415293E-7, "0.00000011709707236415293");
        testAppendDoubleOnce(1.0272238286878982E-7, "0.00000010272238286878982");
        testAppendDoubleOnce(9.077547054210796E-8, "0.00000009077547054210796");
        testAppendDoubleOnce(-1.1914407211387385E-7, "-0.00000011914407211387385");
//        testAppendDoubleOnce(1.0626477603237785E-10, "0.00000000010626477603237785");
        testAppendDoubleOnce(8.871684275243539E-4, "0.0008871684275243539");
        testAppendDoubleOnce(8.807878708605213E-4, "0.0008807878708605213");
        testAppendDoubleOnce(8.417670165790972E-4, "0.0008417670165790972");
        testAppendDoubleOnce(0.0013292726996348332, "0.0013292726996348332");
        testAppendDoubleOnce(2.4192540417349368E-4, "0.00024192540417349368");
        testAppendDoubleOnce(1.9283711356548258E-4, "0.00019283711356548258");
        testAppendDoubleOnce(-8.299137873077923E-5, "-0.00008299137873077923");

        // OK
        testAppendDoubleOnce(0, "0.0");
        testAppendDoubleOnce(0.001, "0.001");
        testAppendDoubleOnce(0.0001, "0.0001");
        testAppendDoubleOnce(0.000001, "0.000001");
        testAppendDoubleOnce(0.0000001, "0.0000001");
        testAppendDoubleOnce(1.0E-8, "1.0E-8");
        testAppendDoubleOnce(1.0E-9, "1.0E-9");
        testAppendDoubleOnce(0.009, "0.009");
        testAppendDoubleOnce(0.0009, "0.0009");
        testAppendDoubleOnce(0.00009, "0.00009");
        testAppendDoubleOnce(0.000009, "0.000009");
        testAppendDoubleOnce(0.0000009, "0.0000009");
        testAppendDoubleOnce(0.00000009, "0.00000009");
        testAppendDoubleOnce(9.0E-9, "9.0E-9");
        testAppendDoubleOnce(Double.NaN, "NaN");
        testAppendDoubleOnce(Double.POSITIVE_INFINITY, "Infinity");
        testAppendDoubleOnce(Double.NEGATIVE_INFINITY, "-Infinity");
        testAppendDoubleOnce(0.1, "0.1");
        testAppendDoubleOnce(12.0, "12.0");
        testAppendDoubleOnce(12.1, "12.1");
        testAppendDoubleOnce(12.00000001, "12.00000001");
        testAppendDoubleOnce(1e-9 + Math.ulp(1e-9), "1.0000000000000003E-9");
        testAppendDoubleOnce(1e-10 + Math.ulp(1e-10), "1.0000000000000002E-10");
        testAppendDoubleOnce(1e-11 + Math.ulp(1e-11), "1.0000000000000001E-11");
        double d = -1e30;
        testAppendDoubleOnce(d - Math.ulp(d), "-1000000000000000158000000000000");
        d = -1e31;
        testAppendDoubleOnce(d - Math.ulp(d), "-1.0000000000000001E31");
        d = -1e32;
        testAppendDoubleOnce(d - Math.ulp(d), "-1.0000000000000002E32");

    }

    public void testAppendDoubleOnce(double value, String expectedValue) {
        long address = UNSAFE.allocateMemory(max + 8);
        try {
            final String memVal = appendDoubleToString(value, address);
            assertEquals("value; " + value, expectedValue, memVal);
        } finally {
            UNSAFE.freeMemory(address);
        }
    }

    static final int max = 32;

    @Test
    public void testRandom() {
        int runLength = 100_000;
        IntStream.range(0, runLength).parallel().forEach(t -> {
            Random r = new Random();
            long address = UNSAFE.allocateMemory(max + 8);
            long l = r.nextLong() | 1L;
            for (int i = 0; i < 1_000; i++) {
                // agitate
                l += 2;
                double d = Double.longBitsToDouble(l);
                if (!Double.isFinite(d) || Math.abs(d) < 1e-20 || Math.abs(d) > 5e11) {
                    break;
                }
                String s = appendDoubleToString(d, address);
                double d2 = Double.parseDouble(s);
                if (d != d2) {
                    String message = "" + (d - d2);
                    assertEquals(message, d, d2, 0);
                }
            }
            // this is called unless the test is about to die
            UNSAFE.freeMemory(address);
        });
    }

    @Test
    public void testSequential() {
        IntStream.range(0, 300).parallel().forEach(t -> {
            // odd numbers have the most precision error
            long address = UNSAFE.allocateMemory(max + 8);
            // double only has 16-17 digits of accuracy so 1 + x/1e15 has 16 digits.
            double n = 1e15;
            for (int i = 1; i < 10_000; i += 2) {
                // multiply by 10191 to traverse the space.
                long l = (t * 10_000L + i) * 10191L + (long) n;
                double d = l / n;
                String s = appendDoubleToString(d, address);
                double d2 = Double.parseDouble(s);
                if (d != d2)
                    assertEquals("" + (d - d2), d, d2, 0);
            }
            // this is called unless the test is about to die
            UNSAFE.freeMemory(address);
        });
    }

    public String appendDoubleToString(double value, long address) {
        UNSAFE.putLong(address + max, 0L);
        final long endAddress = UnsafeText.appendDouble(address, value);
        if (endAddress > address + max)
            fail("value: " + value + " length: " + (endAddress - address));
        long end = UNSAFE.getLong(address + max);
        if (end != 0L)
            fail("Overwrite: " + Long.toHexString(end));
        return LongStream.range(address, endAddress)
                .mapToInt(addr -> UNSAFE.getByte(null, addr))
                .mapToObj(c -> (char) c)
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();
    }


}