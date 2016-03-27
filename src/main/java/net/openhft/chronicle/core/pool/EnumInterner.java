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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;

/**
 * @author peter.lawrey
 */
public class EnumInterner<E extends Enum<E>> {
    public static final ClassLocal<EnumInterner> ENUM_INTERNER =
            ClassLocal.withInitial(c -> new EnumInterner<>(c));
    private final E[] interner;
    private final int mask;
    private final Class<E> eClass;

    public EnumInterner(Class<E> eClass) {
        this(eClass, 64);
    }

    public EnumInterner(Class<E> eClass, int capacity) {
        this.eClass = eClass;
        int n = Maths.nextPower2(capacity, 16);
        interner = (E[]) new Enum[n];
        mask = n - 1;
    }

    public E intern(CharSequence cs) {
        int h = Maths.hash32(cs) & mask;
        E e = interner[h];
        if (e != null && StringUtils.isEqual(e.name(), cs))
            return e;
        String s2 = cs.toString();
        interner[h] = Enum.valueOf(eClass, s2);
        
        return interner[h];
    }
}
