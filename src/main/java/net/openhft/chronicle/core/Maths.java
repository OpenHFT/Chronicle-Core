/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core;

public enum Maths {
    ;
    /**
     * Numbers larger than this are whole numbers due to representation error.
     */
    private static final double WHOLE_NUMBER = 1L << 53;

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5
     * it might be rounded up or down. This is a pragmatic choice for performance reasons
     * as it is assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round2(double d) {
        final double factor = 1e2;
        return d > WHOLE_NUMBER || d < -WHOLE_NUMBER ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5
     * it might be rounded up or down. This is a pragmatic choice for performance reasons
     * as it is assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round4(double d) {
        final double factor = 1e4;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5
     * it might be rounded up or down. This is a pragmatic choice for performance reasons
     * as it is assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round6(double d) {
        final double factor = 1e6;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    /**
     * Performs a round which is accurate to within 1 ulp. i.e. for values very close to 0.5
     * it might be rounded up or down. This is a pragmatic choice for performance reasons
     * as it is assumed you are not working on the edge of the precision of double.
     *
     * @param d value to round
     * @return rounded value
     */
    public static double round8(double d) {
        final double factor = 1e8;
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    public static int nextPower2(int n, int min) {
        return (int) Math.min(1 << 30, nextPower2((long) n, (long) min));
    }

    public static long nextPower2(long n, long min) {
        if (!isPowerOf2(min))
            throw new IllegalArgumentException();
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

    static boolean isPowerOf2(long n) {
        return n != 0 && (n & (n - 1L)) == 0L;
    }

    public static int hash(CharSequence cs) {
        long h = longHash(cs);
        return (int) (h ^ (h >> 32));
    }

    public static long longHash(CharSequence cs) {
        long hash = 0;
        for (int i = 0; i < cs.length(); i++)
            hash = Long.rotateLeft(hash, 7) + cs.charAt(i);
        return longHash(hash);
    }

    public static int intLog2(long num) {
        long l = Double.doubleToRawLongBits((double) num);
        return (int) ((l >> 52) - 1023L);
    }

    public static byte toInt8(long x) {
        if ((byte) x == x)
            return (byte) x;
        throw new IllegalArgumentException("Byte " + x + " out of range");
    }

    public static short toInt16(long x) {
        if ((short) x == x)
            return (short) x;
        throw new IllegalArgumentException("Short " + x + " out of range");
    }

    public static int toInt32(long x, String msg) {
        if ((int) x == x)
            return (int) x;
        throw new IllegalArgumentException(String.format(msg, x));
    }

    public static int toInt32(long x) {
        if ((int) x == x)
            return (int) x;
        throw new IllegalArgumentException("Int " + x + " out of range");
    }

    public static short toUInt8(long x) {
        if ((x & 0xFF) == x)
            return (short) x;
        throw new IllegalArgumentException("Unsigned Byte " + x + " out of range");
    }

    public static int toUInt16(long x) {
        if ((x & 0xFFFF) == x)
            return (int) x;
        throw new IllegalArgumentException("Unsigned Short " + x + " out of range");
    }

    public static int toUInt31(long x) {
        if ((x & 0x7FFFFFFFL) == x)
            return (int) x;
        throw new IllegalArgumentException("Unsigned Int 31-bit " + x + " out of range");
    }

    public static long toUInt32(long x) {
        if ((x & 0xFFFFFFFFL) == x)
            return x;
        throw new IllegalArgumentException("Unsigned Int " + x + " out of range");
    }

    public static long agitate(long l) {
        l ^= l >> 23;
        l += Long.rotateRight(l, 18);
        return l;
    }

    /**
     * net.openhft.chronicle.bytes.algo.NativeBytesHash
     *
     * @param l to hash
     * @return int hash value.
     */
    public static long longHash(long l) {
        long h0 = -7826470488L + l * 0x4932_5e2f;
        long h1 = (l >> 32) * 0x3275_2743;

        return agitate(h0) ^ agitate(h1);
    }
}
