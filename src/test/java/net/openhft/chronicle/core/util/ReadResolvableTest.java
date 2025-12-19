/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.io.Serializable;
import static org.junit.jupiter.api.Assertions.*;

class ReadResolvableTest {

    @Test
    void readResolveMethodInImplementingClassShouldReturnReplacementObject() {
        ReadResolvableImpl original = new ReadResolvableImpl();
        ReadResolvableImpl replacement = original.readResolve();

        assertNotNull(replacement, "reflection should find method");
        assertNotSame(original, replacement, "readResolve on ReadResolvable should return new replacement object");
        // Additional assertions based on the expected behavior of the replacement object
    }

    @Test
    void staticReadResolveShouldCallReadResolveForReadResolvableObjects() {
        ReadResolvableImpl original = new ReadResolvableImpl();
        ReadResolvableImpl resolved = ReadResolvable.readResolve(original);

        assertNotNull(resolved, "required object should not be null");
        assertNotSame(original, resolved, "static readResolve should return different instance for ReadResolvable");
    }

    @Test
    void staticReadResolveShouldReturnSameObjectForSerializableNonReadResolvableObjects() {
        SerializableObject serializableObject = new SerializableObject();
        SerializableObject resolved = ReadResolvable.readResolve(serializableObject);

        assertSame(serializableObject, resolved, "static readResolve should return same Serializable non-ReadResolvable object");
    }

    @Test
    void staticReadResolveShouldReturnSameObjectForNonSerializableNonReadResolvableObjects() {
        NonSerializableObject nonSerializableObject = new NonSerializableObject();
        NonSerializableObject resolved = ReadResolvable.readResolve(nonSerializableObject);

        assertSame(nonSerializableObject, resolved, "static readResolve should return same non-Serializable non-ReadResolvable object");
    }

    // Hypothetical implementation of ReadResolvable
    static class ReadResolvableImpl implements ReadResolvable<ReadResolvableImpl> {
        @Override
        public ReadResolvableImpl readResolve() {
            return new ReadResolvableImpl(); // Return a new instance or a specific replacement object
        }
    }

    @SuppressWarnings("serial")
    // Serializable object not implementing ReadResolvable
    static class SerializableObject implements Serializable {
    }

    // Non-serializable, non-ReadResolvable object
    static class NonSerializableObject {
    }
}
