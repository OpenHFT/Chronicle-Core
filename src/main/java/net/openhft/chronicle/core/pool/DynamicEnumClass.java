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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.CoreDynamicEnum;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class DynamicEnumClass<E extends CoreDynamicEnum<E>> extends EnumCache<E> {
    final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    final List<E> eList = new ArrayList<>();
    E[] values = null;
    private final Field nameField;
    private final Field ordinalField;
    private final Function<String, E> create = this::create;

    DynamicEnumClass(Class<E> eClass) {
        super(eClass);
        E[] enumConstants = eClass.isEnum() ? eClass.getEnumConstants() : getStaticConstants(eClass);
        for (E e : enumConstants) {
            eMap.put(e.name(), e);
            eList.add(e);
        }
        nameField = Jvm.getField(eClass, "name");
        ordinalField = Jvm.getFieldOrNull(eClass, "ordinal");
    }

    private E[] getStaticConstants(Class<E> eClass) {
        List<E> eList = new ArrayList<>();
        Field[] fields = eClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == eClass) {
                try {
                    field.setAccessible(true);
                    Object o = field.get(null);
                    eList.add((E) o);
                } catch (IllegalAccessException e) {
                    Jvm.warn().on(getClass(), e.toString());
                }
            }
        }
        return (E[]) eList.toArray(new CoreDynamicEnum[eList.size()]);
    }

    @Override
    public E get(String name) {
        return eMap.get(name);
    }

    @Override
    public E valueOf(String name) {
        return eMap.computeIfAbsent(name, create);
    }

    // called while holding a lock on eMap
    private E create(String name) {
        try {
            E e = OS.memory().allocateInstance(type);
            nameField.set(e, name);
            if (ordinalField != null) {
                ordinalField.set(e, eMap.size());
                eList.add(e);
                values = null;
            }
            return e;

        } catch (Exception e1) {
            throw new AssertionError(e1);
        }
    }

    @Override
    public int size() {
        return eMap.size();
    }

    @Override
    public E forIndex(int index) {
        return eList.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E[] asArray() {
        if (values != null)
            return values;
        return values = eList.toArray((E[])Array.newInstance(type, eList.size()));
    }

    @Override
    public <T> Map<E, T> createMap() {
        // needs to be a SortedMap so as to behave as similarly to EnumMap as possible
        return new TreeMap<>();
    }

    @Override
    public Set<E> createSet() {
        // see comment in createMap
        return new TreeSet<>();
    }
}
