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

/*
 * Based on java.util.concurrent.TimeUnit, which is
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package net.openhft.chronicle.core;

import java.util.concurrent.TimeUnit;

/**
 * A {@code MemoryUnit} represents memory amounts at a given unit of
 * granularity and provides utility methods to convert across units.  A
 * {@code MemoryUnit} does not maintain memory information, but only
 * helps organize and use memory amounts representations that may be maintained
 * separately across various contexts.
 * <p>
 * <p>A {@code MemoryUnit} is mainly used to inform memory amount-based methods
 * how a given memory amount parameter should be interpreted.
 * <p>
 * <p>API of {@code MemoryUnit} is copied from {@link TimeUnit} enum.
 */
public enum MemoryUnit {

    /**
     * Memory unit representing one bit.
     */
    BITS {
        @Override
        public long toBits(long a) {
            return a;
        }

        @Override
        public long toBytes(long a) {
            return div(a, C1 / C0);
        }

        @Override
        public long toLongs(long a) {
            return div(a, C2 / C0);
        }
    },
    /**
     * Memory unit representing 8 bytes, i. e. 64-bit word,
     * the width of Java's primitive {@code long} type.
     */
    LONGS {
        @Override
        public long toBits(long a) {
            return x(a, C2 / C0, MAX / (C2 / C0));
        }

        @Override
        public long toBytes(long a) {
            return x(a, C2 / C1, MAX / (C2 / C1));
        }

        @Override
        public long toLongs(long a) {
            return a;
        }
    };

    // Handy constants for conversion methods
    static final long C0 = 1L;
    static final long C1 = C0 * 8L;
    static final long C2 = C1 * 8L;
    static final long MAX = Long.MAX_VALUE;

    private static long div(long num, long den) {
        return (num + den - 1) / den;
    }

    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     */
    static long x(long a, long m, long over) {
        if (a > over) return Long.MAX_VALUE;
        if (a < -over) return Long.MIN_VALUE;
        return a * m;
    }

    // To maintain full signature compatibility with 1.5, and to improve the
    // clarity of the generated javadoc (see 6287639: Abstract methods in
    // enum classes should not be listed as abstract), method convert
    // etc. are not declared abstract but otherwise act as abstract methods.

    /**
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toBits(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toBytes(long amount) {
        throw new AbstractMethodError();
    }

    /**
     * @param amount the amount
     * @return the converted amount,
     * or {@code Long.MIN_VALUE} if conversion would negatively
     * overflow, or {@code Long.MAX_VALUE} if it would positively overflow.
     */
    public long toLongs(long amount) {
        throw new AbstractMethodError();
    }
}
