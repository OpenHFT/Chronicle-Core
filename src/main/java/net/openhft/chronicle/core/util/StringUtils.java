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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.Java9;
import net.openhft.chronicle.core.internal.Bootstrap;
import net.openhft.chronicle.core.internal.ClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static java.lang.Character.toLowerCase;

/**
 * A utility class that provides a collection of static methods for advanced string manipulation.
 * <p>
 * This class contains methods for handling {@link CharSequence} and {@link StringBuilder} instances, including:
 * <ul>
 *   <li>Comparing {@link CharSequence}s for content equality.</li>
 *   <li>Checking if a {@link CharSequence} starts or ends with a specific substring.</li>
 *   <li>Setting the length and content of a {@link StringBuilder}.</li>
 *   <li>Transforming a string to Title Case.</li>
 *   <li>Reversing part of a StringBuilder's content.</li>
 *   <li>Generating {@link NumberFormatException} with custom messages.</li>
 * </ul>
 * <p>
 * Note: This class is not meant to be instantiated and therefore has a private constructor.
 */
public final class StringUtils {

    // Suppresses default constructor, ensuring non-instantiability.
    private StringUtils() {
    }

    private static final String VALUE_FIELD_NAME = "value";
    private static final String COUNT_FIELD_NAME = "count";
    private static final String CODER_FIELD_NAME = "coder";

    private static final Field S_VALUE;
    private static final Field SB_COUNT;
    private static final Field S_CODER;
    private static final Field SB_CODER;
    private static final long S_VALUE_OFFSET;
    private static final long SB_VALUE_OFFSET;
    private static final long SB_COUNT_OFFSET;
    private static final long S_COUNT_OFFSET;
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;

