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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.annotation.ForceInline;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static java.lang.Character.toLowerCase;

/**
 * Created by Rob Austin
 */
public enum StringUtils {
    ;

    private static final Constructor<String> STRING_CONSTRUCTOR;
    private static final Field S_VALUE, SB_VALUE, SB_COUNT;
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;

    static {
        try {
            STRING_CONSTRUCTOR = String.class.getDeclaredConstructor(char[].class, boolean.class);
            STRING_CONSTRUCTOR.setAccessible(true);
            S_VALUE = String.class.getDeclaredField("value");
            S_VALUE.setAccessible(true);
            SB_VALUE = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("value");
            SB_VALUE.setAccessible(true);
            SB_COUNT = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("count");
            SB_COUNT.setAccessible(true);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static boolean endsWith(@NotNull final CharSequence source,
                                   @NotNull final String endsWith) {
        for (int i = 1; i <= endsWith.length(); i++) {
            if (toLowerCase(source.charAt(source.length() - i)) !=
                    toLowerCase(endsWith.charAt(endsWith.length() - i))) {
                return false;
            }
        }

        return true;
    }

    @ForceInline
    public static boolean isEqual(CharSequence s, CharSequence cs) {
        if (s == cs)
            return true;
        if (s == null) return false;
        if (cs == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (s.charAt(i) != cs.charAt(i))
                return false;
        return true;
    }

    @ForceInline
    public static boolean equalsCaseIgnore(CharSequence s, CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (Character.toLowerCase(s.charAt(i)) !=
                    Character.toLowerCase(cs.charAt(i)))
                return false;
        return true;
    }

    @ForceInline
    public static String toString(Object o) {
        return o == null ? null : o.toString();
    }

    public static char[] extractChars(StringBuilder sb) {
        try {
            return (char[]) SB_VALUE.get(sb);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    public static char[] extractChars(String s) {
        try {
            return (char[]) S_VALUE.get(s);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    public static void setCount(StringBuilder sb, int count) {
        try {
            SB_COUNT.setInt(sb, count);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    public static String newString(char[] chars) {
        try {
            return STRING_CONSTRUCTOR.newInstance(chars, true);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static String firstLowerCase(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static double parseDouble(@net.openhft.chronicle.core.annotation.NotNull
                                     CharSequence in) {
        long value = 0;
        int exp = 0;
        boolean negative = false;
        int decimalPlaces = Integer.MIN_VALUE;

        int ch = in.charAt(0);
        int pos = 1;
        switch (ch) {
            case 'N':
                if (compareRest(in, 1, "aN"))
                    return Double.NaN;
                return Double.NaN;
            case 'I':
                //noinspection SpellCheckingInspection
                if (compareRest(in, 1, "nfinity"))
                    return Double.POSITIVE_INFINITY;

                return Double.NaN;
            case '-':
                if (compareRest(in, 1, "Infinity"))
                    return Double.NEGATIVE_INFINITY;
                negative = true;
                ch = in.charAt(pos++);
                break;
        }
        while (true) {
            if (ch >= '0' && ch <= '9') {
                while (value >= MAX_VALUE_DIVIDE_10) {
                    value >>>= 1;
                    exp++;
                }
                value = value * 10 + (ch - '0');
                decimalPlaces++;

            } else if (ch == '.') {
                decimalPlaces = 0;

            } else {
                break;
            }
            if (pos == in.length())
                break;
            ch = in.charAt(pos++);
        }

        return asDouble(value, exp, negative, decimalPlaces);
    }

    private static boolean compareRest(@net.openhft.chronicle.core.annotation.NotNull CharSequence in,
                                       int pos, @net.openhft.chronicle.core.annotation.NotNull String s) {

        if (s.length() > in.length() - pos)
            return false;

        for (int i = 0; i < s.length(); i++) {
            if (in.charAt(i + pos) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static double asDouble(long value, int exp, boolean negative, int decimalPlaces) {
        if (decimalPlaces > 0 && value < Long.MAX_VALUE / 2) {
            if (value < Long.MAX_VALUE / (1L << 32)) {
                exp -= 32;
                value <<= 32;
            }
            if (value < Long.MAX_VALUE / (1L << 16)) {
                exp -= 16;
                value <<= 16;
            }
            if (value < Long.MAX_VALUE / (1L << 8)) {
                exp -= 8;
                value <<= 8;
            }
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
            }
        }
        for (; decimalPlaces > 0; decimalPlaces--) {
            exp--;
            long mod = value % 5;
            value /= 5;
            int modDiv = 1;
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
                modDiv <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
                modDiv <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
                modDiv <<= 1;
            }
            if (decimalPlaces > 1)
                value += modDiv * mod / 5;
            else
                value += (modDiv * mod + 4) / 5;
        }
        final double d = Math.scalb((double) value, exp);
        return negative ? -d : d;
    }
}
