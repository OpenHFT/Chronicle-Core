/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Canonical invalid-input suite for {@link Jvm#parseSize(String)} and
 * {@link Jvm#getSize(String, long)}. Blank strings are treated as invalid
 * input rather than as silently unset values; only an absent property short-
 * circuits to the default in {@link Jvm#getSize(String, long)}.
 */
@RunWith(Parameterized.class)
public class JvmParseSizeInvalidTest extends CoreTestCommon {
    private static final String PROPERTY = "JvmParseSizeInvalidTest";
    private static final long KIB = 1L << 10;
    private static final long MIB = 1L << 20;
    private static final long GIB = 1L << 30;
    private static final long TIB = 1L << 40;
    private static final long DEFAULT_VALUE = -1L;

    private final String text;

    @SuppressWarnings("unused")
    public JvmParseSizeInvalidTest(String text) {
        this.text = text;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // Long.MAX_VALUE + 1, written as the unsigned decimal string.
        final String overMax = "9223372036854775808";
        return Arrays.asList(new Object[][]{
                {""},
                {"     "},
                {"10XB"},
                {"iB"},
                {"IB"},
                {"-3"},
                {"-3MB"},
                {overMax},
                {overMax + "B"},
                {(Long.MAX_VALUE / KIB + 1) + "kB"},
                {(Long.MAX_VALUE / MIB + 1) + "mB"},
                {(Long.MAX_VALUE / GIB + 1) + "gB"},
                {(Long.MAX_VALUE / TIB + 1) + "tB"},
        });
    }

    @After
    public void teardown() {
        System.getProperties().remove(PROPERTY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSizeRejectsInvalidInput() {
        Jvm.parseSize(text);
    }

    @Test
    public void getSizeFallsBackForInvalidInput() {
        expectException("Unable to parse the property " + PROPERTY + " as a size");
        System.setProperty(PROPERTY, text);
        assertEquals(DEFAULT_VALUE, Jvm.getSize(PROPERTY, DEFAULT_VALUE));
    }
}
