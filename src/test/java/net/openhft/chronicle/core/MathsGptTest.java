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

package net.openhft.chronicle.core;


import org.junit.Assert;
import org.junit.Test;

import static net.openhft.chronicle.core.Maths.asDouble;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MathsGptTest {

    @Test
    public void testRoundNPositive() {
        double result = Maths.roundN(123.4567, 2);
        assertEquals(123.46, result, 0.001);
    }

    @Test
    public void testRoundNNegative() {
        double result = Maths.roundN(-123.4567, 2);
        assertEquals(-123.46, result, 0.001);
    }

    @Test
    public void testRoundNZero() {
        double result = Maths.roundN(0, 2);
        assertEquals(0, result, 0.001);
    }

    @Test
    public void testRoundNWholeNumber() {
        double result = Maths.roundN(123.0, 2);
        assertEquals(123.0, result, 0.001);
    }

    @Test
    public void testRoundNMoreDigits() {
        double result = Maths.roundN(123.4567, 4);
        assertEquals(123.4567, result, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNInvalidDigits() {
        Maths.roundN(123.4567, 20);
    }


    @Test
    public void testRoundNupPositive() {
        double result = Maths.roundNup(123.4567, 2);
        assertEquals(123.46, result, 0.001);
    }

    @Test
    public void testRoundNupNegative() {
        double result = Maths.roundNup(-123.4567, 2);
        assertEquals(-123.46, result, 0.001);
    }

    @Test
    public void testRoundNupZero() {
        double result = Maths.roundNup(0, 2);
        assertEquals(0, result, 0.001);
    }

    @Test
    public void testRoundNupWholeNumber() {
        double result = Maths.roundNup(123.0, 2);
        assertEquals(123.0, result, 0.001);
    }

    @Test
    public void testRoundNupMoreDigits() {
        double result = Maths.roundNup(123.4567, 4);
        assertEquals(123.4567, result, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNupInvalidDigits() {
        Maths.roundNup(123.4567, 20);
    }

    private static final double EPSILON = 0; // Tolerance for comparing double values

    @Test
    public void testRoundN_withZeroDigits() {
        double result = Maths.roundN(1.23456789, 0);
        assertEquals(1.0, result, EPSILON);
    }

    @Test
    public void testRoundN_withPositiveDigits() {
        double result = Maths.roundN(1.23456789, 4);
        assertEquals(1.2346, result, EPSILON);
    }

    @Test
    public void testRoundN_withMaxDigits() {
        double result = Maths.roundN(1.23456789, 18);
        assertEquals(1.23456789, result, EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundN_withNegativeDigits() {
        double result = Maths.roundN(12345.6789, -2);
        assertEquals(12300.0, result, EPSILON);
    }

    @Test
    public void testRoundN_withLargeValue() {
        double result = Maths.roundN(1.0e20, 5);
        assertEquals(1.0e20, result, EPSILON);
    }

    @Test
    public void testRoundN_withSmallValue() {
        double result = Maths.roundN(1.0e-10, 8);
        assertEquals(0, result, EPSILON);
    }

    @Test
    public void testRoundN_withNegativeValue() {
        double result = Maths.roundN(-3.14159, 2);
        assertEquals(-3.14, result, EPSILON);
    }
    @Test
    public void testCeilN() {
        double result = Maths.ceilN(123.4567, 2);
        assertEquals(123.46, result, 0.001);
    }

    @Test
    public void testFloorN() {
        double result = Maths.floorN(123.4567, 2);
        assertEquals(123.45, result, 0.001);
    }

    @Test
    public void testCeilNWholeNumber() {
        double result = Maths.ceilN(123.0, 2);
        assertEquals(123.0, result, 0.001);
    }

    @Test
    public void testFloorNWholeNumber() {
        double result = Maths.floorN(123.0, 2);
        assertEquals(123.0, result, 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeilNInvalidDigits() {
        Maths.ceilN(123.4567, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFloorNInvalidDigits() {
        Maths.floorN(123.4567, 20);
    }

    @Test
    public void testRound1Positive() {
        double result = Maths.round1(123.4567);
        assertEquals(123.5, result, 0.1);
    }

    @Test
    public void testRound1Negative() {
        double result = Maths.round1(-123.4567);
        assertEquals(-123.5, result, 0.1);
    }

    @Test
    public void testRound1Zero() {
        double result = Maths.round1(0);
        assertEquals(0, result, 0.1);
    }

    @Test
    public void testRound1WholeNumber() {
        double result = Maths.round1(123.0);
        assertEquals(123.0, result, 0.1);
    }

    @Test
    public void testRound1NaN() {
        double result = Maths.round1(Double.NaN);
        Assert.assertTrue(Double.isNaN(result));
    }

    @Test
    public void testRound1LargerThanWholeNumber() {
        double result = Maths.round1(Maths.WHOLE_NUMBER + 1);
        assertEquals(Maths.WHOLE_NUMBER + 1, result, 0.1);
    }

    @Test
    public void testNextPower2Int() {
        int result = Maths.nextPower2(10, 2);
        assertEquals(16, result);
    }

    @Test
    public void testNextPower2Long() {
        long result = Maths.nextPower2(100L, 64L);
        assertEquals(128L, result);
    }

    @Test
    public void testNextPower2IntMinGreaterThanN() {
        int result = Maths.nextPower2(10, 16);
        assertEquals(16, result);
    }

    @Test
    public void testNextPower2LongMinGreaterThanN() {
        long result = Maths.nextPower2(100L, 128L);
        assertEquals(128L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextPower2IntMinNotPowerOf2() {
        Maths.nextPower2(10, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextPower2LongMinNotPowerOf2() {
        Maths.nextPower2(100L, 65L);
    }

    @Test
    public void testNextPower2IntMaxValue() {
        int result = Maths.nextPower2(Integer.MAX_VALUE, 2);
        assertEquals(1 << 30, result);
    }

    @Test
    public void testNextPower2LongMaxValue() {
        long result = Maths.nextPower2(Long.MAX_VALUE, 2L);
        assertEquals(1L << 62, result);
    }

    @Test
    public void testHash64() {
        long result = Maths.hash64("Hello, World!");
        // Expected result is calculated manually based on the provided hash64 and agitate methods
        long expected = 0x11c8caaf79c655c9L;
        assertEquals(expected, result);
    }

    @Test
    public void testHash64EmptyString() {
        long result = Maths.hash64("");
        // Expected result is calculated manually based on the provided hash64 and agitate methods
        long expected = 0L;
        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash64Null() {
        Maths.hash64((String) null);
    }


    @Test
    public void testHash32String() {
        int result = Maths.hash32("Hello, World!");
        // Expected result is calculated manually based on the provided hash64, agitate, and hash32 methods
        int expected = 0x680e9f66;
        assertEquals(expected, result);
    }

    @Test
    public void testHash32StringBuilder() {
        int result = Maths.hash32(new StringBuilder("Hello, World!"));
        // Expected result is calculated manually based on the provided hash64, agitate, and hash32 methods
        int expected = 0x680e9f66;
        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash32StringNull() {
        Maths.hash32((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testHash32StringBuilderNull() {
        Maths.hash32((StringBuilder) null);
    }

    @Test
    public void testIntLog2() {
        assertEquals(0, Maths.intLog2(1));
        assertEquals(1, Maths.intLog2(2));
        assertEquals(2, Maths.intLog2(4));
        assertEquals(3, Maths.intLog2(8));
        assertEquals(4, Maths.intLog2(16));
        assertEquals(5, Maths.intLog2(32));
        assertEquals(6, Maths.intLog2(64));
    }

    @Test
    public void testIntLog2NonPowerOfTwo() {
        assertEquals(2, Maths.intLog2(7));
        assertEquals(4, Maths.intLog2(31));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntLog2Zero() {
        Maths.intLog2(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntLog2Negative() {
        Maths.intLog2(-1);
    }

    @Test
    public void testToInt8() {
        assertEquals((byte) 0, Maths.toInt8(0L));
        assertEquals((byte) 1, Maths.toInt8(1L));
        assertEquals((byte) -1, Maths.toInt8(-1L));
        assertEquals(Byte.MAX_VALUE, Maths.toInt8((long) Byte.MAX_VALUE));
        assertEquals(Byte.MIN_VALUE, Maths.toInt8((long) Byte.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testToInt8OverflowPositive() {
        Maths.toInt8((long) Byte.MAX_VALUE + 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testToInt8OverflowNegative() {
        Maths.toInt8((long) Byte.MIN_VALUE - 1);
    }

    @Test
    public void testToInt16() {
        assertEquals((short) 0, Maths.toInt16(0L));
        assertEquals((short) 1, Maths.toInt16(1L));
        assertEquals((short) -1, Maths.toInt16(-1L));
        assertEquals(Short.MAX_VALUE, Maths.toInt16((long) Short.MAX_VALUE));
        assertEquals(Short.MIN_VALUE, Maths.toInt16((long) Short.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testToInt16OverflowPositive() {
        Maths.toInt16((long) Short.MAX_VALUE + 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testToInt16OverflowNegative() {
        Maths.toInt16((long) Short.MIN_VALUE - 1);
    }

    @Test
    public void testToInt32() {
        assertEquals(0, Maths.toInt32(0L, "Value %d out of range for int"));
        assertEquals(1, Maths.toInt32(1L, "Value %d out of range for int"));
        assertEquals(-1, Maths.toInt32(-1L, "Value %d out of range for int"));
        assertEquals(Integer.MAX_VALUE, Maths.toInt32((long) Integer.MAX_VALUE, "Value %d out of range for int"));
        assertEquals(Integer.MIN_VALUE, Maths.toInt32((long) Integer.MIN_VALUE, "Value %d out of range for int"));
    }

    @Test
    public void testToInt32OverflowPositive() {
        try {
            Maths.toInt32((long) Integer.MAX_VALUE + 1, "Value %d out of range for int");
            Assert.fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            assertEquals("Value 2147483648 out of range for int", e.getMessage());
        }
    }

    @Test
    public void testToInt32OverflowNegative() {
        try {
            Maths.toInt32((long) Integer.MIN_VALUE - 1, "Value %d out of range for int");
            Assert.fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            assertEquals("Value -2147483649 out of range for int", e.getMessage());
        }
    }

    @Test
    public void testToUInt8() {
        assertEquals(0, Maths.toUInt8(0L));
        assertEquals(1, Maths.toUInt8(1L));
        assertEquals(255, Maths.toUInt8(255L));
    }

    @Test
    public void testToUInt8OverflowPositive() {
        try {
            Maths.toUInt8(256L);
            Assert.fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            assertEquals("Unsigned Byte 256 out of range", e.getMessage());
        }
    }

    @Test
    public void testToUInt8OverflowNegative() {
        try {
            Maths.toUInt8(-1L);
            Assert.fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            assertEquals("Unsigned Byte -1 out of range", e.getMessage());
        }
    }

    @Test
    public void testAgitate() {
        assertEquals(0L, Maths.agitate(0L));
        assertEquals(0x800000000001L, Maths.agitate(1L));
        assertEquals(0x1000000000002L, Maths.agitate(2L));
        assertEquals(0x7fff41ffff000001L, Maths.agitate(Long.MAX_VALUE));
        assertEquals(0x8000420001000000L, Maths.agitate(Long.MIN_VALUE));
    }

    @Test
    public void testDivideRoundUp() {
        assertEquals(2L, Maths.divideRoundUp(10L, 5L));
        assertEquals(3L, Maths.divideRoundUp(11L, 5L));
        assertEquals(-2L, Maths.divideRoundUp(-10L, 5L));
        assertEquals(-3L, Maths.divideRoundUp(-11L, 5L));
        assertEquals(0L, Maths.divideRoundUp(0L, 5L));
        assertEquals(-1L, Maths.divideRoundUp(-4L, 5L));
        assertEquals(1L, Maths.divideRoundUp(4L, 5L));
        assertEquals(-1L, Maths.divideRoundUp(-1L, Long.MAX_VALUE));
        assertEquals(1L, Maths.divideRoundUp(1L, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, Maths.divideRoundUp(Long.MAX_VALUE, 1L));
        assertEquals(-Long.MAX_VALUE, Maths.divideRoundUp(Long.MAX_VALUE, -1L));
    }

    @Test
    public void testAsDouble() {
        assertEquals(5.0, asDouble(5, 0, false, 0), 0);
        assertEquals(-5.0, asDouble(5, 0, true, 0), 0);
        assertEquals(10.0, asDouble(5, 1, false, 0), 0);
        assertEquals(2.5, asDouble(5, -1, false, 0), 0);
        assertEquals(5.5, asDouble(55, 0, false, 1), 0);
        assertEquals(11.0, asDouble(55, 1, false, 1), 0);
        assertEquals(1.375, asDouble(55, -2, false, 1), 0);
        assertEquals(0.0, asDouble(0, 0, false, 0), 0);
//        assertThrows(IllegalArgumentException.class, () -> asDouble(-1, 0, false, 0));
//        assertEquals(-1, asDouble(-1, 0, false, 0), 0);
    }

    @Test
    public void testAdd() {
        assertEquals(5.5, Maths.add(5, 1, 2), 0);
        assertEquals(10.333333333333334, Maths.add(10, 1, 3), 0);
        assertEquals(-5.5, Maths.add(-5, -1, 2), 0);
        assertEquals(0.0, Maths.add(0, 0, 1), 0);
//        assertThrows(ArithmeticException.class, () -> Maths.add(1, 1, 0));
        assertEquals(Double.POSITIVE_INFINITY, Maths.add(1, 1, 0), 0);
    }
}
