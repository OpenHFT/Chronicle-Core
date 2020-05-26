/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
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

public class EnumInterner<E extends Enum<E>> {
    public static final ClassLocal<EnumInterner> ENUM_INTERNER = ClassLocal.withInitial(EnumInterner::create);

    @NotNull
    private final E[] interner;

    private final int mask;
    private final EnumCache<E> enumCache;

    public EnumInterner(Class<E> eClass) {
        this(eClass, 64);
    }

    public EnumInterner(Class<E> eClass, int capacity) {
        int n = Maths.nextPower2(capacity, 16);
        interner = (E[]) new Enum[n];
        mask = n - 1;
        enumCache = EnumCache.of(eClass);
    }

    // bridging method to fix the types.
    @NotNull
    static <V extends Enum<V>> EnumInterner<V> create(Class<?> aClass) {
        @NotNull @SuppressWarnings("unchecked")
        Class<V> vClass = (Class<V>) aClass;
        return new EnumInterner<>(vClass);
    }

    public E intern(@NotNull CharSequence cs) {
        int h = Maths.hash32(cs) & mask;
        E e = interner[h];
        if (e != null && StringUtils.isEqual(e.name(), cs))
            return e;
        @NotNull String s2 = cs.toString();
        E value = enumCache.valueOf(s2);
        interner[h] = value;
        return interner[h];
    }
}
