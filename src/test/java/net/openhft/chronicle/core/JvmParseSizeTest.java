/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Canonical test suite for size parsing and retrieval via {@link Jvm#parseSize(String)} and
 * {@link Jvm#getSize(String, long)}. Keep related assertions here to avoid duplication.
 */
public class JvmParseSizeTest extends CoreTestCommon {
    private static final String PROPERTY = "JvmParseSizeTest";

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("100", 100L),
                Arguments.of("  100  ", 100L),
                Arguments.of("100b", 100L),
                Arguments.of("100B", 100L),
                Arguments.of("0.5kb", 512L),
                Arguments.of("0.5KiB", 512L),
                Arguments.of("0.125MB", 128L << 10),
                Arguments.of("2M", 2L << 20),
                Arguments.of(" 2 M", 2L << 20),
                Arguments.of("0.75GiB", 768L << 20),
                Arguments.of("1.5 GiB", 1536L << 20),
                Arguments.of("0.001TiB", Math.round((1L << 40) / 1000.0))
        );
    }

    @AfterEach
    public void teardown() {
        System.getProperties().remove(PROPERTY);
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("data")
    public void parseSize(String text, long value) throws IllegalArgumentException {
        assertEquals(value, Jvm.parseSize(text), "parseSize: L57");
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("data")
    public void getSize(String text, long value) {
        System.setProperty(PROPERTY, text);
        assertEquals(value, Jvm.getSize(PROPERTY, -1), "getSize: L63");
    }

    @Test
    public void parseSizeRejectsUnknownSuffix() {
        assertThrows(IllegalArgumentException.class, () -> Jvm.parseSize("10XB"), "parseSizeRejectsUnknownSuffix");
    }
}
