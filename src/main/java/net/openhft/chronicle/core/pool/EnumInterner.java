/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
/**
 * This class represents a cache for enum values to improve performance in scenarios where the same enum values
 * are frequently looked up by name. The class is generic, so it can be used with any enum type.
 *
 * @param <E> the type of the enum
 */
public class EnumInterner<E extends Enum<E>> {

    /**
     * A cache for EnumInterner instances. Each class will have its own EnumInterner.
     */
    public static final ClassLocal<EnumInterner<?>> ENUM_INTERNER = ClassLocal.withInitial(EnumInterner::create);

    @NotNull
    private final E[] interner;

    private final int mask;
    private final EnumCache<E> enumCache;

    /**
     * Constructs a new EnumInterner with a default capacity.
     *
     * @param eClass the enum class
     * @throws IllegalArgumentException If an illegal argument is provided
     */
    public EnumInterner(Class<E> eClass) throws IllegalArgumentException {
        this(eClass, 64);
    }

    /**
     * Constructs a new EnumInterner with a specified capacity.
     *
     * @param eClass   the enum class
     * @param capacity the initial capacity of the EnumInterner
     * @throws IllegalArgumentException If an illegal argument is provided
     */
    public EnumInterner(Class<E> eClass, int capacity) throws IllegalArgumentException {
        enumCache = EnumCache.of(eClass);
        int initialSize = enumCache.size() * 3 / 2;
        int n = Maths.nextPower2(Math.max(initialSize, capacity), 16);
        interner = (E[]) new Enum[n];
        mask = n - 1;
    }

    /**
     * Creates an EnumInterner for a specified class.
     *
     * @param aClass the class for which to create the EnumInterner
     * @return an EnumInterner for the specified class
     * @throws AssertionError if there is an error during the creation
     */
    @NotNull
    static <V extends Enum<V>> EnumInterner<V> create(Class<?> aClass) {
        @NotNull @SuppressWarnings("unchecked")
        Class<V> vClass = (Class<V>) aClass;
        return new EnumInterner<>(vClass);
    }

    /**
     * Looks up an enum value by its name. If the value is not yet in the cache, it is added.
     * If the value is already in the cache, the cached value is returned.
     *
     * @param cs the name of the enum value
     * @return the enum value corresponding to the given name
     */
    public E intern(@NotNull CharSequence cs) {
        long h1 = Maths.hash64(cs);
        h1 ^= h1 >> 32;
        int h = (int) h1 & mask;
        E e = interner[h];
        if (e != null && StringUtils.isEqual(e.name(), cs))
            return e;
        @NotNull String s2 = cs.toString();
        E value = enumCache.valueOf(s2);
        interner[h] = value;
        return value;
    }
}
