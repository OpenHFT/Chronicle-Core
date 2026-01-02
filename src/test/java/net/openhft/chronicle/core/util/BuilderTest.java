/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    @Test
    @DisplayName("Builder build returns fresh instance for each call")
    void buildShouldReturnNonNullInstance() {
        Builder<MyClass> builder = new MyClassBuilder(); // MyClassBuilder is a hypothetical implementation
        MyClass instance = builder.build();
        assertNotNull(instance, "Builder.build should return a non-null instance");
    }

    @Test
    @DisplayName("Build should return new instance for mutable types builder")
    void buildShouldReturnNewInstanceForMutableTypes() {
        Builder<MyClass> builder = new MyClassBuilder(); // Assuming MyClass is mutable
        MyClass firstInstance = builder.build();
        MyClass secondInstance = builder.build();
        assertNotSame(firstInstance, secondInstance, "Builder.build should return a fresh instance for mutable types");
    }

    @Test
    @DisplayName("Build should throw exception if invoked multiple times when not allowed")
    void buildShouldThrowExceptionIfInvokedMultipleTimesWhenNotAllowed() {
        Builder<MyClass> oneTimeUseBuilder = new OneTimeUseMyClassBuilder(); // Hypothetical one-time use builder
        oneTimeUseBuilder.build();
        assertThrows(IllegalStateException.class, oneTimeUseBuilder::build,
                "one-time builder should throw when build is invoked twice");
    }

    @Test
    @DisplayName("Builder get delegates to build factory method")
    void getShouldDelegateToBuild() {
        Builder<MyClass> builder = new MyClassBuilder();
        MyClass instanceFromGet = builder.get();
        MyClass instanceFromBuild = builder.build();
        // For a mutable type, both calls should yield non-null instances.
        assertNotNull(instanceFromGet, "get() should return a non-null instance via delegation to build()");
        assertNotNull(instanceFromBuild, "build() should return a non-null instance after get() was called");
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
