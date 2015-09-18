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
    private static final int K0 = 0x6d0f27bd;
    private static final int M0 = 0x5bc80bad;
    private static final int M1 = 0xea7585d7;
    private static final int M2 = 0x7a646e19;
    private static final int M3 = 0x855dd4db;

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
        return d > WHOLE_NUMBER || d < -WHOLE_NUMBER ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
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
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
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
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
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
        return d > Long.MAX_VALUE / factor || d < -Long.MAX_VALUE / factor ? d :
                (long) (d < 0 ? d * factor - 0.5 : d * factor + 0.5) / factor;
    }

    public static int nextPower2(int n, int min) throws IllegalArgumentException {
        return (int) Math.min(1 << 30, nextPower2((long) n, (long) min));
    }

    public static long nextPower2(long n, long min) throws IllegalArgumentException {
        if (!isPowerOf2(min))
            throw new IllegalArgumentException(min+" must be a power of 2");
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
        return Long.bitCount(n) == 1;
    }

    public static int hash32(CharSequence cs) {
        long h = hash64(cs);
        h ^= h >> 32;
        return (int) h;
    }

    public static int hash32(long l0) {
        long h = hash64(l0);
        h ^= h >> 32;
        return (int) h;
    }

    public static long hash64(CharSequence cs) {
        long hash = 0;
        for (int i = 0; i < cs.length(); i++)
            hash = hash * 841248317 + cs.charAt(i);
        return agitate(hash);
    }

    public static int intLog2(long num) {
        long l = Double.doubleToRawLongBits((double) num);
        return (int) ((l >> 52) - 1023L);
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

    public static int toInt32(long x, String msg) throws IllegalArgumentException {
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
}
