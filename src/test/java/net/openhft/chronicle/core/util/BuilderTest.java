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
import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    @Test
    void buildShouldReturnNonNullInstance() {
        Builder<MyClass> builder = new MyClassBuilder(); // MyClassBuilder is a hypothetical implementation
        MyClass instance = builder.build();
        assertNotNull(instance);
    }

    @Test
    void buildShouldReturnNewInstanceForMutableTypes() {
        Builder<MyClass> builder = new MyClassBuilder(); // Assuming MyClass is mutable
        MyClass firstInstance = builder.build();
        MyClass secondInstance = builder.build();
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    void buildShouldThrowExceptionIfInvokedMultipleTimesWhenNotAllowed() {
        Builder<MyClass> oneTimeUseBuilder = new OneTimeUseMyClassBuilder(); // Hypothetical one-time use builder
        oneTimeUseBuilder.build();
        assertThrows(IllegalStateException.class, oneTimeUseBuilder::build);
    }

    @Test
    void getShouldDelegateToBuild() {
        Builder<MyClass> builder = new MyClassBuilder();
        MyClass instanceFromGet = builder.get();
        MyClass instanceFromBuild = builder.build();
        // For a mutable type, both calls should yield non-null instances.
        assertNotNull(instanceFromGet);
        assertNotNull(instanceFromBuild);
    }
}

// Hypothetical implementations of Builder
class MyClassBuilder implements Builder<MyClass> {
    @Override
    public MyClass build() {
        return new MyClass(); // Assuming MyClass is a mutable type
    }
}

class OneTimeUseMyClassBuilder implements Builder<MyClass> {
    private boolean built = false;

    @Override
    public MyClass build() {
        if (built) {
            throw new IllegalStateException("Builder can only be used once");
        }
        built = true;
        return new MyClass(); // Assuming MyClass is a mutable type
    }
}
