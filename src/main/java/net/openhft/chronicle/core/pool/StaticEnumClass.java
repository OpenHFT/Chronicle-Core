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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a static enumeration class that extends the capabilities of {@link EnumCache}.
 * This class is designed to work with traditional Java enum types, enabling efficient
 * access and operations on the enum instances.
 *
 * <p>Unlike its counterpart {@link DynamicEnumClass}, this class does not support dynamic
 * creation of enum instances. The enum instances are fixed at compile time as per
 * standard Java enum behavior.
 *
 * @param <E> the type of enum instances this class will manage.
 *            Example usage:
 *            <pre>
 *            {@code
 *            EnumCache<Ecn> ecnEnumCache = EnumCache.of(Ecn.class);
 *            Ecn rfx = ecnEnumCache.get("RFX");
 *            }
 *            </pre>
 */
public class StaticEnumClass<E extends Enum<E>> extends EnumCache<E> {

    private final E[] values;

    /**
     * Constructs a new StaticEnumClass for managing instances of the specified enum class.
     *
     * @param eClass the enum class this StaticEnumClass will manage.
     */
    StaticEnumClass(Class<E> eClass) {
        super(eClass);
        this.values = eClass.getEnumConstants();
    }

    /**
     * Returns the enum instance with the specified name. The method returns {@code null}
     * if the name is {@code null} or empty, or if the instance does not exist.
     *
     * @param name the name of the enum instance to be retrieved.
     * @return the enum instance with the specified name, or {@code null} if not present.
     */
    @Override
    public E valueOf(String name) {
        return name == null || name.isEmpty() ? null : Enum.valueOf(type, name);
    }

    /**
     * Returns the total number of enum instances managed by this class, which corresponds
     * to the count of enum constants in the original enum class.
     *
     * @return the number of enum instances.
     */
    @Override
    public int size() {
        return values.length;
    }

    /**
     * Retrieves the enum instance at the given ordinal index.
     *
     * @param index the ordinal index of the enum instance to retrieve.
     * @return the enum instance at the given index.
     * @throws ArrayIndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public E forIndex(int index) {
        return values[index];
    }

    /**
     * Returns an array containing the enum instances managed by this class in
     * the order they were declared in the original enum class.
     *
     * @return an array containing the enum instances.
     */
    @Override
    public E[] asArray() {
        return values;
    }

    /**
     * Creates a map with enum instances as keys.
     *
     * @return a map where the keys are enum instances.
     */
    @Override
    public <T> Map<E, T> createMap() {
        return new EnumMap<>(type);
    }

    /**
     * Creates a set for holding enum instances.
     *
     * @return a set for holding enum instances.
     */
    @Override
    public Set<E> createSet() {
        return EnumSet.noneOf(type);
    }
}
