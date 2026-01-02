/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassLookupTest {

    private final ClassLookup classLookup = ClassAliasPool.CLASS_ALIASES;

    @Test
    @DisplayName("Lookup resolves fully qualified class name to class")
    void testClassLookupByName() {
        Class<?> clazz = classLookup.forName("java.lang.String");
        assertEquals(String.class, clazz, "forName should resolve fully-qualified class name to String.class");
    }

    @Test
    @DisplayName("Alias registration resolves alias to target class")
    void testAddingAliasAndLookupByAlias() {
        classLookup.addAlias(String.class, "StringAlias");
        Class<?> clazz = classLookup.forName("StringAlias");
        assertEquals(String.class, clazz, "forName should resolve alias 'StringAlias' to String.class");
    }

    @Test
    @DisplayName("Wrapped lookup does not mutate alias pool")
    void testImmutabilityOfWrappedInstance() {
        ClassLookup wrapped = classLookup.wrap();
        wrapped.addAlias(String.class, "StringAlias");

        assertThrows(ClassNotFoundRuntimeException.class, () -> classLookup.forName("StringAlias"),
                "wrapped lookup should not mutate the original alias pool");
    }

    @Test
    @DisplayName("Lookup rejects lambda class name resolution")
    void testLookupOfLambdaClass() {
        Runnable lambda = () -> {
        };
        assertThrows(IllegalArgumentException.class, () -> classLookup.nameFor(lambda.getClass()),
                "nameFor should reject lambda classes");
    }
}
