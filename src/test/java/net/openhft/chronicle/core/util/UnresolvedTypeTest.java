/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class UnresolvedTypeTest {

    @DisplayName("constructorShouldInitializeTypeName behaviour under expected input and output conditions")
    @Test
    void constructorShouldInitializeTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName(), "constructor should initialize type name to provided value");
    }

    @DisplayName("factoryMethodShouldCreateUnresolvedType behaviour under expected input and output conditions")
    @Test
    void factoryMethodShouldCreateUnresolvedType() {
        String expectedTypeName = "MyType";
        Type type = UnresolvedType.of(expectedTypeName);

        assertInstanceOf(UnresolvedType.class, type, "factory method should return UnresolvedType instance");
        assertEquals(expectedTypeName, type.getTypeName(), "factory method should create UnresolvedType with correct type name");
    }

    @DisplayName("getTypeNameShouldReturnCorrectTypeName behaviour under expected input and output conditions")
    @Test
    void getTypeNameShouldReturnCorrectTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName(), "getTypeName should return the type name provided at construction");
    }

    @DisplayName("toStringShouldReturnTypeName behaviour under expected input and output conditions")
    @Test
    void toStringShouldReturnTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.toString(), "toString should return the type name");
    }
}
