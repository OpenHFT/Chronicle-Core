/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeOfTest extends CoreTestCommon {

    @DisplayName("TypeOf captures wildcard and nested generic types")
    @Test
    <T extends Number> void type() {
        assertEquals("java.util.List<?>", new TypeOf<List<?>>() {
        }.type().toString(), "TypeOf should capture wildcard type as List<?>");
        assertEquals("java.util.List<java.lang.String>", new TypeOf<List<String>>() {
        }.type().toString(), "TypeOf should capture concrete generic type as List<String>");
        assertEquals("java.util.List<T>", new TypeOf<List<T>>() {
        }.type().toString(), "TypeOf should capture type variable as List<T>");
        assertEquals("java.util.function.BiFunction<java.util.List<java.lang.String>, java.lang.Integer, java.lang.String>", new TypeOf<BiFunction<List<String>, Integer, String>>() {
        }.type().toString(), "TypeOf should capture complex nested generic types with full qualification");
    }
}
