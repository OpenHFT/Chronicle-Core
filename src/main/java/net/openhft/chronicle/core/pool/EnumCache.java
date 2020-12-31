/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.util.CoreDynamicEnum;

public abstract class EnumCache<E> {
    private static final ClassLocal<EnumCache> ENUM_CACHE_CL = ClassLocal.withInitial(
            eClass -> CoreDynamicEnum.class.isAssignableFrom(eClass)
                    ? new DynamicEnumClass(eClass)
                    : new StaticEnumClass(eClass));
    protected final Class<E> type;

    protected EnumCache(Class<E> type) {
        this.type = type;
    }

    public static <E> EnumCache<E> of(Class<E> eClass) {
        return ENUM_CACHE_CL.get(eClass);
    }

    public E get(String name) {
        return valueOf(name);
    }

    public abstract E valueOf(String name);

    public abstract int size();

    public Class<?> type() {
        return type;
    }

    public abstract E forIndex(int index);

    public abstract E[] asArray();
}
