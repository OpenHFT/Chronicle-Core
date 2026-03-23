/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Canonical test suite for size parsing and retrieval via {@link Jvm#parseSize(String)} and
 * {@link Jvm#getSize(String, long)}. Keep related assertions here to avoid duplication.
 */
public class JvmParseSizeTest extends CoreTestCommon {
    private static final String PROPERTY = "JvmParseSizeTest";

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

    @AfterEach
    public void teardown() {
        System.getProperties().remove(PROPERTY);
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("data")
    public void parseSize(String text, long value) throws IllegalArgumentException {
        assertEquals(value, Jvm.parseSize(text));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("data")
    public void getSize(String text, long value) {
        System.setProperty(PROPERTY, text);
        assertEquals(value, Jvm.getSize(PROPERTY, -1));
    }

    @Test
    public void parseSizeRejectsUnknownSuffix() {
        assertThrows(IllegalArgumentException.class, () -> {
            Jvm.parseSize("10XB");
        });
    }
}
