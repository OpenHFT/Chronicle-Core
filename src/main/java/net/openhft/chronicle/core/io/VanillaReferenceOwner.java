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
     * @throws IllegalArgumentException if name is null.
     */
    public VanillaReferenceOwner(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    /**
     * Returns the string representation of this reference owner, which
     * is primarily used for identification and debugging purposes.
     *
     * @return a string in the form "VanillaReferenceOwner{name='[name]'}".
     */
    @Override
    public String referenceName() {
        return toString();
    }

    /**
     * Returns the string representation of this reference owner, which
     * is primarily used for identification and debugging purposes.
     *
     * @return a string in the form "VanillaReferenceOwner{name='[name]'}".
     */
    @Override
    public String toString() {
        return "VanillaReferenceOwner{" +
                "name='" + name + '\'' +
                '}';
    }

    /**
     * Checks whether this reference owner is closed. As this implementation is not
     * actually closeable, this method always returns {@code false}.
     *
     * @return {@code false} as this reference owner is never considered closed.
     */
    @Override
    public boolean isClosed() {
        return false;
    }
}