    static {
        try {
            S_VALUE = String.class.getDeclaredField(VALUE_FIELD_NAME);
            ClassUtil.setAccessible(S_VALUE);
            S_VALUE_OFFSET = getMemory().getFieldOffset(S_VALUE);
            if (Bootstrap.isJava9Plus()) {
                SB_CODER = ClassUtil.getField0(StringBuilder.class.getSuperclass(), CODER_FIELD_NAME, true);
                S_CODER = ClassUtil.getField0(String.class, CODER_FIELD_NAME, true);
            } else {
                S_CODER = null;
                SB_CODER = null;
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        long sCountOffset = -1;
        try {
            Field sCount = String.class.getDeclaredField(COUNT_FIELD_NAME);
            ClassUtil.setAccessible(sCount);
            sCountOffset = getMemory().getFieldOffset(sCount);
        } catch (Exception ignored) {
            // Do nothing
        }
        S_COUNT_OFFSET = sCountOffset;

        try {
            final SbFields sbFields = new SbFields();
            SB_COUNT = sbFields.sbCount;
            SB_VALUE_OFFSET = sbFields.sbValOffset;
            SB_COUNT_OFFSET = sbFields.sbCountOffset;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @NotNull
    private static UnsafeMemory getMemory() {
        return UnsafeMemory.INSTANCE;
    }

    /**
     * Compares a {@link StringBuilder} and a {@link CharSequence} for content equality.
     *
     * @param s  the {@link StringBuilder} to be compared.
     * @param cs the {@link CharSequence} to be compared.
     * @return {@code true} if both character sequences are equal, {@code false} otherwise.
     */
    public static boolean isEqual(@Nullable StringBuilder s, @Nullable CharSequence cs) {
        if (s == cs)
            return true;
        if (s == null) return false;
        if (cs == null) return false;
        int length = cs.length();
        if (s.length() != length) return false;

        return Bootstrap.isJava9Plus()
                ? isEqualJava9(s, cs, length)
                : isEqualJava8(s, cs, length);
    }

    /**
     * Sets the length of a {@link StringBuilder} without altering its content.
     * This operation has performance benefits compared to {@link StringBuilder#setLength(int)}.
     *
     * @param sb     the {@link StringBuilder} whose length needs to be set.
     * @param length the new length.
     * @throws AssertionError if there is an IllegalAccessException or IllegalArgumentException.
     */
    public static void setLength(@NotNull StringBuilder sb, int length) {
        try {
            SB_COUNT.set(sb, length);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets the content of a {@link StringBuilder} to the content of the given {@link CharSequence}.
     *
     * @param sb the {@link StringBuilder} to be modified.
     * @param cs the {@link CharSequence} whose content will be set in the {@link StringBuilder}.
     */
    public static void set(@NotNull StringBuilder sb, CharSequence cs) {
        sb.setLength(0);
        sb.append(cs);
    }

    /**
     * Checks if the given {@link CharSequence} ends with the specified string, in a case-insensitive way.
     *
     * @param source   the {@link CharSequence} to be checked.
     * @param endsWith the string to check if the {@link CharSequence} ends with.
     * @return {@code true} if the {@link CharSequence} ends with the specified string, {@code false} otherwise.
     */
    public static boolean endsWith(@NotNull final CharSequence source,
                                   @NotNull final String endsWith) {
        for (int i = 1; i <= endsWith.length(); i++) {
            if (toLowerCase(charAt(source, source.length() - i)) !=
                    toLowerCase(endsWith.charAt(endsWith.length() - i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given {@link CharSequence} starts with the specified string.
     *
     * @param source     the {@link CharSequence} to be checked.
     * @param startsWith the string to check if the {@link CharSequence} starts with.
     * @return {@code true} if the {@link CharSequence} starts with the specified string, {@code false} otherwise.
     */
    public static boolean startsWith(@NotNull final CharSequence source,
                                     @NotNull final String startsWith) {
        for (int i = 0; i < startsWith.length(); i++) {
            if (toLowerCase(charAt(source, i)) != toLowerCase(startsWith.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compares two {@link CharSequence}s for content equality.
     *
     * @param s  the first {@link CharSequence} to be compared.
     * @param cs the second {@link CharSequence} to be compared.
     * @return {@code true} if both character sequences are equal, {@code false} otherwise.
     */
    public static boolean isEqual(@Nullable CharSequence s, @Nullable CharSequence cs) {
        if (s instanceof StringBuilder) {
            return isEqual((StringBuilder) s, cs);
        }
        if (s == cs)
            return true;
        if (s == null) return false;
        if (cs == null) return false;
        int sLength = s.length();
        int csLength = cs.length();
        if (sLength != csLength) return false;
        for (int i = 0; i < csLength; i++)
            if (charAt(s, i) != charAt(cs, i))
                return false;
        return true;
    }

    private static char charAt(@NotNull CharSequence s, int i) {
        return s.charAt(i);
    }

    public static char[] extractChars(StringBuilder sb) {
        if (Bootstrap.isJava9Plus()) {
            final char[] data = new char[sb.length()];
            sb.getChars(0, sb.length(), data, 0);
            return data;
        }

        return getMemory().getObject(sb, SB_VALUE_OFFSET);
    }

    private static boolean isEqualJava8(@NotNull StringBuilder s, @NotNull CharSequence cs, int length) {
        char[] chars = StringUtils.extractChars(s);
        for (int i = 0; i < length; i++)
            if (chars[i] != charAt(cs, i))
                return false;
        return true;
    }

    private static boolean isEqualJava9(@NotNull StringBuilder s, @NotNull CharSequence cs, int length) {
        for (int i = 0; i < length; i++)
            // This is not as fast as it could be.
            if (s.charAt(i) != charAt(cs, i))
                return false;
        return true;
    }

    /**
     * Compares two {@link CharSequence}s for equality ignoring case considerations.
     *
     * @param s  the first {@link CharSequence} to be compared.
     * @param cs the second {@link CharSequence} to be compared.
     * @return {@code true} if the {@link CharSequence}s are equal irrespective of case, {@code false} otherwise.
     */
    public static boolean equalsCaseIgnore(@Nullable CharSequence s, @NotNull CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (Character.toLowerCase(charAt(s, i)) !=
                    Character.toLowerCase(charAt(cs, i)))
                return false;
        return true;
    }

    /**
     * Returns the string representation of the specified object.
     *
     * @param o the object whose string representation is to be returned.
     * @return the string representation of the specified object or null if the object is null.
     */
    @Nullable
    public static String toString(@Nullable Object o) {
        return o == null ? null : o.toString();
    }

    @Java9
    private static byte getStringCoderForStringOrStringBuilder(@NotNull CharSequence charSequence) {
        try {
            Field coder;
            if (charSequence instanceof String)
                coder = S_CODER;
            else if (charSequence instanceof StringBuilder)
                coder = SB_CODER;
            else
                coder = ClassUtil.getField0(charSequence.getClass(), CODER_FIELD_NAME, true);
            assert coder != null;
            return coder.getByte(charSequence);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Java9
    public static byte getStringCoder(@NotNull String str) {
        return getStringCoderForStringOrStringBuilder(str);
    }

    @Java9
    public static byte getStringCoder(@NotNull StringBuilder str) {
        return getStringCoderForStringOrStringBuilder(str);
    }

    @Java9
    public static byte[] extractBytes(@NotNull StringBuilder sb) {
        ensureJava9Plus();

        return getMemory().getObject(sb, SB_VALUE_OFFSET);
    }

    public static char[] extractChars(@NotNull String s) {
        if (Bootstrap.isJava9Plus()) {
            return s.toCharArray();
        }
        return getMemory().getObject(s, S_VALUE_OFFSET);
    }

    @Java9
    public static byte[] extractBytes(@NotNull String s) {
        ensureJava9Plus();

        return getMemory().getObject(s, S_VALUE_OFFSET);
    }

    public static void setCount(@NotNull StringBuilder sb, int count) {
        getMemory().writeInt(sb, SB_COUNT_OFFSET, count);
    }

    @NotNull
    public static String newString(char @NotNull [] chars) {
        if (Bootstrap.isJava9Plus()) {
            return new String(chars);
        }
        //noinspection RedundantStringConstructorCall
        @NotNull String str = new String();
        try {
            S_VALUE.set(str, chars);
            if (S_COUNT_OFFSET > -1)
                getMemory().writeInt(str, S_COUNT_OFFSET, chars.length);
            return str;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static void ensureJava9Plus() {
        if (!Bootstrap.isJava9Plus()) {
            throw new UnsupportedOperationException("This method is only supported on Java9+ runtimes");
        }
    }

    @Java9
    @NotNull
    public static String newStringFromBytes(byte @NotNull [] bytes) {
        ensureJava9Plus();
        //noinspection RedundantStringConstructorCall
        @NotNull String str = new String();
        try {
            S_VALUE.set(str, bytes);
            return str;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Nullable
    public static String firstLowerCase(@Nullable String str) {
        if (str == null || str.isEmpty())
            return str;
        final char ch = str.charAt(0);
        final char c2 = Character.toLowerCase(ch);
        return ch == c2 ? str : c2 + str.substring(1);
    }

    public static double parseDouble(@NotNull CharSequence in) {
        long value = 0;
        int exp = 0;
        boolean negative = false;
        int decimalPlaces = Integer.MIN_VALUE;

        int ch = charAt(in, 0);
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
                ch = charAt(in, pos++);
                break;
            default:
                // Continue below
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
            ch = charAt(in, pos++);
        }

        if (decimalPlaces < 0)
            decimalPlaces = 0;

        return Maths.asDouble(value, exp, negative, decimalPlaces);
    }

    private static final class SbFields {

        private Field sbValue;
        private long sbValOffset;
        private Field sbCount;
        private long sbCountOffset;

        public SbFields() throws ClassNotFoundException, NoSuchFieldException {
            try {
                sbValue = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField(VALUE_FIELD_NAME);
                ClassUtil.setAccessible(sbValue);
                sbValOffset = getMemory().getFieldOffset(sbValue);
                sbCount = Class.forName("java.lang.AbstractStringBuilder").getDeclaredField(COUNT_FIELD_NAME);
                ClassUtil.setAccessible(sbCount);
                sbCountOffset = getMemory().getFieldOffset(sbCount);
            } catch (NoSuchFieldException e) {
                sbValue = Class.forName("java.lang.StringBuilder").getDeclaredField(VALUE_FIELD_NAME);
                ClassUtil.setAccessible(sbValue);
                sbValOffset = getMemory().getFieldOffset(sbValue);
                sbCount = Class.forName("java.lang.StringBuilder").getDeclaredField(COUNT_FIELD_NAME);
                ClassUtil.setAccessible(sbCount);
                sbCountOffset = getMemory().getFieldOffset(sbCount);
            }
        }
    }

    private static boolean compareRest(@NotNull CharSequence in,
                                       final int pos,
                                       @NotNull String s) {

        if (s.length() > in.length() - pos)
            return false;

        for (int i = 0; i < s.length(); i++) {
            if (charAt(in, i + pos) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts the given string to title case. It will capitalize the first letter
     * of the string and then replace spaces with underscores and adjust casing for subsequent characters.
     *
     * @param name the input string to be converted.
     * @return the converted string in title case with underscores, or null if the input is null.
     */
    @Nullable
    public static String toTitleCase(@Nullable String name) {
        if (name == null || name.isEmpty())
            return name;
        @NotNull StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(name.charAt(0)));
        boolean wasUnder = false;
        for (int i = 1; i < name.length(); i++) {
            char ch0 = name.charAt(i);
            char ch1 = i + 1 < name.length() ? name.charAt(i + 1) : ' ';
            if (Character.isLowerCase(ch0)) {
                sb.append(Character.toUpperCase(ch0));
                if (Character.isUpperCase(ch1)) {
                    sb.append('_');
                    wasUnder = true;
                } else {
                    wasUnder = false;
                }
            } else if (Character.isUpperCase(ch0)) {
                if (!wasUnder && Character.isLowerCase(ch1)) {
                    sb.append('_');
                }
                sb.append(ch0);
                wasUnder = false;
            } else {
                sb.append(ch0);
                wasUnder = ch0 == '_';
            }
        }
        return sb.toString();
    }

    /**
     * Reverses a portion of the content of a {@link StringBuilder} in place, from the given start index to the end.
     *
     * @param text  the {@link StringBuilder} whose content needs to be partially reversed.
     * @param start the index from which to start the reverse operation.
     */
    public static void reverse(StringBuilder text, int start) {
        int end = text.length() - 1;
        int mid = (start + end + 1) / 2;
        for (int i = 0; i < mid - start; i++) {
            char ch = text.charAt(start + i);
            text.setCharAt(start + i, text.charAt(end - i));
            text.setCharAt(end - i, ch);
        }
    }

    public static int parseInt(CharSequence s, int radix)
            throws NumberFormatException {
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw forRadix(radix, true);
        }

        if (radix > Character.MAX_RADIX) {
            throw forRadix(radix, false);
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = charAt(s, 0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(charAt(s, i++), radix);
                if (digit < 0) {
                    throw forInputString(s);
                }
                if (result < multmin) {
                    throw forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw forInputString(s);
        }
        return negative ? result : -result;
    }

    /**
     * Constructs a {@link NumberFormatException} with the message "For input string: '[input]'"
     * where [input] is the content of the input CharSequence.
     *
     * @param s the input CharSequence that was attempted to be parsed.
     * @return a {@link NumberFormatException} with a detail message.
     */
    static NumberFormatException forInputString(CharSequence s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }

    /**
     * Constructs a {@link NumberFormatException} with a message indicating that the given
     * radix is either less than {@link Character#MIN_RADIX} or greater than {@link Character#MAX_RADIX}.
     *
     * @param radix the radix value.
     * @param less  if true, indicates that the radix is less than {@link Character#MIN_RADIX},
     *              otherwise it indicates that the radix is greater than {@link Character#MAX_RADIX}.
     * @return a {@link NumberFormatException} with a detail message.
     */
    static NumberFormatException forRadix(int radix, boolean less) {
        if (less)
            return new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        else
            return new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
    }

    public static long parseLong(CharSequence s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw forRadix(radix, true);
        }
        if (radix > Character.MAX_RADIX) {
            throw forRadix(radix, false);
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = charAt(s, 0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(charAt(s, i++), radix);
                if (digit < 0) {
                    throw forInputString(s);
                }
                if (result < multmin) {
                    throw forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw forInputString(s);
        }
        return negative ? result : -result;
    }
}
