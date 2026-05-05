/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Canonical invalid-input suite for {@link Jvm#parseSize(String)} and
 * {@link Jvm#getSize(String, long)}. Blank strings are treated as invalid
 * input rather than as silently unset values; only an absent property short-
 * circuits to the default in {@link Jvm#getSize(String, long)}.
 */
class JvmParseSizeInvalidTest extends CoreTestCommon {
    private static final String PROPERTY = "JvmParseSizeInvalidTest";
    private static final long KIB = 1L << 10;
    private static final long MIB = 1L << 20;
    private static final long GIB = 1L << 30;
    private static final long TIB = 1L << 40;
    private static final long DEFAULT_VALUE = -1L;

    static Collection<String> data() {
        // Long.MAX_VALUE + 1, written as the unsigned decimal string.
        final String overMax = "9223372036854775808";
        return Arrays.asList(
                "",
                "     ",
                "10XB",
                "iB",
                "IB",
                "-3",
                "-3MB",
                overMax,
                overMax + "B",
                (Long.MAX_VALUE / KIB + 1) + "kB",
                (Long.MAX_VALUE / MIB + 1) + "mB",
                (Long.MAX_VALUE / GIB + 1) + "gB",
                (Long.MAX_VALUE / TIB + 1) + "tB");
    }

    @AfterEach
    void teardown() {
        System.getProperties().remove(PROPERTY);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("data")
    void parseSizeRejectsInvalidInput(String text) {
        assertThrows(IllegalArgumentException.class, () -> Jvm.parseSize(text));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("data")
    void getSizeFallsBackForInvalidInput(String text) {
        expectException("Unable to parse the property " + PROPERTY + " as a size");
        System.setProperty(PROPERTY, text);
        assertEquals(DEFAULT_VALUE, Jvm.getSize(PROPERTY, DEFAULT_VALUE));
    }
}
