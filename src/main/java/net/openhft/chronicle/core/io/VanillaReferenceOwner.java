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

package net.openhft.chronicle.core.io;

/**
 * A simple implementation of the {@link ReferenceOwner} and {@link QueryCloseable} interfaces.
 * This class represents an object that can own references, but is not closeable.
 * It holds a name for identification purposes.
 */
public class VanillaReferenceOwner implements ReferenceOwner, QueryCloseable {

    private final String name;

    /**
     * Constructs a new instance of {@code VanillaReferenceOwner} with the specified name.
     *
     * @param name the name of the reference owner, used for identification.
     * @throws IllegalArgumentException If name is null.
     */
    public VanillaReferenceOwner(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    /**
     * Returns the name of the reference owner in a string format.
     *
     * @return A {@link String} representing the name of the reference owner,
     * wrapped inside the standard string representation of this object.
     */
    @Override
    public String referenceName() {
        return toString();
    }

    /**
     * Returns the string representation of the reference owner.
     * This representation includes the name provided during the construction of this instance.
     *
     * @return A {@link String} representing the reference owner in the format: "VanillaReferenceOwner{name='[name]'}".
     */
    @Override
    public String toString() {
        return "VanillaReferenceOwner{" +
                "name='" + name + '\'' +
                '}';
    }

    /**
     * Indicates whether this reference owner is closed.
     * Since the {@code VanillaReferenceOwner} does not support the concept of being closed,
     * this method always returns {@code false}.
     *
     * @return {@code false}, indicating that this reference owner is not closeable.
     */
    @Override
    public boolean isClosed() {
        return false;
    }
}
