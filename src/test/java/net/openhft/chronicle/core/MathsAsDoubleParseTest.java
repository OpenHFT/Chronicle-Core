/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for {@link Maths#asDouble(long, int, boolean, int)}.
 * <p>
 * {@code BytesInternal.parseDouble} feeds asDouble the integer mantissa, the binary shed-exponent
 * (0 while the mantissa fits in a long), and the net decimal places. asDouble previously
 * reconstructed {@code value * 10^-decimalPlaces} with a {@code 5^k} integer fixup that is not
 * correctly-rounded, so finite decimals of as few as ~10-15 significant digits could read back one
 * ULP off the correctly-rounded result. These cases were surfaced by JSONWire/TextWire round-trips
 * in {@code [1e-3, 1e15)} and failed when parsed through chronicle-bytes; they now pass with the
 * Clinger fast path in asDouble.
 */
class MathsAsDoubleParseTest extends CoreTestCommon {

    /**
     * Mirror of {@code BytesInternal.parseDouble}'s argument extraction for inputs whose integer
     * mantissa fits in a long (so the binary shed-exponent stays 0): parse the digits into a long,
     * count the net decimal places, then call the same {@link Maths#asDouble} the reader calls.
     */
    private static double asDoubleAsBytesWould(String s) {
        boolean negative = s.charAt(0) == '-';
        String t = (negative || s.charAt(0) == '+') ? s.substring(1) : s;

        int e = t.indexOf('E');
        if (e < 0) e = t.indexOf('e');
        int tens = 0;
        if (e >= 0) {
            tens = Integer.parseInt(t.substring(e + 1));
            t = t.substring(0, e);
        }
        int dot = t.indexOf('.');
        int fractionDigits = dot < 0 ? 0 : t.length() - dot - 1;
        long value = Long.parseLong(t.replace(".", ""));
        int decimalPlaces = fractionDigits - tens;

        return Maths.asDouble(value, 0, negative, decimalPlaces);
    }

    private static void assertParsedFaithfully(String s) {
        double expected = Double.parseDouble(s);
        double actual = asDoubleAsBytesWould(s);
        assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(actual),
                () -> "asDouble(\"" + s + "\")=" + actual + " must equal the correctly-rounded "
                        + expected + " (off by " + ulps(actual, expected) + " ULP)");
    }

    /**
     * Concrete values that read back 1 ULP off through chronicle-bytes before the fix (each has an
     * integer mantissa <= 2^53 and a small decimal exponent, so the fast path now applies).
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "-6.1625505370891E11",      // 14 significant digits
            "-1.05056753113792E12",     // 15 significant digits
            "2.116299837525985E12",     // 16 significant digits
            "5238753360239.026",        // 16 significant digits, plain decimal form
            "3.447381479371051E13",     // 16 significant digits
    })
    void previouslyOneUlpOffValuesNowParseExactly(String s) {
        assertParsedFaithfully(s);
    }

    /** Magnitudes outside [1e-3, 1e15) use the fast path too while operands stay exact. */
    @ParameterizedTest
    @MethodSource("assortedExactDecimals")
    void assortedDecimalsParseExactly(String s) {
        assertParsedFaithfully(s);
    }

    private static Stream<String> assortedExactDecimals() {
        return Stream.of(
                "0.1", "0.2", "0.3", "0.001", "1.5", "100.25",
                "0.0012345678901234",       // small magnitude, 14 sig digits
                "-0.00098765432109876",     // small magnitude, negative
                "123456789012.345",         // 15 sig digits
                "9.99999999999999E11");     // 15 sig digits
    }

    private static long ulps(double a, double b) {
        long la = Double.doubleToLongBits(a);
        long lb = Double.doubleToLongBits(b);
        if (la < 0) la = 0x8000000000000000L - la;
        if (lb < 0) lb = 0x8000000000000000L - lb;
        return Math.abs(la - lb);
    }
}
