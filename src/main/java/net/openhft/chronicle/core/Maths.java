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

import net.openhft.chronicle.assertions.AssertUtil;
import net.openhft.chronicle.core.annotation.NonNegative;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class Maths {

    // Suppresses default constructor, ensuring non-instantiability.
    private Maths() {
    }

    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    private static final double WHOLE_NUMBER = 1L << 52;
    private static final int M0 = 0x5bc80bad;
    private static final int M1 = 0xea7585d7;
    private static final long[] TENS = new long[19];
    private static final long[] FIVES = new long[28];
    private static final String OUT_OF_RANGE = " out of range";

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

    /**
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double roundNup(double d, int digits) {
        if (d < 0)
            return -roundNup(-d, digits);
        final long factor = roundingFactor(digits);
        if (d >= WHOLE_NUMBER / factor)
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / (double) factor;
    }

    public static long roundingFactor(int digits) {
        return tens(digits);
    }

    public static long roundingFactor(double digits) {
        int iDigits = (int) digits;
        long ten = tens(iDigits);

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

    /**
     * Rounds up the given double value to the nearest higher number,
     * accurate to the specified number of decimal places.
     *
     * @param d      The double value to be rounded up.
     * @param digits The number of decimal places to which the value is to be rounded.
     * @return The value rounded up to the nearest higher number.
     */
    public static double ceilN(double d, int digits) {
        final long factor = roundingFactor(digits);
        double ulp = Math.ulp(d);
        double ulp2 = ulp * factor;
        return Math.abs(d) < (double) Long.MAX_VALUE / factor && ulp2 < 1
                ? Math.ceil((d - ulp) * factor) / factor : d;
    }

    /**
     * Rounds down the given double value to the nearest lower number,
     * accurate to the specified number of decimal places.
     *
     * @param d      The double value to be rounded down.
     * @param digits The number of decimal places to which the value is to be rounded.
     * @return The value rounded down to the nearest lower number.
     */
    public static double floorN(double d, int digits) {
        final long factor = roundingFactor(digits);
        double ulp = Math.ulp(d);
        double ulp2 = ulp * factor;
        return Math.abs(d) < (double) Long.MAX_VALUE / factor && ulp2 < 1
                ? Math.floor((d + ulp) * factor) / factor : d;
    }

    public static double roundN(double d, double digits) {
        final long factor = roundingFactor(digits);
        return Math.abs(d) < (double) Long.MAX_VALUE / factor
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
        if (d < 0)
            return -round1(-d);
        final double factor = 1e1;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        return (long) (d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round1up(double d) {
        return round1(d);
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round2up(double d) {
        if (d < 0)
            return -round2(-d);
        final double factor = 1e2;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round3up(double d) {
        if (d < 0)
            return -round3(-d);
        final double factor = 1e3;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round4up(double d) {
        if (d < 0)
            return -round4(-d);
        final double factor = 1e4;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round5up(double d) {
        if (d < 0)
            return -round5(-d);
        final double factor = 1e5;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round6up(double d) {
        if (d < 0)
            return -round6(-d);
        final double factor = 1e6;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
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
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round7up(double d) {
        if (d < 0)
            return -round7(-d);
        final double factor = 1e7;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
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

    /**
     * Performs a round which is accurate to within 1 ulp.
     * The value 0.5 should round up however the value one ulp less might round up or down.
     * This is a pragmatic choice for performance reasons as it is
     * assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round8up(double d) {
        if (d < 0)
            return -round8(-d);
        final double factor = 1e8;
        if (!(d <= WHOLE_NUMBER / factor)) // to handle NaN
            return d;
        final double df = d * factor;
        long ldf = (long) df;
        final double residual = df - ldf + Math.ulp(d) * (factor * 0.983);
        if (residual >= 0.5)
            ldf++;
        return ldf / factor;
    }

    /**
     * Returns the next power of two.
     *
     * @param n   to find the next power of two from
     * @param min if n < min then use min
     * @return the next power of two
     * @throws IllegalArgumentException if the provided {@code min} value is not a power of two.
     */
    public static int nextPower2(int n, int min) throws IllegalArgumentException {
        return (int) Math.min(1 << 30, nextPower2(n, (long) min));
    }

    /**
     * Returns the next power of two.
     *
     * @param n   to find the next power of two from
     * @param min if n < min then use min
     * @return the next power of two
     * @throws IllegalArgumentException if the provided {@code min} value is not a power of two.
     */
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

    /**
     * Computes a 32-bit hash value for the given string.
     * This is done by first computing a 64-bit hash using the {@link #hash64(String)} method,
     * and then combining the upper and lower 32 bits of the hash using XOR.
     *
     * @param s the string to compute the hash for
     * @return the 32-bit hash value
     * @throws IllegalArgumentException if {@code s} is {@code null}
     */
    public static int hash32(@NotNull String s) {
        long h = hash64(s);
        h ^= h >> 32;
        return (int) h;
    }

    /**
     * Computes a 32-bit hash value for the given StringBuilder.
     * This is done by first computing a 64-bit hash using the {@link #hash64(StringBuilder)} method,
     * and then combining the upper and lower 32 bits of the hash using XOR.
     *
     * @param s the StringBuilder to compute the hash for
     * @return the 32-bit hash value
     * @throws IllegalArgumentException if {@code s} is {@code null}
     */
    public static int hash32(@NotNull StringBuilder s) {
        long h = hash64(s);
        h ^= h >> 32;
        return (int) h;
    }

    @Deprecated(/* to be removed in x.25 */)
    public static int hash32(long l0) {
        long h = hash64(l0);
        h ^= h >> 32;
        return (int) h;
    }

    public static long hash64(@Nullable CharSequence cs) {
        if (cs == null || cs.length() == 0)
            return 0;
        if (cs instanceof String)
            return hash64((String) cs);
        try {
            long hash = 0;
            for (int i = 0, len = cs.length(); i < len; i++)
                hash = hash * 0x32246e3d + cs.charAt(i);
            return agitate(hash);
        } catch (IndexOutOfBoundsException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Computes a 64-bit hash value for the given string.
     * The hash function used is dependent on the Java version and the internal representation of the string.
     * For Java 9 and later, if the string is represented as a byte array, each byte is incorporated into the hash.
     * Otherwise, each character is incorporated into the hash.
     * For Java 8 and earlier, each character is incorporated into the hash.
     * The hash value is agitated using a function not shown in this code snippet.
     *
     * @param s the string to compute the hash for
     * @return the 64-bit hash value
     * @throws IllegalArgumentException if {@code s} is {@code null}
     */
    public static long hash64(@NotNull String s) {
        //noinspection ConstantValue
        if (s == null) throw new IllegalArgumentException();
        long hash = 0;

        if (Jvm.isJava9Plus()) {
            if (StringUtils.getStringCoder(s) == 0) {
                final byte[] bytes = StringUtils.extractBytes(s);
                for (int i = 0, len = s.length(); i < len; i++)
                    hash = hash * 0x32246e3d + bytes[i];
            } else {
                for (int i = 0, len = s.length(); i < len; i++)
                    hash = hash * 0x32246e3d + s.charAt(i);
            }
        } else {
            final char[] chars = StringUtils.extractChars(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 0x32246e3d + chars[i];
        }
        return agitate(hash);
    }

    /**
     * Computes a 64-bit hash value for the given StringBuilder.
     * The hash function used is dependent on the Java version and the internal representation of the string.
     * For Java 9 and later, if the string is represented as a byte array, each byte is incorporated into the hash.
     * Otherwise, each character is incorporated into the hash.
     * For Java 8 and earlier, each character is incorporated into the hash.
     * The hash is then agitated using a function not shown in this code snippet.
     *
     * @param s the string to compute the hash for
     * @return the 64-bit hash value
     * @throws IllegalArgumentException if {@code s} is {@code null}
     */
    public static long hash64(@NotNull StringBuilder s) {
        long hash = 0;

        if (Jvm.isJava9Plus()) {
            if (StringUtils.getStringCoder(s) == 0) {
                final byte[] bytes = StringUtils.extractBytes(s);
                for (int i = 0, len = s.length(); i < len; i++)
                    hash = hash * 0x32246e3d + bytes[i];
            } else {
                final byte[] bytes = StringUtils.extractBytes(s);
                for (int i = 0, len = s.length(); i < len; i++)
                    hash = hash * 0x32246e3d + s.charAt(i);
            }
        } else {
            final char[] chars = StringUtils.extractChars(s);
            for (int i = 0, len = s.length(); i < len; i++)
                hash = hash * 0x32246e3d + chars[i];
        }
        return agitate(hash);
    }

    /**
     * Returns rounded down log<sub>2</sub>{@code num}, e. g.: {@code intLog2(1) == 0},
     * {@code intLog2(2) == 1}, {@code intLog2(7) == 2}, {@code intLog2(8) == 3}, etc.
     *
     * @throws IllegalArgumentException if the given number &lt;= 0
     */
    public static int intLog2(long num) throws IllegalArgumentException {
        if (num <= 0)
            throw new IllegalArgumentException("positive argument expected, " + num + " given");
        return 63 - Long.numberOfLeadingZeros(num);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows a {@code byte}.
     *
     * @param value the long value
     * @return the argument as a byte
     * @throws ArithmeticException if the {@code argument} overflows a byte
     */
    public static byte toInt8(long value) throws ArithmeticException {
        if ((byte) value == value)
            return (byte) value;
        throw new ArithmeticException("Byte " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows a {@code short}.
     *
     * @param value the long value
     * @return the argument as a short
     * @throws ArithmeticException if the {@code argument} overflows a short
     */
    public static short toInt16(long value) throws ArithmeticException {
        if ((short) value == value)
            return (short) value;
        throw new ArithmeticException("Short " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an {@code int}.
     *
     * @param value the long value
     * @param msg   to use in a potential exception message
     * @return the argument as an int
     * @throws ArithmeticException if the {@code argument} overflows an int
     */
    public static int toInt32(long value, @NotNull String msg) throws ArithmeticException {
        if ((int) value == value)
            return (int) value;
        throw new ArithmeticException(String.format(msg, value));
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an {@code int}.
     *
     * @param value the long value
     * @return the argument as an int
     * @throws ArithmeticException if the {@code argument} overflows an int
     */
    public static int toInt32(long value) throws ArithmeticException {
        if ((int) value == value)
            return (int) value;
        throw new ArithmeticException("Int " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an unsigned byte (0xFF).
     *
     * @param value the long value
     * @return the argument as a short
     * @throws ArithmeticException if the {@code argument} overflows an unsigned byte
     */
    public static short toUInt8(long value) throws ArithmeticException {
        if ((value & 0xFF) == value)
            return (short) value;
        throw new ArithmeticException("Unsigned Byte " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an unsigned short (0xFFFF).
     *
     * @param value the long value
     * @return the argument as an int
     * @throws ArithmeticException if the {@code argument} overflows an unsigned short
     */
    public static int toUInt16(long value) throws ArithmeticException {
        if ((value & 0xFFFF) == value)
            return (int) value;
        throw new ArithmeticException("Unsigned Short " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an unsigned 31 bit value (0x7FFFFFFFL).
     *
     * @param value the long value
     * @return the argument as a long
     * @throws ArithmeticException if the {@code argument} overflows an unsigned int
     */
    public static int toUInt31(long value) throws ArithmeticException {
        if ((value & 0x7FFFFFFFL) == value)
            return (int) value;
        throw new ArithmeticException("Unsigned Int 31-bit " + value + OUT_OF_RANGE);
    }

    /**
     * Returns the value of the {@code long} argument;
     * throwing an exception if the value overflows an unsigned int (0xFFFFFFFFL).
     *
     * @param value the long value
     * @return the argument as a long
     * @throws ArithmeticException if the {@code argument} overflows an unsigned int
     */
    public static long toUInt32(long value) throws ArithmeticException {
        if ((value & 0xFFFFFFFFL) == value)
            return value;
        throw new ArithmeticException("Unsigned Int " + value + OUT_OF_RANGE);
    }

    /**
     * Performs a series of bit operations on a long value to "agitate" the bits.
     * This includes a right shift, addition, and a right rotation with a bitwise XOR operation.
     * The purpose of this method is to create a more evenly distributed set of bit patterns
     * which can be useful in certain hashing or random number generation scenarios.
     *
     * @param l the long value to be agitated
     * @return the agitated long value
     */
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
    @Deprecated(/* to be removed in x.25 */)
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

    /**
     * Returns the power of ten for the given decimal places.
     *
     * @param decimalPlaces the number of decimal places
     * @return the power of ten corresponding to the decimal places
     * @throws IllegalArgumentException if the decimal places are less than 0 or greater than or equal to 19
     */
    public static long tens(int decimalPlaces) {
        if (decimalPlaces < 0 || decimalPlaces >= 19)
            throw decimalPlacesIAE(decimalPlaces);
        return TENS[decimalPlaces];
    }

    @NotNull
    private static IllegalArgumentException decimalPlacesIAE(int decimalPlaces) {
        return new IllegalArgumentException("decimalPlaces=" + decimalPlaces);
    }

    /**
     * Returns the number of digits in the given number.
     *
     * @param num the number to count the digits of
     * @return the number of digits in the given number
     */
    public static int digits(long num) {
        int index = Arrays.binarySearch(TENS, num);
        return index < -1 ? -1 - index : index >= 0 ? index + 1 : 1;
    }

    /**
     * Returns the power of five for the given decimal places.
     *
     * @param decimalPlaces the number of decimal places
     * @return the power of five corresponding to the decimal places
     */
    public static long fives(int decimalPlaces) {
        return FIVES[decimalPlaces];
    }

    /**
     * Checks if two double values are the same. Considers NaN values as equal.
     *
     * @param a the first double value
     * @param b the second double value
     * @return true if both values are the same or both are NaN, false otherwise
     */
    public static boolean same(double a, double b) {
        return Double.isNaN(a) ? Double.isNaN(b) : a == b;
    }

    /**
     * Checks if two float values are the same. Considers NaN values as equal.
     *
     * @param a the first float value
     * @param b the second float value
     * @return true if both values are the same or both are NaN, false otherwise
     */
    public static boolean same(float a, float b) {
        return Float.isNaN(a) ? Float.isNaN(b) : a == b;
    }

    public static int hash(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    public static int hash(Object o1, Object o2) {
        return hash(o1) * M0 + hash(o2);
    }

    public static int hash(Object o1, Object o2, Object o3) {
        return hash(o1, o2) * M0 + hash(o3);
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4) {
        return hash(o1, o2, o3) * M0 + hash(o4);
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4, Object o5) {
        return hash(o1, o2, o3, o4) * M0 + hash(o5);
    }

    /**
     * Convert components to a double value
     *
     * @param value         The integer value
     * @param exponent      The exponent
     * @param negative      Whether it is negative or not
     * @param decimalPlaces The number of decimal places
     * @return The value as a double
     */

    public static double asDouble(@NonNegative long value, int exponent, boolean negative, int decimalPlaces) {
        assert AssertUtil.SKIP_ASSERTIONS || value >= 0;
        // these numbers were determined empirically.
        int leading =
                Long.numberOfLeadingZeros(value) - 1;

        int scale2 = 0;
        if (leading > 0) {
            scale2 = leading > 10 ? (10 + leading) >> 1 : leading;
            value <<= scale2;
        }
        double d;
        if (decimalPlaces > 0) {
            if (decimalPlaces < 28) {
                long fives = Maths.fives(decimalPlaces);
                long whole = value / fives;
                long rem = value % fives;
                int wholeZero = Long.numberOfLeadingZeros(whole) - 10;
                if (wholeZero <= 0) {
                    d = roundUpOneUlp(whole, rem, wholeZero);
                } else {
                    int remZero = Long.numberOfLeadingZeros(rem) - 1;
                    int leading2 = Math.min(wholeZero, remZero);
                    d = addRemainingFraction(fives, whole, rem, leading2);
                    scale2 += leading2;
                }

            } else {
                d = value / Math.pow(5, decimalPlaces);
            }
        } else if (decimalPlaces < -27) {
            d = value * Math.pow(5, -decimalPlaces);

        } else if (decimalPlaces < 0) {
            double fives = Maths.fives(-decimalPlaces);
            d = value * fives;

        } else {
            d = value;
        }

        double scalb = Math.scalb(d, exponent - decimalPlaces - scale2);
        return negative ? -scalb : scalb;
    }

    private static double addRemainingFraction(long fives, long whole, long rem, int leading2) {
        double d;
        rem <<= leading2;
        long rem2 = rem / fives;
        d = add((whole << leading2) + rem2, rem % fives, fives);
        return d;
    }

    private static double roundUpOneUlp(long whole, long rem, int wholeZero) {
        double d;
        d = whole;
        if (wholeZero == 0) {
            long l = (long) d;
            if (rem > 0 && whole > l)
                d += Math.ulp(d);
        }
        return d;
    }

    /**
     * return a + b / c as a double
     */
    public static double add(long a, long b, long c) {
        double d = a;
        long l = (long) d;
        long diff = a - l;
        double div = diff + (double) b / c;
        return d + div;
    }
}
