/*
 * Copyright 2016-2022 chronicle.software
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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.pool.DynamicEnumClass;

/**
 * Represents the core behavior expected of a dynamic enumeration instance.
 *
 * <p>Dynamic enumerations extend the capabilities of traditional enums in Java.
 * Unlike traditional enums, which have a fixed set of instances, dynamic enumerations
 * can create new instances on-the-fly.
 *
 * <p>Classes implementing this interface are expected to have properties similar
 * to traditional enums, such as {@code name} and {@code ordinal}. The {@code name}
 * is the string identifier of the dynamic enum instance, while the {@code ordinal}
 * represents the position of the dynamic enum instance in the declaration order.
 *
 * <p>This interface is generally used in conjunction with {@link DynamicEnumClass},
 * which manages instances of dynamic enumerations.
 * <p>
 * Example usage with {@link DynamicEnumClass}:
 * <pre>
 * {@code
 * DynamicEnumClass<MyDynamicEnum> myDynamicEnums = new DynamicEnumClass<>(MyDynamicEnum.class);
 * MyDynamicEnum customInstance = myDynamicEnums.valueOf("CustomInstance");
 * }
 * </pre>
 *
 * Where {@code MyDynamicEnum} is a class implementing {@code CoreDynamicEnum}.
 * @param <E> the type of the dynamic enum instance.
 */
public interface CoreDynamicEnum<E extends CoreDynamicEnum<E>> {

    /**
     * Returns the unique name of this dynamic enum instance.
     *
     * @return A string representing the unique alias for this dynamic enum instance.
     */
    String name();

    /**
     * Returns the unique ordinal number of this dynamic enum instance.
     * The ordinal represents the position of the enum instance in the declaration order.
     *
     * @return An integer representing the unique id for this dynamic enum,
     * or -1 if the ordinal is not set.
     */
    int ordinal();
}
