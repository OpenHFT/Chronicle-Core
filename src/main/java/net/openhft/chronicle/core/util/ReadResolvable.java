/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * This interface is used for objects that require a post-deserialization step.
 * Objects that implement this interface can provide a custom logic for resolving the object
 * after it has been deserialized.
 *
 * @param <T> The type of object that this interface is applied to.
 */
@FunctionalInterface
public interface ReadResolvable<T> {

    /**
     * Resolves the given object by calling its readResolve method if it implements the ReadResolvable interface,
     * otherwise it checks if the object is Serializable and attempts to call its readResolve method.
     * If the object does not meet any of the above conditions, it is returned as is.
     *
     * @param o   The object to be resolved.
     * @param <T> The type of object.
     * @return The resolved object, or the original object if readResolve does not exist or is not applicable.
     */
    @SuppressWarnings("unchecked")
    static <T> T readResolve(Object o) {
        // Pattern matching
        if (o instanceof ReadResolvable) {
            return (T) ((ReadResolvable<?>) o).readResolve();
        } else if (o instanceof Serializable) {
            return (T) ObjectUtils.readResolve(o);
        } else {
            return (T) o;
        }
    }

    /**
     * A method to be implemented by classes that need to perform a post-deserialization step.
     * This method is called to return a replacement object for the one that was deserialized.
     *
     * @return The replacement object to be used after deserialization.
     */
    @NotNull
    T readResolve();
}
