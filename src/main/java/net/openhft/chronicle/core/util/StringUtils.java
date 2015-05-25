package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.toLowerCase;

/**
 * Created by Rob Austin
 */
public enum StringUtils {
    ;

    public static String[] split2(String str, int ch) {
        int pos = str.indexOf(ch);
        return pos >= 0
                ? new String[]{str.substring(0, pos), str.substring(pos + 1)}
                : new String[]{str, ""};
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

    public static boolean contains(@NotNull final CharSequence source,
                                   @NotNull final String token) {
        OUTER:
        for (int sourceI = 0; sourceI <= source.length() - token.length(); sourceI++) {
            int lastSourceI = sourceI;
            for (int tokenI = 0; tokenI < token.length(); tokenI++) {
                if (toLowerCase(source.charAt(sourceI)) != toLowerCase(token.charAt(tokenI))) {
                    sourceI = lastSourceI;
                    continue OUTER;
                }
                sourceI++;
            }
            return true;
        }
        return false;
    }

    public static boolean isEqual(CharSequence s, CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (s.charAt(i) != cs.charAt(i))
                return false;
        return true;
    }

    public static String toString(Object o) {
        return o == null ? null : o.toString();
    }
}
