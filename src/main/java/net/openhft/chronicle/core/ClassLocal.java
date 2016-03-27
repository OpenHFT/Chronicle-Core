/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import java.util.function.Function;

/**
 * Lambda friendly, ClassLocal value to cache information relating to a class.
 *
 * @param <V> the type of value in this ClassLocal
 */
public class ClassLocal<V> extends ClassValue<V> {
    private final Function<Class, V> classVFunction;

    private ClassLocal(Function<Class, V> classVFunction) {
        this.classVFunction = classVFunction;
    }

    /**
     * Function to create a value to cache information associated with a Class
     *
     * @param classVFunction to generate the associated value.
     * @param <V>            the type of value in this ClassLocal
     * @return the ClassLocal
     */
    public static <V> ClassLocal<V> withInitial(Function<Class, V> classVFunction) {
        return new ClassLocal<>(classVFunction);
    }

    @Override
    protected V computeValue(Class<?> type) {
        return classVFunction.apply(type);
    }
}
