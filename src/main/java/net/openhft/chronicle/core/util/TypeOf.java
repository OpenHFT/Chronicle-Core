/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for capturing and retaining a generic type.
 *
 * <p> This class is intended to be subclassed and allows for type information
 * to be retained at runtime. When subclassing, the generic type parameter should be specified.
 *
 * <p> Example usage:
 * <pre>
 * TypeOf&lt;List&lt;String&gt;&gt; typeToken = new TypeOf&lt;List&lt;String&gt;&gt;(){};
 * Type type = typeToken.type();
 * </pre>
 *
 * @param <T> captured type
 */
public class TypeOf<T> {

    private final Type type = extractType();

    /**
     * Constructs a new type token retaining its generic parameter.
     */
    protected TypeOf() {
    }

    /**
     * Retrieves the captured type.
     *
     * @return The captured type as a {@link Type}.
     */
    public Type type() {
        return type;
    }

    /**
     * Extracts the type information by inspecting the class's generic superclass.
     *
     * @return The captured type as a {@link Type}.
     * @throws RuntimeException if the class does not have type parameters or does not directly extend TypeOf.
     */
    private Type extractType() {
        Type t = getClass().getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            throw new RuntimeException("must specify type parameters");
        }
        ParameterizedType pt = (ParameterizedType) t;
        if (pt.getRawType() != TypeOf.class) {
            throw new RuntimeException("must directly extend TypeOf");
        }
        return pt.getActualTypeArguments()[0];
    }
}
