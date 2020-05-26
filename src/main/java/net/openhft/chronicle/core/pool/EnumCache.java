/*
 * Copyright 2016-2020 Chronicle Software
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

public abstract class EnumCache<E extends Enum<E>> {
    private static final ClassLocal<EnumCache> ENUM_CACHE_CL = ClassLocal.withInitial(
            eClass -> DynamicEnum.class.isAssignableFrom(eClass)
                    ? new DynamicEnumClass(eClass)
                    : new StaticEnumClass(eClass));
    protected final Class<E> eClass;

    protected EnumCache(Class<E> eClass) {
        this.eClass = eClass;
    }

    public static <E extends Enum<E>> EnumCache<E> of(Class<E> eClass) {
        return ENUM_CACHE_CL.get(eClass);
    }

    public abstract E valueOf(String name);
}
