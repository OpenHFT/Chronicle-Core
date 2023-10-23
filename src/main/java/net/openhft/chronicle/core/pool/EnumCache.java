/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassLocal;
import net.openhft.chronicle.core.util.CoreDynamicEnum;

import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for caching and efficient access to enum-like instances. This class
 * supports both traditional static enums, and dynamic enums which allow the creation
 * of enum instances at runtime.
 *
 * <p>Usage of this class provides efficient access to enum instances via name or index,
 * and also provides convenience methods for creating maps and sets that use enum instances
 * as keys or elements.
 *
 * @param <E> the type of enum instances this cache will manage.
 *            Example usage:
 *            <pre>
 *            {@code
 *            EnumCache<MyEnum> myEnumCache = EnumCache.of(MyEnum.class);
 *            MyEnum someValue = myEnumCache.valueOf("SOME_VALUE");
 *            }
 *            </pre>
 */
public abstract class EnumCache<E> {

    private static final ClassLocal<EnumCache<?>> ENUM_CACHE_CL = ClassLocal.withInitial(EnumCache::createFromUnknownClass);

    protected final Class<E> type;

    /**
     * Constructs an EnumCache for the specified enum type.
     *
     * @param type the class of the enum this cache will manage.
     */
    protected EnumCache(Class<E> type) {
        this.type = type;
    }

    /**
     * Retrieves an EnumCache instance for the specified enum class.
     *
     * @param eClass the class of the enum.
     * @param <E>    the type of the enum.
     * @return an EnumCache instance for the specified enum class.
     */
    @SuppressWarnings("unchecked")
    public static <E> EnumCache<E> of(Class<E> eClass) {
        return (EnumCache<E>) ENUM_CACHE_CL.get(eClass);
    }

    /**
     * Returns the enum instance with the specified name.
     *
     * @param name the name of the enum instance to be returned.
     * @return the enum instance with the specified name.
     */
    public E get(String name) {
        return valueOf(name);
    }

    /**
     * Returns the enum instance with the specified name.
     *
     * @param name the name of the enum instance to be returned.
     * @return the enum instance with the specified name.
     */
    public abstract E valueOf(String name);

    /**
     * Returns the total number of enum instances managed by this cache.
     *
     * @return the number of enum instances.
     */
    public abstract int size();

    /**
     * Returns the class of the enum type this cache is managing.
     *
     * @return the enum class.
     */
    public Class<?> type() {
        return type;
    }

    /**
     * Retrieves the enum instance at the given ordinal index.
     *
     * @param index the ordinal index of the enum instance to retrieve.
     * @return the enum instance at the given index.
     */
    public abstract E forIndex(int index);

    /**
     * Returns an array containing all the enum instances managed by this cache.
     *
     * @return an array of all enum instances.
     */
    public abstract E[] asArray();

    /**
     * Creates a map where the keys are enum instances.
     *
     * @param <T> the type of the map values.
     * @return a map with enum instances as keys.
     */
    public abstract <T> Map<E, T> createMap();

    /**
     * Creates a set for holding enum instances.
     *
     * @return a set of enum instances.
     */
    public abstract Set<E> createSet();

    @SuppressWarnings("unchecked")
    private static <E, D extends CoreDynamicEnum<D>, S extends Enum<S> & CoreDynamicEnum<S>> EnumCache<E> createFromUnknownClass(Class<E> eClass) {
        return (EnumCache<E>) (CoreDynamicEnum.class.isAssignableFrom(eClass)
                ? new DynamicEnumClass<>((Class<D>) eClass)
                : new StaticEnumClass<>((Class<S>) eClass));
    }

}
