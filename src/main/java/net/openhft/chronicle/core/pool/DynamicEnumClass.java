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

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * Represents a dynamic enumeration class that extends the capabilities of {@link EnumCache}.
 * The class is capable of dynamically creating and managing instances which resemble enumerations
 * (enums) in behavior. Unlike traditional enums, which have a fixed set of instances,
 * {@code DynamicEnumClass} can create new instances on-the-fly.
 *
 * <p>This class is thread-safe and ensures that each unique name maps to a single instance.
 *
 * <p>Instances of {@code DynamicEnumClass} have properties similar to traditional enums,
 * like {@code name} and {@code ordinal}. The {@code name} is the string identifier of an enum instance,
 * while the {@code ordinal} represents the position of the enum instance in the declaration order.
 *
 * @param <E> the type of enum instances this class will manage. It must extend {@link CoreDynamicEnum}.
 *            Example usage:
 *            <pre>
 *            {@code
 *            EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
 *            YesNo maybe = yesNoEnumCache.valueOf("Maybe"); // Dynamically creates a new enum instance with name "Maybe"
 *            }
 *            </pre>
 */
public class DynamicEnumClass<E extends CoreDynamicEnum<E>> extends EnumCache<E> {

    public static final CoreDynamicEnum<?>[] CORE_DYNAMIC_ENUMS = {};
    // The map and list that holds the enum instances.
    private final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<E> eList = new ArrayList<>();
    // Fields to reflectively set properties on new instances.
    private final Field nameField;
    // An array of enum values
    private E[] values = null;
    private final Field ordinalField;
    // The function used to create new enum instances
    private final Function<String, E> create = this::create;

    /**
     * Constructs a new DynamicEnumClass for managing instances of the specified class.
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
                    fieldList.add(uncheckedCast(o));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    Jvm.warn().on(getClass(), e.toString());
                }
            }
        }
        return uncheckedCast(fieldList.toArray(CORE_DYNAMIC_ENUMS));
    }

    /**
     * Returns the enum instance with the specified name. The method returns {@code null}
     * if the instance does not exist.
     *
     * @param name the name of the enum instance to be retrieved.
     * @return the enum instance with the specified name, or {@code null} if not present.
     */
    @Override
    public E get(String name) {
        return eMap.get(name);
    }

    /**
     * Returns the enum instance with the specified name. If an instance with the specified
     * name does not exist, this method dynamically creates a new one.
     *
     * @param name the name of the enum instance to be retrieved or created.
     * @return the enum instance with the specified name.
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
     * Returns an array containing the enum instances managed by this class in
     * the order they were created.
     *
     * @return an array containing the enum instances.
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
        return values = eList.toArray((E[]) Array.newInstance(type, eList.size()));
    }

    @Override
    public <T> Map<E, T> createMap() {
        // needs to be a SortedMap to behave as similarly to EnumMap as possible
        return new TreeMap<>();
    }

    @Override
    public Set<E> createSet() {
        // see comment in createMap
        return new TreeSet<>();
    }

    /**
     * Resets the internal state of this class by clearing the stored enum instances
     * and resetting them to the initial state. Use with caution as this will
     * delete any dynamically created enum instances.
     *
     * <p>This method is intended to be used for testing purposes.
     */
    @TestOnly
    public void reset() {
        values = null;
        eMap.clear();
        eList.clear();
        reset0();
    }
}
