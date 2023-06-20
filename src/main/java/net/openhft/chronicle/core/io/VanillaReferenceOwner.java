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
 */
public class VanillaReferenceOwner implements ReferenceOwner, QueryCloseable {

    private final String name;

    /**
     * Constructs a {@code VanillaReferenceOwner} with the specified name.
     *
     * @param name the name of the reference owner
     */
    public VanillaReferenceOwner(String name) {
        this.name = name;
    }

    /**
     * @return the name of the reference owner
     */
    @Override
    public String referenceName() {
        return toString();
    }

    /**
     * @return the string representation of the reference owner
     */
    @Override
    public String toString() {
        return "VanillaReferenceOwner{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
