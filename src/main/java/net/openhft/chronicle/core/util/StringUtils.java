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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.annotation.ForceInline;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static java.lang.Character.toLowerCase;

/**
 * Created by Rob Austin
 */
public enum StringUtils {
    ;

    private static final Field S_VALUE, SB_VALUE, SB_COUNT;
    private static final long S_VALUE_OFFSET, SB_VALUE_OFFSET, SB_COUNT_OFFSET;
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;

    static {
        try {
            S_VALUE = String.class.getDeclaredField("value");
            S_VALUE.setAccessible(true);
            S_VALUE_OFFSET = OS.memory().getFieldOffset(S_VALUE);
            SB_VALUE = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("value");
            SB_VALUE.setAccessible(true);
            SB_VALUE_OFFSET = OS.memory().getFieldOffset(SB_VALUE);
            SB_COUNT = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField("count");
            SB_COUNT.setAccessible(true);
            SB_COUNT_OFFSET = OS.memory().getFieldOffset(SB_COUNT);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void setLength(StringBuilder sb, int length) {
        try {
            SB_COUNT.set(sb, length);
        } catch (IllegalAccessException e) {
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
    public static boolean isEqual(StringBuilder s, CharSequence cs) {
        if (s == cs)
            return true;
        if (s == null) return false;
        if (cs == null) return false;
        int length = cs.length();
        if (s.length() != length) return false;
        char[] chars = StringUtils.extractChars(s);
        for (int i = 0; i < length; i++)
            if (chars[i] != cs.charAt(i))
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
        return OS.memory().getObject(sb, SB_VALUE_OFFSET);
    }

    public static char[] extractChars(String s) {
        return OS.memory().getObject(s, S_VALUE_OFFSET);
    }

    public static void setCount(StringBuilder sb, int count) {
        OS.memory().setInt(sb, SB_COUNT_OFFSET, count);
    }

    public static String newString(char[] chars) {
        //noinspection RedundantStringConstructorCall
        String str = new String();
        try {
            S_VALUE.set(str, chars);
            return str;
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
