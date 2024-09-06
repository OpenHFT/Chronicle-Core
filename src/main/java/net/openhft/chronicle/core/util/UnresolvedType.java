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

package net.openhft.chronicle.core.util;

import java.lang.reflect.Type;

/**
 * Represents an unresolved type in a Java program, implementing the {@link Type} interface.
 * This class is used when a type name is known, but the type itself is not yet resolved at runtime.
 * <p>
 * The primary purpose of this class is to provide a way to represent types that cannot be resolved
 * using standard Java reflection mechanisms. It allows developers to create type placeholders
 * with just a name, facilitating operations that need type information without necessarily having
 * the class loaded.
 */
public class UnresolvedType implements Type {
    private final String typeName;

    /**
     * Constructs an UnresolvedType with the specified type name.
     *
     * @param typeName The name of the unresolved type. This is a string representation of the type
     *                 that has not been resolved to a specific {@link Class} object.
     */
    protected UnresolvedType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Creates a new instance of {@link UnresolvedType} with the specified type name.
     * This method provides a convenient factory method for creating instances of {@link UnresolvedType}.
     *
     * @param typeName The name of the unresolved type.
     * @return The created {@link UnresolvedType} instance.
     */
    public static Type of(String typeName) {
        return new UnresolvedType(typeName);
    }

    /**
     * Returns the name of the unresolved type.
     * This method is required by the {@link Type} interface and returns the string representation
     * of the type name that this {@link UnresolvedType} instance represents.
     *
     * @return The name of the unresolved type.
     */
    @Override
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns a string representation of the unresolved type.
     * This implementation returns the type name, providing a simple way to view the type as a string.
     *
     * @return A string representation of the unresolved type.
     */
    @Override
    public String toString() {
        return typeName;
    }
}
