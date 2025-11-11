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
                {"0.5kb", 512L},
                {"0.5KiB", 512L},
                {"0.125MB", 128L << 10},
                {"2M", 2L << 20},
                {" 2 M", 2L << 20},
                {"0.75GiB", 768L << 20},
                {"1.5 GiB", 1536L << 20},
                {"0.001TiB", Math.round((1L << 40) / 1000.0)}
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

    @Test(expected = IllegalArgumentException.class)
    public void parseSizeRejectsUnknownSuffix() {
        Jvm.parseSize("10XB");
    }
}
