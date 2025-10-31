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
package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ClassLookupTest {

    private ClassLookup classLookup = ClassAliasPool.CLASS_ALIASES;

    @Test
    void testClassLookupByName() {
        Class<?> clazz = classLookup.forName("java.lang.String");
        assertEquals(String.class, clazz);
    }

    @Test
    void testAddingAliasAndLookupByAlias() {
        classLookup.addAlias(String.class, "StringAlias");
        Class<?> clazz = classLookup.forName("StringAlias");
        assertEquals(String.class, clazz);
    }

    @Test
    void testImmutabilityOfWrappedInstance() {
        ClassLookup wrapped = classLookup.wrap();
        wrapped.addAlias(String.class, "StringAlias");

        assertThrows(ClassNotFoundRuntimeException.class, () -> classLookup.forName("StringAlias"));
    }

    @Test
    void testLookupOfLambdaClass() {
        Runnable lambda = () -> {};
        assertThrows(IllegalArgumentException.class, () -> classLookup.nameFor(lambda.getClass()));
    }
}
