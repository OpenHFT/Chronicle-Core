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
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class UnresolvedTypeTest {

    @Test
    void constructorShouldInitializeTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName());
    }

    @Test
    void factoryMethodShouldCreateUnresolvedType() {
        String expectedTypeName = "MyType";
        Type type = UnresolvedType.of(expectedTypeName);

        assertTrue(type instanceof UnresolvedType);
        assertEquals(expectedTypeName, type.getTypeName());
    }

    @Test
    void getTypeNameShouldReturnCorrectTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName());
    }

    @Test
    void toStringShouldReturnTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.toString());
    }
}
