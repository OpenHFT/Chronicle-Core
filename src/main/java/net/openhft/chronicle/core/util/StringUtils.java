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
        if (s == null) return false;
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
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static char[] extractChars(String s) {
        try {
            return (char[]) S_VALUE.get(s);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void setCount(StringBuilder sb, int count) {
        try {
            SB_COUNT.setInt(sb, count);
        } catch (IllegalAccessException e) {
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
}
