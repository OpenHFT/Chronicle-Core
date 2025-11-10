//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Lambda friendly, ClassLocal value to cache information relating to a class.
 *
 * @param <V> the type of value in this ClassLocal
 */
public class ClassLocal<V> extends ClassValue<V> {
    private final Function<Class<?>, V> classVFunction;

    private ClassLocal(Function<Class<?>, V> classVFunction) {
        this.classVFunction = classVFunction;
    }

    /**
     * Function to create a value to cache information associated with a Class
     *
     * @param classVFunction to generate the associated value.
     * @param <V>            the type of value in this ClassLocal
     * @return the ClassLocal
     */
    @NotNull
    public static <V> ClassLocal<V> withInitial(Function<Class<?>, V> classVFunction) {
        return new ClassLocal<>(classVFunction);
    }

    /**
     * WARNING Do not call this directly
     */
    @Override
    protected V computeValue(Class<?> type) {
        return classVFunction.apply(type);
    }
}
