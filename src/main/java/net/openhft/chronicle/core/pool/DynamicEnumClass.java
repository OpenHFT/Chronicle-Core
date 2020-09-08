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
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.OS;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class DynamicEnumClass<E extends DynamicEnumPooled> extends EnumCache<E> {
    final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Field nameField;
    private final Field ordinalField;
    private final Function<String, E> create = this::create;

    DynamicEnumClass(Class<E> eClass) {
        super(eClass);
        E[] enumConstants = eClass.isEnum() ? eClass.getEnumConstants() : getStaticConstants(eClass);
        for (E e : enumConstants) {
            eMap.put(e.name(), e);
        }
        nameField = Jvm.getField(eClass, "name");
        ordinalField = Enum.class.isAssignableFrom(eClass) ? Jvm.getField(eClass, "ordinal") : null;
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
        return (E[]) eList.toArray(new DynamicEnumPooled[eList.size()]);
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
            E e = OS.memory().allocateInstance(eClass);
            nameField.set(e, name);
            if (ordinalField != null)
                ordinalField.set(e, eMap.size());
            return e;

        } catch (Exception e1) {
            throw new AssertionError(e1);
        }
    }

    @Override
    public int initialSize() {
        return Maths.nextPower2(eMap.size(), 64);
    }
}
