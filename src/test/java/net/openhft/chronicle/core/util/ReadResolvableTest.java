/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

        assertNotNull(replacement);
        assertNotSame(original, replacement);
        // Additional assertions based on the expected behavior of the replacement object
    }

    @Test
    void staticReadResolveShouldCallReadResolveForReadResolvableObjects() {
        ReadResolvableImpl original = new ReadResolvableImpl();
        ReadResolvableImpl resolved = ReadResolvable.readResolve(original);

        assertNotNull(resolved);
        assertNotSame(original, resolved);
    }

    @Test
    void staticReadResolveShouldReturnSameObjectForSerializableNonReadResolvableObjects() {
        SerializableObject serializableObject = new SerializableObject();
        SerializableObject resolved = ReadResolvable.readResolve(serializableObject);

        assertSame(serializableObject, resolved);
    }

    @Test
    void staticReadResolveShouldReturnSameObjectForNonSerializableNonReadResolvableObjects() {
        NonSerializableObject nonSerializableObject = new NonSerializableObject();
        NonSerializableObject resolved = ReadResolvable.readResolve(nonSerializableObject);

        assertSame(nonSerializableObject, resolved);
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
