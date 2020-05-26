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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class DynamicEnumClass<E extends Enum<E>> extends EnumCache<E> {
    final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Field nameField;
    private final Field ordinalField;
    private final Function<String, E> create = this::create;

    DynamicEnumClass(Class<E> eClass) {
        super(eClass);
        for (E e : eClass.getEnumConstants()) {
            eMap.put(e.name(), e);
        }
        nameField = Jvm.getField(eClass, "name");
        ordinalField = Jvm.getField(eClass, "ordinal");
    }

    @Override
    public E valueOf(String name) {
        return eMap.computeIfAbsent(name, create);
    }

    // called while holding a lock on eMap
    private E create(String name) {
        try {
            E e = OS.memory().allocateInstance(eClass);
            nameField.set(e, name);
            ordinalField.set(e, eMap.size());
            return e;

        } catch (Exception e1) {
            throw new AssertionError(e1);
        }
    }
}
