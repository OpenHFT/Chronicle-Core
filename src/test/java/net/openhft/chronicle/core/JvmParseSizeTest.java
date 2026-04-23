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
 * Canonical test suite for size parsing and retrieval via {@link Jvm#parseSize(String)} and
 * {@link Jvm#getSize(String, long)}. Keep related assertions here to avoid duplication.
 */
@RunWith(Parameterized.class)
public class JvmParseSizeTest extends CoreTestCommon {
    private static final String PROPERTY = "JvmParseSizeTest";
    private static final long KIB = 1L << 10;
    private static final long MIB = 1L << 20;
    private static final long GIB = 1L << 30;
    private static final long TIB = 1L << 40;
    private final String text;
    private final long value;

    @SuppressWarnings("unused")
    public JvmParseSizeTest(String text, long value) {
        this.text = text;
        this.value = value;
    }

    @Parameterized.Parameters(name = "{0} => {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"100", 100L},
                {"  100  ", 100L},
                {"100b", 100L},
                {"100B", 100L},
                {" 100B ", 100L},
                {"0.5kb", 512L},
                {"0.5KiB", 512L},
                {"0.125MB", 128L << 10},
                {"2M", 2L << 20},
                {" 2 M", 2L << 20},
                {"0.75GiB", 768L << 20},
                {"1.5 GiB", 1536L << 20},
                {"0.25TiB", Math.round((1L << 40) / 4.0)},
                {"0", 0},
                {"-0.0GB", 0}, // BigDecimal("-0.0").longValue() == 0, so the negative-size guard is not triggered
                {Long.MAX_VALUE + "", Long.MAX_VALUE},
                {Long.MAX_VALUE / KIB + "kb", Long.MAX_VALUE / KIB * KIB},
                {Long.MAX_VALUE / MIB + "mb", Long.MAX_VALUE / MIB * MIB},
                {Long.MAX_VALUE / GIB + "gb", Long.MAX_VALUE / GIB * GIB},
                {Long.MAX_VALUE / TIB + "tb", Long.MAX_VALUE / TIB * TIB},
        });
    }

    @After
    public void teardown() {
        System.getProperties().remove(PROPERTY);
    }

    @Test
    public void parseSize() throws IllegalArgumentException {
        assertEquals(value, Jvm.parseSize(text));
    }

    @Test
    public void getSize() {
        System.setProperty(PROPERTY, text);
        assertEquals(value, Jvm.getSize(PROPERTY, -1));
    }
}
