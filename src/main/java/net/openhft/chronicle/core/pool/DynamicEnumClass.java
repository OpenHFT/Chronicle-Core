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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.CoreDynamicEnum;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

/**
 * Represents a dynamic enum class that extends the capabilities of {@link EnumCache}.
 * It allows dynamic creation and management of enum-like instances.
 *
 * @param <E> the type of enum instances this class will manage.
 */
public class DynamicEnumClass<E extends CoreDynamicEnum<E>> extends EnumCache<E> {

    // The map and list that holds the enum instances.
    private final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<E> eList = new ArrayList<>();

    // An array of enum values
    private E[] values = null;

    // Fields to reflectively set properties on new instances.
    private final Field nameField;
    private final Field ordinalField;

    // The function used to create new enum instances
    private final Function<String, E> create = this::create;

    /**
     * Constructs a new DynamicEnumClass.
     *
     * @param eClass the enum class this DynamicEnumClass will manage.
     */
    DynamicEnumClass(Class<E> eClass) {
        super(eClass);
        reset0();
        nameField = Jvm.getField(eClass, "name");
        ordinalField = Jvm.getFieldOrNull(eClass, "ordinal");
    }

    private void reset0() {
        E[] enumConstants = type.isEnum() ? type.getEnumConstants() : getStaticConstants(type);
        for (E e : enumConstants) {
            eMap.put(e.name(), e);
            eList.add(e);
        }
    }

    private E[] getStaticConstants(Class<E> eClass) {
        final List<E> fieldList = new ArrayList<>();
        Field[] fields = eClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == eClass) {
                try {
                    field.setAccessible(true);
                    Object o = field.get(null);
                    fieldList.add((E) o);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    Jvm.warn().on(getClass(), e.toString());
                }
            }
        }
        return (E[]) fieldList.toArray(new CoreDynamicEnum[fieldList.size()]);
    }

    /**
     * Returns the enum constant with the specified name.
     *
     * @param name the name of the enum constant to be returned.
     * @return the enum constant with the specified name.
     */
    @Override
    public E get(String name) {
        return eMap.get(name);
    }

    /**
     * Returns the enum constant with the specified name, creating it if it doesn't exist.
     *
     * @param name the name of the enum constant to be returned.
     * @return the enum constant with the specified name.
     */
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

    /**
     * Returns the number of enum instances currently managed by this class.
     *
     * @return the size of enum instances.
     */
    @Override
    public int size() {
        return eMap.size();
    }

    /**
     * Retrieves the enum instance at the given index.
     *
     * @param index the index of the enum instance to retrieve.
     * @return the enum instance at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
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

    /**
     * Resets the internal state of this class. This includes clearing the stored enum instances
     * and resetting to the initial state.
     *
     * This method should only be used for testing purposes.
     */
    @TestOnly
    public void reset() {
        values = null;
        eMap.clear();
        eList.clear();
        reset0();
    }
}
