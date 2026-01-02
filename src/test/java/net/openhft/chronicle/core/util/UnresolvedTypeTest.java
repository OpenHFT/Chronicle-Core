/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class UnresolvedTypeTest {

    @Test
    @DisplayName("Constructor should initialize type name unresolved")
    void constructorShouldInitializeTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName(), "constructor should initialize type name to provided value");
    }

    @Test
    @DisplayName("Factory method should create unresolved type")
    void factoryMethodShouldCreateUnresolvedType() {
        String expectedTypeName = "MyType";
        Type type = UnresolvedType.of(expectedTypeName);

        assertInstanceOf(UnresolvedType.class, type, "factory method should return UnresolvedType instance");
        assertEquals(expectedTypeName, type.getTypeName(), "factory method should create UnresolvedType with correct type name");
    }

    @Test
    @DisplayName("Get type name should return correct type name unresolved")
    void getTypeNameShouldReturnCorrectTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName(), "getTypeName should return the type name provided at construction");
    }

    @Test
    @DisplayName("To string should return type name unresolved")
    void toStringShouldReturnTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.toString(), "toString should return the type name");
    }
}
