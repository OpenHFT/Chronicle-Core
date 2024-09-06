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

    // The map that holds enum instances keyed by their names.
    private final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());

    // The list that holds enum instances in the order they were created.
    private final List<E> eList = new ArrayList<>();

    // Fields used to reflectively set properties on new instances.
    private final Field nameField;
    private final Field ordinalField;

    // An array of enum values, lazily initialized.
    private E[] values = null;

    // Function used to create new enum instances dynamically.
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

    /**
     * Initializes the internal state with the predefined enum constants, if any.
     */
    private void reset0() {
        E[] enumConstants = type.isEnum() ? type.getEnumConstants() : getStaticConstants(type);
        for (E e : enumConstants) {
            eMap.put(e.name(), e);
            eList.add(e);
        }
    }

    /**
     * Retrieves static constants defined in the class using reflection.
     *
     * @param eClass the class to retrieve constants from.
     * @return an array of enum constants.
     */
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

    /**
     * Creates a new enum instance with the specified name. This method is called while
     * holding a lock on eMap to ensure thread safety.
     *
     * @param name the name for the new enum instance.
     * @return the newly created enum instance.
     */
    private E create(String name) {
        try {
            // Use low-level memory operations to instantiate the class without calling its constructor.
            E e = OS.memory().allocateInstance(type);

            // Set the name and ordinal fields using reflection.
            nameField.set(e, name);
            if (ordinalField != null) {
                ordinalField.set(e, eMap.size());
                eList.add(e);
                values = null;  // Reset cached array to force regeneration.
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
     * Returns the enum instance at the specified index.
     *
     * @param index the index of the enum instance to return.
     * @return the enum instance at the specified index.
     */
    @Override
    public E forIndex(int index) {
        return eList.get(index);
    }

    /**
     * Returns an array containing the enum instances managed by this class in
     * the order they were created.
     *
     * @return an array containing the enum instances.
     */
    @SuppressWarnings("unchecked")
    @Override
    public E[] asArray() {
        if (values != null)
            return values;
        return values = eList.toArray((E[]) Array.newInstance(type, eList.size()));
    }

    /**
     * Creates a map for associating enum instances with values.
     *
     * @param <T> the type of the values to be associated with the enum instances.
     * @return a new sorted map for enum instances.
     */
    @Override
    public <T> Map<E, T> createMap() {
        // Needs to be a SortedMap to behave as similarly to EnumMap as possible.
        return new TreeMap<>();
    }

    /**
     * Creates a set for holding enum instances.
     *
     * @return a new sorted set for enum instances.
     */
    @Override
    public Set<E> createSet() {
        // See comment in createMap.
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
