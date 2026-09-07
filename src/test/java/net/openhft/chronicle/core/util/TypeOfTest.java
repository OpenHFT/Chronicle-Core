/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class TypeOfTest extends CoreTestCommon {

    @Test
    <T extends Number> void type() {
        assertEquals("java.util.List<?>", new TypeOf<List<?>>() {
        }.type().toString());
        assertEquals("java.util.List<java.lang.String>", new TypeOf<List<String>>() {
        }.type().toString());
        assertEquals("java.util.List<T>", new TypeOf<List<T>>() {
        }.type().toString());
        assertEquals("java.util.function.BiFunction<java.util.List<java.lang.String>, java.lang.Integer, java.lang.String>", new TypeOf<BiFunction<List<String>, Integer, String>>() {
        }.type().toString());
    }
}
