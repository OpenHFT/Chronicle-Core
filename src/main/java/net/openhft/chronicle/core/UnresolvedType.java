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
package net.openhft.chronicle.core;

import java.lang.reflect.Type;

/**
 * The UnresolvedType class represents an unresolved type.
 * It implements the Type interface.
 */
public class UnresolvedType implements Type {
    private final String typeName;

    /**
     * Constructs an UnresolvedType with the specified type name.
     *
     * @param typeName the name of the unresolved type
     */
    protected UnresolvedType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Creates a new instance of UnresolvedType with the specified type name.
     *
     * @param typeName the name of the unresolved type
     * @return the created UnresolvedType instance
     */
    public static Type of(String typeName) {
        return new UnresolvedType(typeName);
    }

    /**
     * Returns the name of the unresolved type.
     *
     * @return the name of the unresolved type
     */
    @Override
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns a string representation of the unresolved type.
     *
     * @return a string representation of the unresolved type
     */
    @Override
    public String toString() {
        return typeName;
    }
}
