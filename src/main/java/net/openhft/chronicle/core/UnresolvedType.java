/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core;

import java.lang.reflect.Type;

public class UnresolvedType implements Type {
    private final String typeName;

    protected UnresolvedType(String typeName) {
        this.typeName = typeName;
    }

    public static Type of(String typeName) {
        return new UnresolvedType(typeName);
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
