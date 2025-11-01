/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.lang.reflect.Type;

/**
 * The UnresolvedType class represents an unresolved type.
 * It implements the Type interface.
 */
public class UnresolvedType implements Type {
    private final String typeName;

    /**
     * Constructs an UnresolvedType with the specified type name.
     *
     * @param typeName the name of the unresolved type
     */
    protected UnresolvedType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Creates a new instance of UnresolvedType with the specified type name.
     *
     * @param typeName the name of the unresolved type
     * @return the created UnresolvedType instance
     */
    public static Type of(String typeName) {
        return new UnresolvedType(typeName);
    }

    /**
     * Returns the name of the unresolved type.
     *
     * @return the name of the unresolved type
     */
    @Override
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns a string representation of the unresolved type.
     *
     * @return a string representation of the unresolved type
     */
    @Override
    public String toString() {
        return typeName;
    }
}
