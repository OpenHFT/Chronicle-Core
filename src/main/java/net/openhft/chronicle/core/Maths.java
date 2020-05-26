/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum Maths {
    ;

    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    private static final double WHOLE_NUMBER = 1L << 52;
    private static final int K0 = 0x6d0f27bd;
    private static final int M0 = 0x5bc80bad;
    private static final int M1 = 0xea7585d7;
    private static final int M2 = 0x7a646e19;
    private static final int M3 = 0x855dd4db;
    @NotNull
    static long[] TENS = new long[19];
    @NotNull
    static long[] FIVES = new long[28];

    static {
        TENS[0] = FIVES[0] = 1;
        for (int i = 1; i < TENS.length; i++)
            TENS[i] = 10 * TENS[i - 1];
        for (int i = 1; i < FIVES.length; i++)
            FIVES[i] = 5 * FIVES[i - 1];
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d      value to round
     * @param digits 0 to 18 digits of precision
     * @return rounded value
     */
    public static double roundN(double d, int digits) {
        final long factor = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (double) (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static long roundingFactor(int digits) {
        return TENS[digits];
    }

    public static long roundingFactor(double digits) {
        int iDigits = (int) digits;
        long ten = TENS[iDigits];

        switch ((int) ((digits - iDigits) * 10 + 0.5)) {
            case 0:
            case 1:
            case 2:
                return ten;
            case 3:
            case 4:
            case 5:
                return 2 * ten;
            case 6:
                return 4 * ten;
            case 7:
            case 8:
                return 5 * ten;
            case 9:
                return 8 * ten;
            default:
                return 10 * ten;
        }
    }

    public static double ceilN(double d, int digits) {
        final long factor = roundingFactor(digits + 8);
        final long factor2 = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? Math.ceil(Math.round(d * factor) / 1e8) / factor2 : d;
    }

    public static double floorN(double d, int digits) {
        final long factor = roundingFactor(digits + 8);
        final long factor2 = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? Math.floor(Math.round(d * factor) / 1e8) / factor2 : d;
    }

    public static double roundN(double d, double digits) {
        final long factor = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (double) (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static double ceilN(double d, double digits) {
        final long factor = roundingFactor(digits + 8);
        final long factor2 = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? Math.ceil(Math.round(d * factor) / 1e8) / factor2 : d;
    }

    public static double floorN(double d, double digits) {
        final long factor = roundingFactor(digits + 8);
        final long factor2 = roundingFactor(digits);
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? Math.floor(Math.round(d * factor) / 1e8) / factor2 : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round1(double d) {
        final double factor = 1e1;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round2(double d) {
        final double factor = 1e2;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round3(double d) {
        final double factor = 1e3;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round4(double d) {
        final double factor = 1e4;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round5(double d) {
        final double factor = 1e5;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round6(double d) {
        final double factor = 1e6;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round7(double d) {
        final double factor = 1e7;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5 it
     * might be rounded up or down. This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round8(double d) {
        final double factor = 1e8;
        return Math.abs(d) < WHOLE_NUMBER / factor
                ? (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor : d;
    }

    public static int nextPower2(int n, int min) throws IllegalArgumentException {
        return (int) Math.min(1 << 30, nextPower2((long) n, (long) min));
    }

    public static long nextPower2(long n, long min) throws IllegalArgumentException {
        if (!isPowerOf2(min))
            throw new IllegalArgumentException(min + " must be a power of 2");
        if (n < min) return min;
        if (isPowerOf2(n))
            return n;
        long i = min;
        while (i < n) {
            i *= 2;
            if (i <= 0) return 1L << 62;
        }
        return i;
    }

    public static boolean isPowerOf2(long n) {
        return Long.bitCount(n) == 1;
    }

    public static int hash32(@NotNull CharSequence cs) {
        long h = hash64(cs);
        h ^= h >> 32;
        return (int) h;
    }

    public static int hash32(@NotNull String s) {
        long h = hash64(s);
        h ^= h >> 32;
        return (int) h;
    }

    public static int hash32(@NotNull StringBuilder s) {
        long h = hash64(s);
        h ^= h >> 32;
        return (int) h;
    }

    public static int hash32(long l0) {
        long h = hash64(l0);
        h ^= h >> 32;
        return (int) h;
    }

    public static long hash64(@NotNull CharSequence cs) {
        long hash = 0;
        for (int i = 0, len = cs.length(); i < len; i++)
            hash = hash * 841248317 + cs.charAt(i);
        return agitate(hash);
    }

    public static long hash64(@NotNull String s) {
        long hash = 0;

        if (Jvm.isJava9Plus()) {
            final byte[] bytes = StringUtils.extractBytes(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 841248317 + bytes[i];
        } else {
            final char[] chars = StringUtils.extractChars(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 841248317 + chars[i];
        }
        return agitate(hash);
    }

    public static long hash64(@NotNull StringBuilder s) {
        long hash = 0;

        if (Jvm.isJava9Plus()) {
            final byte[] bytes = StringUtils.extractBytes(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 841248317 + bytes[i];
        } else {
            final char[] chars = StringUtils.extractChars(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 841248317 + chars[i];
        }
        return agitate(hash);
    }

    /**
     * Returns rounded down log<sub>2</sub>{@code num}, e. g.: {@code intLog2(1) == 0},
     * {@code intLog2(2) == 1}, {@code intLog2(7) == 2}, {@code intLog2(8) == 3}, etc.
     *
     * @throws IllegalArgumentException if the given number &lt;= 0
     */
    public static int intLog2(long num) {
        if (num <= 0)
            throw new IllegalArgumentException("positive argument expected, " + num + " given");
        return 63 - Long.numberOfLeadingZeros(num);
    }

    public static byte toInt8(long x) throws IllegalArgumentException {
        if ((byte) x == x)
            return (byte) x;
        throw new IllegalArgumentException("Byte " + x + " out of range");
    }

    public static short toInt16(long x) throws IllegalArgumentException {
        if ((short) x == x)
            return (short) x;
        throw new IllegalArgumentException("Short " + x + " out of range");
    }

    public static int toInt32(long x, @NotNull String msg) throws IllegalArgumentException {
        if ((int) x == x)
            return (int) x;
        throw new IllegalArgumentException(String.format(msg, x));
    }

    public static int toInt32(long x) throws IllegalArgumentException {
        if ((int) x == x)
            return (int) x;
        throw new IllegalArgumentException("Int " + x + " out of range");
    }

    public static short toUInt8(long x) throws IllegalArgumentException {
        if ((x & 0xFF) == x)
            return (short) x;
        throw new IllegalArgumentException("Unsigned Byte " + x + " out of range");
    }

    public static int toUInt16(long x) throws IllegalArgumentException {
        if ((x & 0xFFFF) == x)
            return (int) x;
        throw new IllegalArgumentException("Unsigned Short " + x + " out of range");
    }

    public static int toUInt31(long x) throws IllegalArgumentException {
        if ((x & 0x7FFFFFFFL) == x)
            return (int) x;
        throw new IllegalArgumentException("Unsigned Int 31-bit " + x + " out of range");
    }

    public static long toUInt32(long x) throws IllegalArgumentException {
        if ((x & 0xFFFFFFFFL) == x)
            return x;
        throw new IllegalArgumentException("Unsigned Int " + x + " out of range");
    }

    public static long agitate(long l) {
        l += l >>> 22;
        l ^= Long.rotateRight(l, 17);
        return l;
    }

    /**
     * A simple hashing algorithm for a 64-bit value
     *
     * @param l0 to hash
     * @return hash value.
     */
    public static long hash64(long l0) {
        int l0a = (int) (l0 >> 32);

        long h0 = l0 * M0 + l0a * M1;

        return agitate(h0);
    }

    /**
     * A simple hashing algorithm for a 128-bit value
     *
     * @param l0 to hash
     * @param l1 to hash
     * @return hash value.
     */
    public static long hash64(long l0, long l1) {
        int l0a = (int) (l0 >> 32);
        int l1a = (int) (l1 >> 32);

        long h0 = (l0 + l1a) * M0;
        long h1 = (l1 + l0a) * M1;

        return agitate(h0) ^ agitate(h1);
    }

    /**
     * Divide {@code dividend} by divisor, if division is not integral the result is rounded up.
     * Examples: {@code divideRoundUp(10, 5) == 2}, {@code divideRoundUp(11, 5) == 3},
     * {@code divideRoundUp(-10, 5) == -2}, {@code divideRoundUp(-11, 5) == -3}.
     *
     * @return the rounded up quotient
     */
    public static long divideRoundUp(long dividend, long divisor) {
        int sign = (dividend > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
        return sign * (Math.abs(dividend) + Math.abs(divisor) - 1) / Math.abs(divisor);
    }

    public static long tens(int decimalPlaces) {
        return TENS[decimalPlaces];
    }

    public static int digits(long num) {
        int index = Arrays.binarySearch(TENS, num);
        return index < -1 ? -1-index : index >= 0 ? index+1 : 1;
    }

    public static long fives(int decimalPlaces) {
        return FIVES[decimalPlaces];
    }

    public static boolean same(double a, double b) {
        return Double.isNaN(a) ? Double.isNaN(b) : a == b;
    }

    public static boolean same(float a, float b) {
        return Float.isNaN(a) ? Float.isNaN(b) : a == b;
    }
}
