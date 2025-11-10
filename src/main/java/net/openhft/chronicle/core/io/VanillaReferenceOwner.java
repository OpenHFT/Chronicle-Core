//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;

/**
 * Simple {@link ReferenceOwner} that carries a name. It is not closeable and is
 * typically used in examples or tests to demonstrate {@link ReferenceCounted}
 * usage.
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
