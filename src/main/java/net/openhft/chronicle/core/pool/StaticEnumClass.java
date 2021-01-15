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

import net.openhft.chronicle.core.util.CoreDynamicEnum;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class StaticEnumClass<E extends Enum<E> & CoreDynamicEnum<E>> extends EnumCache<E> {
    private final E[] values;

    StaticEnumClass(Class<E> eClass) {
        super(eClass);
        this.values = eClass.getEnumConstants();
    }

    @Override
    public E valueOf(String name) {
        return name == null || name.isEmpty() ? null : Enum.valueOf(type, name);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public E forIndex(int index) {
        return values[index];
    }

    @Override
    public E[] asArray() {
        return values;
    }

    @Override
    public <T> Map<E, T> createMap() {
        return new EnumMap<>(type);
    }

    @Override
    public Set<E> createSet() {
        return EnumSet.noneOf(type);
    }
}
